package com.cmu.scout.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cmu.scout.test.SendToDeviceActivity;

public class BluetoothScoutReceiver extends BroadcastReceiver {
	
    private SendToDeviceActivity sendToDeviceActivity;

    public BluetoothScoutReceiver(SendToDeviceActivity activity) {
        this.sendToDeviceActivity = activity;
    }
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(BluetoothDevicePicker.ACTION_DEVICE_SELECTED)) {
			// Listen for device selected (in the DevicePickerFragment)
			context.unregisterReceiver(this);
			BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			sendToDeviceActivity.connectDevice(device.getAddress());
		}
		
		// TODO: check this part!
		// WHERE IS THE BEST PLACE TO UNREGISTER THE RECEIVER?
		/*
		else if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
			// Listen to changes in discoverability				    
			switch(intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)) {
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
				Toast.makeText(context, "Discoverability mode enabled...", Toast.LENGTH_SHORT).show();
				break;
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
				// TODO: what does this even mean??
				// has something to do with "page scan" vs. "inquiry scan"? Check BluetoothAdapter documentation
				Toast.makeText(sendToDeviceActivity, "Connectability mode enabled...", Toast.LENGTH_SHORT).show();
				break;
			case BluetoothAdapter.SCAN_MODE_NONE:
				// TODO: check if this should also be unregistered in the parent Activity's onDestroy method?
				context.unregisterReceiver(this);
				Toast.makeText(sendToDeviceActivity, "Discoverability mode disabled...", Toast.LENGTH_SHORT).show();
				break;
			}
		}
		*/
	}
}
