package com.cmu.scout.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cmu.scout.test.SendToDeviceActivity;

public class BluetoothScoutService {
	
	// TODO: replace hard-coded strings with strings from xml resource file
	
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean DEBUG = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothService";
    
    // Unique UUID for this application
    private static final UUID MY_UUID =
    		UUID.fromString("8e1f0cf7-508f-4875-b62c-fbb67fd34812");//UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    /** 
     * WARNING!!!! CHECK ALL COMBINATIONS OF POSSIBLE CONNECTIONS!!! 
     * PHONE TO TABLET. TABLET TO PHONE. ETC.
 	 * 
 	 * the following uuid works w/ xoom connecting to phone
     * where the phone is discoverable) but not the other way
     * around!
     * 
     * cee1fffb-01d6-41ce-9500-d4e6e1417ecf
     */

    // Member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // debugging variables
    public boolean acceptRunning = false;
    public boolean connectRunning = false;
    public boolean connectedRunning = false;
    //public 
    
    /**
     * Constructor. Prepares a new Bluetooth session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothScoutService(Context context, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }
    
    public void debugCurrentState() {
    	String state;
    	switch (mState) {
    		case STATE_NONE: state = "STATE_NONE"; break;
    		case STATE_LISTEN: state = "STATE_LISTEN"; break;
    		case STATE_CONNECTING: state = "STATE_CONNECTING"; break;
    		case STATE_CONNECTED: state = "STATE_CONNECTED"; break;
    		default: state = "ERROR: UNKNOWN?!?"; break;
    	}
    	
    	Log.v(TAG, "mService state: " + state);
		Log.v(TAG, "mAcceptThread " + ((mAcceptThread == null) ? "IS null" : "IS NOT null"));
		Log.v(TAG, "mConnectThread " + ((mConnectThread == null) ? "IS null" : "IS NOT null"));
		Log.v(TAG, "mConnectedThread " + ((mConnectedThread == null) ? "IS null" : "IS NOT null"));
		
		Log.v(TAG, "mAcceptThread " + ((acceptRunning) ? "IS running" : "IS NOT running"));
		Log.v(TAG, "mConnectThread " + ((connectRunning) ? "IS running" : "IS NOT running"));
		Log.v(TAG, "mConnectedThread " + ((connectedRunning) ? "IS running" : "IS NOT running"));
    }

    /**
     * Start the service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume(). 
     */
    public synchronized void start() {
        if (DEBUG) Log.v(TAG, "start()");

        //if (mAcceptThread != null) {
        //	mAcceptThread.cancel();
        //	mAcceptThread = null;
        //}
        
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }
        
    	connectRunning = false;
    	connectedRunning = false;
    	acceptRunning = true;
        
    	setState(STATE_LISTEN);
        
        //Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
        	mAcceptThread = new AcceptThread();
        	mAcceptThread.start();        	
        }
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (DEBUG) Log.v(TAG, "stop()");
        
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;        	
        }
              
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }
           
        // Stop the thread that is listening on a BluetoothServerSocket
        if (mAcceptThread != null) {
        	mAcceptThread.cancel(); 
        	mAcceptThread = null;        	
        }
        
    	connectRunning = false;
    	connectedRunning = false;
        acceptRunning = false;
        
        setState(STATE_NONE);
    }
    
    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (DEBUG) Log.v(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. 
     */
    public synchronized int getState() {    	
    	if (DEBUG) Log.v(TAG, "getState()");
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (DEBUG) Log.v(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
            	mConnectThread.cancel(); 
            	mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        
    	connectedRunning = false;
        connectRunning = true;
        
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (DEBUG) Log.v(TAG, "connected()");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
        	mAcceptThread.cancel(); 
        	mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
        acceptRunning = false;
    	connectedRunning = true;
    	connectRunning = false;

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(SendToDeviceActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
    	if (DEBUG) Log.v(TAG, "write()");

        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
    	if (DEBUG) Log.v(TAG, "connectionFailed()");

    	setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(SendToDeviceActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	if (DEBUG) Log.v(TAG, "connectionLost()");
		
    	// Start the service over to restart listening mode
		// TODO: should this be re-started once the connection has been lost?
		// TODO: this was uncommented on 5-feb-2012 1:05am 
		// (not sure why it was originally uncommented) because it seems like once the
		// connection was lost on the server side, the accept thread never restarted
		// to listen for incoming connection requests afterwards.


		
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(SendToDeviceActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
  
    private class AcceptThread extends Thread {
    	// The local server socket. Listens for connection and eventually receives 
    	// BluetoothSocket (unless connection is lost or cancelled).
    	private final BluetoothServerSocket mmServerSocket;

    	public AcceptThread() {
    		BluetoothServerSocket tmp = null; 		
    		try {
    			// NAME: 
				// The string is an identifiable name of your service, which the
				// system will automatically write to a new Service Discovery
				// Protocol (SDP) database entry on the device (the name is
				// arbitrary and can simply be your application name).
				
    			// MY_UUID: 
    			// The UUID is also included in the SDP entry and will be the
				// basis for the connection agreement with the client device.
				// That is, when the client attempts to connect with this
				// device, it will carry a UUID that uniquely identifies the
				// service with which it wants to connect. These UUIDs must
				// match in order for the connection to be accepted.
    			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
    		} catch(IOException e) {
    			Log.e(TAG, "listen() failed", e);
    		}
    		mmServerSocket = tmp;
    	}

    	public void run() {
    		if (DEBUG) Log.v(TAG, "BEGIN mAcceptThread " + this);    	
    		
    		setName("AcceptThread");    		
    		BluetoothSocket socket = null;
    		
			// Listen to the server socket if we are not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					// make synchronized to avoid race conditions
					synchronized (BluetoothScoutService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Do work to manage the connection in a separate
							// thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
    		if (DEBUG) Log.v(TAG, "END mAcceptThread");
    	}
    	
    	// public method that can close the private BluetoothSocket in the event that
    	// you need to stop listening on the server socket.
    	public void cancel() {
    		if (DEBUG) Log.v(TAG, "Cancel " + this);
    		try {
    			mmServerSocket.close();
    		} catch (IOException e) {
    			Log.e(TAG, "Error closing bluetooth server socket", e);
    		}
    	}
    }
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
    	private final BluetoothSocket mmSocket;
    	private final BluetoothDevice mmDevice;
    	
    	public ConnectThread(BluetoothDevice device) {
    		mmDevice = device;
    		BluetoothSocket tmp = null;
    		
    		// Get a BluetoothSocket to connect with the given BluetoothDevice
    		try {
    			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
    		} catch (IOException e) { 
    			Log.e(TAG, "Error creating BluetoothSocket", e);
    		}
    		mmSocket = tmp;
    	}
    	
    	public void run() {
    		if (DEBUG) Log.v(TAG, "BEGIN mConnectThread");
    		setName("ConnectThread");
    		
    		// Always cancel discovery because it will slow down a connection
    		mBluetoothAdapter.cancelDiscovery();
    		
    		try {
    		// Connect the device through the socket. This will block until it
    		// succeeds or throws an exception.
    			mmSocket.connect();
    		} catch (IOException e1) {
                Log.v(TAG, "Connection failed in mConnectThread.run(). BluetoothScoutService.this.start() called.");
    			connectionFailed();
    			// Close the socket
    			try {
    				mmSocket.close();
    			} catch (IOException e2) {
    				Log.e(TAG, "Unable to close() socket during connection failure", e2);
    			}    			
                // Start the service over to restart listening mode
                BluetoothScoutService.this.start();
    			//BluetoothScoutService.this.stop();
                return;
    		}
    		 		
    		// Reset the ConnectThread because we are done.
    		synchronized (BluetoothScoutService.this) {
    			mConnectThread = null;
    		}
    		
    		connected(mmSocket, mmDevice);
    	}
    	
    	// Cancels an in-progress connection and closes the socket
    	public void cancel() {
    		try {
    			mmSocket.close();
    		} catch (IOException e) {
    			Log.e(TAG, "close() of connect socket failed", e);
    		}
    	}
    }
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
    	private static final String TAG = "ConnectedThread";
       	private final BluetoothSocket mmSocket;
    	private final InputStream mmInStream;
    	private final OutputStream mmOutStream;
    	
    	public ConnectedThread(BluetoothSocket socket) {
    		if (DEBUG) Log.v(TAG, "CREATE mConnectedThread");
    		
    		mmSocket = socket;
    		InputStream tmpIn = null;
    		OutputStream tmpOut = null;
    		
    		// Get the input and output streams from the BluetoothSocket
    		try {
    			tmpIn = socket.getInputStream();
    			tmpOut = socket.getOutputStream();
    		} catch (IOException e) {
    			Log.e(TAG, "Error creating temporary bluetooth sockets", e);
    		}
    		
    		mmInStream = tmpIn;
    		mmOutStream = tmpOut;
    	}
    
    	public void run() {
    		if (DEBUG) Log.v(TAG, "BEGIN mConnectedThread");
    		
    		// buffer store for the stream
    		byte[] buffer = new byte[1024];
    		
    		// bytes returned from read()
    		int bytes;
    	
    		// Keep listening to the InputStream while connected (until an
    		// exception occurs).
    		while (true) {
    			try {
    				// Read from the InputStream
    				// TODO: loop over this? i.e. what if the data being sent is over 1024 bytes?
    				// TODO: also check to see if there is a bug in how this data is read in by
    				// the InputStream.
    				bytes = mmInStream.read(buffer);
    				
    				// Send the obtained bytes back to the UI Activity
    				mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
    			} catch (IOException e) {
    				if (DEBUG) Log.v(TAG, "END mConnectedThread");  	
    				Log.e(TAG, "Connection lost in mConnectedThread.run(). BluetoothScoutService.this.start() called.");
    				connectionLost();
    		    	BluetoothScoutService.this.start();
    				break;
    			}
    		}
    	}
    	
    	// Call this from the UI Activity to send data to the remote device
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(SendToDeviceActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        // Call this from the UI Activity to shutdown the connection
        public void cancel() {
			/*	if (mmInStream != null) {
					try {
						mmInStream.close();
					} catch (IOException e) {
						Log.e(TAG, "Error closing mmInStream in mConnectedThread");
					}
					//mmInStream = null;
				}

				if (mmOutStream != null) {
					try {
						mmOutStream.close();
					} catch (IOException e) {
						Log.e(TAG, "Error closing mmInStream in mConnectedThread");
					}
					//mmOutStream = null;
				}
*/
				if (mmSocket != null) {
					try {
						mmSocket.close();
					} catch (Exception e) {
						Log.e(TAG, "Error closing mmSocket in mConnectionThread");
					}
					//mmSocket = null;
				}
			}
    }
}
