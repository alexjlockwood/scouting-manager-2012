package com.cmu.scout.test;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.cmu.scout.R;
import com.cmu.scout.bluetooth.BluetoothDevicePicker;
import com.cmu.scout.bluetooth.BluetoothScoutReceiver;
import com.cmu.scout.bluetooth.BluetoothScoutService;
import com.cmu.scout.provider.ScoutContract.Teams;

public class SendToDeviceActivity extends FragmentActivity implements
		TeamListFragment.OnTeamSelectedListener{

	// TODO: what if the receiver is in a different activity but a connection is still in progress?
	// this would mean that when the person sends the data, it will crash the phone. in other words,
	// if the client is not in the activity, then the server should not still be connected to the device.
	
	// TODO: cover case when the connection is lost due to un-clean cases.
	// if the app force closes... then the connection for the OTHER device should
	// be fresh and ready to go. this is currently not the case. if a force close
	// occurs then the only way to get it to work is by restarting both applications.
	//
	// this has something to do with the following logcat error:
	// E/BluetoothService.cpp(   94): stopDiscoveryNative: D-Bus error in StopDiscovery: org.bluez.Error.Failed (Invalid discovery session)
	
	// TODO: also... don't allow the user attempt to "connect/scan/discover" once
	// a connection has been made... this just messes everything up!!
	
	// TODO: give an easy way for the user to close the connection. this shouldn't
	// happen on its own after the user closes the application... bad practice!!
	
	// TODO: disable buttons once connection has been made.
	
	// TODO: CHECK FOR AIRPLANE MODE... WEIRD THINGS HAPPEN!!
	
	// TODO: "MAKE DISCOVERABLE" ACTION ITEM SHOULD RESTART THE "120" SECOND TIMER!!
	
	// TODO: phone can't seem to connect to tablet?? WHY?!?!?!
	
	// Debugging
    private static final String TAG = "SendToDeviceActivity";
    private static final boolean DEBUG = true;
	
    // Data-transfer types sent from the Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    // private static final int REQUEST_CONNECT_DEVICE = 2;
        
    // Name of the connected device
    private String mConnectedDeviceName = null;    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;    
    // Member object for the bluetooth services
    private BluetoothScoutService mService = null;
    // BroadcastReceiver responsible for opening the device-picker
	private final BluetoothScoutReceiver mBluetoothScoutReceiver = 
			new BluetoothScoutReceiver(this);

	// Number of seconds that the device will be discoverable.
	private static final int TIME_DISCOVERABLE = 120;
    
    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
        	if(DEBUG) Log.v(TAG, "MESSAGE_STATE_CHANGE: " + message.what);  

            switch (message.what) { 
            
            case MESSAGE_STATE_CHANGE:
            	if(DEBUG) Log.v(TAG, "MESSAGE_STATE_CHANGE: " + message.arg1);              
                switch (message.arg1) {
                case BluetoothScoutService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));                    
                    break;
                case BluetoothScoutService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothScoutService.STATE_LISTEN:
                	setStatus(R.string.title_not_connected);
                	// TODO: make sure the control flow is right... not 100% sure
                	mConnectedDeviceName = null;
                	break;
                case BluetoothScoutService.STATE_NONE:
                    setStatus(R.string.title_not_connected);                	
                	// TODO: make sure the control flow is right... not 100% sure
                	mConnectedDeviceName = null;
                    break;
                }
                break;
                
            case MESSAGE_WRITE:       
            	onMessageSent();           	
            	break;
            	
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) message.obj;             
                onMessageReceived(readBuf);
                break;
                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = message.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
                
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), message.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
                
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
    	
    	// TODO: this Activity will eventually add some sort of
    	// ListFragment to the screen layout
    	
    	// Set up the window layout
    	// setContentView(R.layout.send_to_device_main);
    	
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	// If the adapter is null, then Bluetooth is not supported.
        // Exit the Activity if this is the case.
        if (mBluetoothAdapter == null) {
        	if (DEBUG) Log.v(TAG, "Device does not support Bluetooth");
            Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Create the list fragment and add it as our sole content.
        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            TeamListFragment list = new TeamListFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
        
        /*
        Button btn = (Button) findViewById(R.id.btn_send);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        btn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO: upgrade layout to something that can actually be used.
        		sendMessage(convertTeamRowToBytes(mTeamId));
        	}
        });
        
        btnStop.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO: upgrade layout to something that can actually be used.
        		if (mService != null) {
        			// TODO: WE WOULD ONLY WANT TO DO "START"... STOP IS UNNECESSARY.
        			// BUT ALSO... ADDING A STOP BUTTON IS PROBABLY NOT THE BEST IDEA...
        			// THE CONNECTION SHOULD BE DROPPED AUTOMATICALLY WHEN THE FILE
        			// IS SENT TO PREVENT THE SENDER FROM SENDING ADDITIONAL FILES
        			// WHEN THE CONFIRM/OVERWRITE DIALOGS POP UP.
        			// mService.stop();
        			mService.start();
        		}
        	}
        });
        */
    }
    
	@Override
	public void onTeamSelected(Uri uri) {
		if (DEBUG) Log.v(TAG, "-ON TEAM SELECTED-");
		
		// TODO: ensure that we are not querying unless we are connected to a device!
		// TODO: ensure that this can't be pressed millions of times in a row... could cause
		// memory issues if we are doing expensive operations very fast (pushing repeatedly).
		if (mService != null) {
			sendMessage(convertDataToBytes(uri));
		}
	}
    
    @Override
    public void onStart() {
        super.onStart();
        if(DEBUG) Log.v(TAG, "++ ON START ++");

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            // If BT is not on, request that it be enabled. Start system Activity
            // for result. Error in enabling the Activity is handled in
            // onActivityResult().
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
        	// Otherwise, setup the service
            if (mService == null) {
            	mService = new BluetoothScoutService(this, mHandler);
            	mService.start();
            }
        }
    }
    
    // TODO: IS SYNCHRONIZED NECESSARY HERE?!?!?!?!?!
    @Override
    public void onResume() {
        super.onResume();
        if(DEBUG) Log.v(TAG, "+ ON RESUME +");
        
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
        	if (mService != null) {
        		// Only if the state is STATE_NONE, do we know that we haven't started already.
        		if (mService.getState() == BluetoothScoutService.STATE_NONE) {
        			// Start the BT service
        			//mService.start();
        		}
        	}
        }
        
        // TODO: see below in "onPause". should the service ALWAYS be stopped
        // in onPause? if so, then we would ALWAYS want to start the service in this method.
        // this will happen automatically as is (since the service's state will be STATE_NONE)
    }
    
    // TODO: IS SYNCHRONIZED NECESSARY HERE?!?!?!?!?!
    @Override
    public void onPause() {
        super.onPause();
        if(DEBUG) Log.v(TAG, "- ON PAUSE -");
        if (mService != null) {
        	if (mService.getState() == BluetoothScoutService.STATE_CONNECTING ||
      			mService.getState() == BluetoothScoutService.STATE_CONNECTED) {
        		//mService.stop();
        	}
        }
        // 05-feb-2012: thinking that it is best to keep it as is. service remains
        // running even if onPause() is called.
        // TODO: consider stopping BluetoothService here to conserve battery life.
        // TODO: but also consider whether or not this is what we really want...
        // what if the user turns off the screen accidentally during file transfer?
        // can something like this cause un-desirable side-effects?
        // also note that we would only want to "stop" the service... making it "null"
        // would prevent onResume from re-starting the service.
        
        // TODO: ANOTHER THING TO CONSIDER IS ORIENTATION CHANGES!! so it is probably best
        // to just leave it as is.
    }

    @Override
    public void onStop() {
        super.onStop();
        if(DEBUG) Log.v(TAG, "-- ON STOP --");
    }
    // TODO: commented out on 05-feb-2:21am (service cancelled on orientation change)
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(DEBUG) Log.v(TAG, "--- ON DESTROY ---");
        if (mService != null) {
        	mService.stop();
        	mService = null;
        }
        
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.v(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode != Activity.RESULT_OK) {
                if (DEBUG) Log.d(TAG, "Bluetooth not enabled");
                // User did not enable Bluetooth or an error occurred
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            } else { 
            	if (mService == null) {
                	mService = new BluetoothScoutService(this, mHandler);
                }
            }
            break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        
        // TODO: either re-think this layout, or disable send button
        // when no teams are selected.
        
        // TODO: or better yet... use the "selection" functionalities
        // to make the button only appear when teams are selected (i.e.
        // gmail application, etc.).
        
        inflater.inflate(R.menu.bluetooth_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        // TODO: RETHINK THIS BUTTON!!! SHOULD IT BE SEND??
        
        case R.id.secure_connect_scan:
        	// Launch dialog and select receiver device.
        	openDevicePicker();
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
        	ensureDiscoverable();
            return true;
        case R.id.debug:
        	debugCurrentState();
        	return true;
        }
        return false;
    }
    
    private final void setStatus(int resId) {
    	// TODO: this is only here for testing purposes... only way to test bluetooth
    	// is with my phone and tablet.final ActionBar actionBar = getActionBar();
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		final ActionBar actionBar = getActionBar();
    		actionBar.setSubtitle(resId);
    	}
    }

    private final void setStatus(String subTitle) {
    	// TODO: this is only here for testing purposes... only way to test bluetooth
    	// is with my phone and tablet.
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		final ActionBar actionBar = getActionBar();
    		actionBar.setSubtitle(subTitle);
    	}
    }
    
    private void openDevicePicker() {
    	if (DEBUG) Log.v(TAG, "scanForDevices");
    	// Launch the DevicePickerFragment (provided by the Android system).
    	// See com.cmu.scout.bluetooth.BluetoothDevicePicker for Intent constants
		registerReceiver(mBluetoothScoutReceiver, new IntentFilter(BluetoothDevicePicker.ACTION_DEVICE_SELECTED));
		startActivity(new Intent(BluetoothDevicePicker.ACTION_LAUNCH)
				.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false)
				.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE, BluetoothDevicePicker.FILTER_TYPE_ALL)
				.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));	
		// TODO: The BroadcastReceiver doesn't call "connectDevice()" on it's own. 
		// Should this be done within the Activity or here?
    }
      
    private void ensureDiscoverable() {
        if(DEBUG) Log.v(TAG, "ensureDiscoverable");

        // TODO: register this when uncommenting the "action_scan_mode_changed" in the broadcastreceiver.
        // probably good idea to test everything before we worry about fancy stuff like this.
        
        // TODO: FIX THIS WEIRD STUPID THING WITH THE "back" ARROW CAUSING THE
        // DIALOG TO NOT GO AWAY COMPLETELY!!! ARGGGG
        // registerReceiver(mBluetoothScoutReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			final Intent discover = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discover.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, TIME_DISCOVERABLE);
			discover.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(discover);
		}
		// TODO: else... do we notify the user if the device is already in discoverable mode?
		// worry about this kind of thing later
    }
    
	public void connectDevice(String address) {
		if (DEBUG) Log.v(TAG, "connectDevice");
		
		if (address != null) {
			// The BroadcastReceiver returns the selected device's address.
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// initiate connection.
			mService.connect(device);
		} else {
			// TODO: check and see if this will actually ever happen...
			Log.e(TAG, "deviceAddress is null in connectDevice");
		}
	}
	
    private void sendMessage(byte[] bytes) {
        // Check that we're actually connected before trying anything
    	
    	// mService != null added at 2:19pm on 7-feb-2012
        if (mService != null && mService.getState() != BluetoothScoutService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check that there's actually something to send
        if (bytes.length > 0) {
            // Get the message bytes and tell the BluetoothService to write
            mService.write(bytes);
        }
    }
    
    @SuppressWarnings("unused")
	private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothScoutService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mService.write(send);
        }
    }
	
    // Called when the handler returns "MESSAGE_READ"
    private void onMessageReceived(byte[] data) {
        // stop the service. connection will be lost.
    	
    	// TODO: check if this may cause issues (i.e. is it GUARANTEED
    	// that data will be 100% sent at this point?? if not, then this could be
    	// A SERIOUS ISSUE!!!).
    	
    	// uncommented on 05-feb-2012 at 1:49am
    	// mService.stop();
    	
    	// close the connection (and restart listening mode)
    	
    	showDialog(data);
    }
    
    // Called when the handler returns "MESSAGE_WRITE"
    private void onMessageSent() {
    	// 1) Simply notify the user that the data was sent.    	
    	Toast.makeText(getApplicationContext(), "Data sent!", Toast.LENGTH_SHORT).show();
        // TODO: CLOSE THE CONNECTION (not by force... don't finish the activity to do this...).           	
    }
	
    // TODO: WEIRD ERROR!! When tablet tried to send phone a team... error occured. Logcat
    // showed that the dalvikvm had an uncaught exception and had to close a thread. also:
    // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    // 
    // note: this definitely has to do with onSavedInstanceState because both
    // the tablet and N1 can receive data before onPause is called... but if onPause is called
    // afterwards (or anything that has to do with onSavedInstanceState), the application
    // crashes. easily reproducible.
	public void showDialog(byte[] data) {
		if (DEBUG) Log.v(TAG, "showDialog()");
		//getSupportFragmentManager().noteStateNotSaved();
		IncomingDataConfirmDialog.newInstance(data).show(getSupportFragmentManager(), 
				IncomingDataConfirmDialog.TAG);
	}
    
	public static class IncomingDataConfirmDialog extends DialogFragment {
		
		private static final String TAG = "IncomingDataConfirmDialog";
		private static final boolean DEBUG = true;
		
		private byte[] mData;
		
		public IncomingDataConfirmDialog(byte[] data) {
			mData = data;
		}
		
		public static IncomingDataConfirmDialog newInstance(byte[] data) {
			if (DEBUG) Log.v(TAG, "newInstance()");
			return new IncomingDataConfirmDialog(data);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (DEBUG) Log.v(TAG, "onCreateDialog");

			//TextView message = new TextView(getActivity());
			//message.setText("Another device has sent you data.\n\nAccept incoming data?");
						
			// TODO: don't reference android drawables... copy and paste
			// into the project resources
			// TODO: Make sure to put this stuff in "values/strings.xml"
			// TODO: set the message for this dialog.
			return new AlertDialog.Builder(getActivity())
				.setTitle("Incoming data")
				.setIcon(android.R.drawable.ic_dialog_info)
				/*.setView(message)*/
				.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							((SendToDeviceActivity) getActivity()).doPositiveClick(mData);
						}
					})
				.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							((SendToDeviceActivity) getActivity()).doNegativeClick();
						}
					})
				.create();
		}
	}
	
	private void doPositiveClick(byte[] data) {
		// TODO: fix the control flow so that a dialog will be presented
		// on possible conflicts (i.e. the team already exists in the receiver's database).
		
		insertDataFromBytes(data);
    	Toast.makeText(getApplicationContext(), "Data Received!", Toast.LENGTH_SHORT).show();
	}
	
	private void doNegativeClick() {
		/* do nothing */
	}
	/*
	public static class OverwriteConfirmDialog extends DialogFragment {
		
		private static final String TAG = "IncomingDataConfirmDialog";
		private static final boolean DEBUG = true;
		
		private byte[] mData;
		
		public OverwriteConfirmDialog(byte[] data) {
			mData = data;
		}
		
		public static OverwriteConfirmDialog newInstance(byte[] data) {
			if (DEBUG) Log.v(TAG, "newInstance()");
			return new IncomingDataConfirmDialog(data);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (DEBUG) Log.v(TAG, "onCreateDialog");

			// TODO: copy and paste the "android.R.drawable.ic_dialog_alert"
			// into the project resources!
			// TODO: Make sure to put this stuff in "values/strings.xml"
			return new AlertDialog.Builder(getActivity())
				.setTitle("Incoming data. Confirm?")
				.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							((SendToDeviceActivity) getActivity()).doPositiveClick(mData);
						}
					})
				.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							((SendToDeviceActivity) getActivity()).doNegativeClick();
							}
					})
				.create();
		}
	}
*/
	// TODO: as of now, this is a simple "test" implementation that sends "teams"    
    private final static String[] PROJECTION = { Teams.TEAM_NUM };
    
    // TODO: figure out how to send data from multiple tables at same time.
    // this will require more knowledge about how JSON objects are represented.
    
    private byte[] convertDataToBytes(Uri uri) {
    	if (DEBUG) Log.v(TAG, "convert uri to bytes: " + uri);
    	Cursor cur = getContentResolver().query(uri, PROJECTION, null, null, null);
    	
    	if (cur != null && cur.moveToFirst()) {	
    		try {
    			JSONObject json = new JSONObject();
    			for(String col : PROJECTION) {
    				json.put(col, cur.getString(cur.getColumnIndex(col)));
    			}
    			return json.toString().getBytes();
    		} catch (JSONException e) {
    			Log.e(TAG, "Error creating JSONObject");
    		}
    	} else {
    		Log.e(TAG, "Error in convertTeamRowToBytes");
    		if (cur == null) Log.e(TAG, "query returned null Cursor");
    		else Log.e(TAG, "query returned empty Cursor");
    	}
    	
    	// This should never happen
    	return new byte[0];
    }
    
    private void insertDataFromBytes(byte[] data) {
    	ContentValues values = new ContentValues();
    	
    	try {
    		JSONObject json = new JSONObject(new String(data));    		
    		values.put(Teams.TEAM_NUM, json.getString(Teams.TEAM_NUM));    		
    		getContentResolver().insert(Teams.CONTENT_URI, values);
    	} catch (JSONException e) {
    		Log.e(TAG, "Error parsing JSONObject");
    	}
    }
    	
    private void debugCurrentState() {
    	if (DEBUG) {
    		Log.v(TAG, "mService " + ((mService == null) ? "IS null" : "IS NOT null"));
    		Log.v(TAG, "mConnectedDeviceName " + ((mConnectedDeviceName == null) ? "IS null" : "IS NOT null"));
    		if (mService != null) {
    			mService.debugCurrentState();
    		}
    	}
    }
}
