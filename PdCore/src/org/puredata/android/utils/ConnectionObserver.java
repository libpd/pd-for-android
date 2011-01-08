/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.utils;

import android.bluetooth.BluetoothDevice;

/**
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public interface ConnectionObserver {

	public void onDeviceConnected(BluetoothDevice device);

	public void onConnectionFailed();
	
	public void onConnectionLost();
}
