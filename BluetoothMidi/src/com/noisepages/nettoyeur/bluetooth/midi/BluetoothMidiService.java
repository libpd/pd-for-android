/*
 * Copyright (C) 2011 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noisepages.nettoyeur.bluetooth.midi;

import java.io.IOException;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.noisepages.nettoyeur.bluetooth.BluetoothSppManager;
import com.noisepages.nettoyeur.bluetooth.BluetoothSppReceiver;


public class BluetoothMidiService extends Service {

	public static enum State {
		NOTE_OFF,
		NOTE_ON,
		POLY_TOUCH,
		CONTROL_CHANGE,
		PROGRAM_CHANGE,
		AFTERTOUCH,
		PITCH_BEND,
		NONE
	}

	private final Binder binder = new BluetoothMidiBinder();
	private BluetoothSppManager btManager = null;
	private BluetoothMidiReceiver receiver = null;
	private State midiState = State.NONE;
	private int channel;
	private int firstByte;
	
	private final BluetoothSppReceiver sppReceiver = new BluetoothSppReceiver() {
		@Override
		public void onBytesReceived(int nBytes, byte[] buffer) {
			for (int i = 0; i < nBytes; i++) {
				processByte(buffer[i]);
			}
		}

		@Override
		public void onConnectionFailed() {
			receiver.onConnectionFailed();
		}

		@Override
		public void onConnectionLost() {
			receiver.onConnectionLost();
		}

		@Override
		public void onDeviceConnected(BluetoothDevice device) {
			receiver.onDeviceConnected(device);
		}
	};

	public class BluetoothMidiBinder extends Binder {
		public BluetoothMidiService getService() {
			return BluetoothMidiService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stop();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		stop();
		super.onDestroy();
	}
	
	public void init() throws IOException {
		stop();
		btManager = new BluetoothSppManager(sppReceiver, 32);
	}
	
	public void setReceiver(BluetoothMidiReceiver receiver) {
		this.receiver = receiver;
	}
	
	public synchronized void connect(String addr) throws IOException {
		btManager.connect(addr);
	}
	
	public BluetoothSppManager.State getState() {
		return btManager.getState();
	}

	public void stop() {
		if (btManager != null) {
			btManager.stop();
		}
	}

	public void sendNoteOff(int ch, int note, int vel) throws IOException {
		write(0x80, ch, note, vel);
	}

	public void sendNoteOn(int ch, int note, int vel) throws IOException {
		write(0x90, ch, note, vel);
	}

	public void sendPolyAftertouch(int ch, int note, int vel) throws IOException {
		write(0xa0, ch, note, vel);
	}

	public void sendControlChange(int ch, int ctl, int val) throws IOException {
		write(0xb0, ch, ctl, val);
	}

	public void sendProgramChange(int ch, int pgm) throws IOException {
		write(0xc0, ch, pgm);
	}

	public void sendAftertouch(int ch, int vel) throws IOException {
		write(0xd0, ch, vel);
	}

	public void sendPitchbend(int ch, int val) throws IOException {
		val += 8192;
		write(0xe0, ch, (val & 0x7f), (val >> 7));
	}

	private void write(int msg, int ch, int a) throws IOException {
		writeBytes(firstByte(msg, ch), (byte) a);
	}

	private void write(int msg, int ch, int a, int b) throws IOException {
		writeBytes(firstByte(msg, ch), (byte) a, (byte) b);
	}

	private byte firstByte(int msg, int ch) {
		return (byte)(msg | (ch & 0x0f));
	}

	private void writeBytes(byte... out) throws IOException {
		btManager.write(out, 0, out.length);
	}

	private void processByte(int b) {
		if (b < 0) {
			midiState = State.values()[(b >> 4) & 0x07];
			channel = b & 0x0f;
			firstByte = -1;
		} else {
			switch (midiState) {
			case NOTE_OFF:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					receiver.onNoteOff(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case NOTE_ON:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					receiver.onNoteOn(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case POLY_TOUCH:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					receiver.onPolyAftertouch(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case CONTROL_CHANGE:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					receiver.onControlChange(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case PROGRAM_CHANGE:
				receiver.onProgramChange(channel, b);
				break;
			case AFTERTOUCH:
				receiver.onAftertouch(channel, b);
				break;
			case PITCH_BEND:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					receiver.onPitchBend(channel, ((b << 7) | firstByte) - 8192);
					firstByte = -1;
				}
				break;
			default:
				break;
			}
		}
	}
}
