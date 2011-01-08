/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.utils;

import java.io.IOException;

import org.puredata.core.PdBase;
import org.puredata.core.PdMidiReceiver;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.noisepages.nettoyeur.bluetooth.BluetoothSppObserver;
import com.noisepages.nettoyeur.bluetooth.midi.BluetoothMidiReceiver;
import com.noisepages.nettoyeur.bluetooth.midi.BluetoothMidiService;

/**
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class BluetoothMidiBridge implements BluetoothMidiReceiver {
	
	private final static String TAG = "BluetoothMidiBridge";
	
	public static void establishMidiBridge(BluetoothMidiService service, BluetoothSppObserver observer) {
		BluetoothMidiBridge bridge = new BluetoothMidiBridge(service, observer);
		PdBase.setMidiReceiver(bridge.receiver);
		service.setReceiver(bridge);
	}

	private final BluetoothMidiService service;
	private final BluetoothSppObserver observer;
	
	private final PdMidiReceiver receiver = new PdMidiReceiver() {
		@Override
		public void receiveProgramChange(int channel, int value) {
			try {
				service.sendProgramChange(channel, value);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		@Override
		public void receivePolyAftertouch(int channel, int pitch, int value) {
			try {
				service.sendPolyAftertouch(channel, pitch, value);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		@Override
		public void receivePitchBend(int channel, int value) {
			try {
				service.sendPitchbend(channel, value);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		@Override
		public void receiveNoteOn(int channel, int pitch, int velocity) {
			try {
				service.sendNoteOn(channel, pitch, velocity);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		@Override
		public void receiveControlChange(int channel, int controller, int value) {
			try {
				service.sendControlChange(channel, controller, value);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		@Override
		public void receiveAftertouch(int channel, int value) {
			try {
				service.sendAftertouch(channel, value);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	};
	
	private BluetoothMidiBridge(BluetoothMidiService service, BluetoothSppObserver observer) {
		this.service = service;
		this.observer = observer;
	}
	
	@Override
	public void onAftertouch(int channel, int velocity) {
		PdBase.sendAftertouch(channel, velocity);
	}

	@Override
	public void onControlChange(int channel, int controller, int value) {
		PdBase.sendControlChange(channel, controller, value);
	}

	@Override
	public void onNoteOff(int channel, int key, int velocity) {
		PdBase.sendNoteOn(channel, key, 0);
	}

	@Override
	public void onNoteOn(int channel, int key, int velocity) {
		PdBase.sendNoteOn(channel, key, velocity);
	}

	@Override
	public void onPitchBend(int channel, int value) {
		PdBase.sendPitchBend(channel, value);
	}

	@Override
	public void onPolyAftertouch(int channel, int key, int velocity) {
		PdBase.sendPolyAftertouch(channel, key, velocity);
	}

	@Override
	public void onProgramChange(int channel, int program) {
		PdBase.sendProgramChange(channel, program);
	}

	@Override
	public void onConnectionFailed() {
		observer.onConnectionFailed();
	}

	@Override
	public void onConnectionLost() {
		observer.onConnectionLost();
	}

	@Override
	public void onDeviceConnected(BluetoothDevice device) {
		observer.onDeviceConnected(device);
	}
}
