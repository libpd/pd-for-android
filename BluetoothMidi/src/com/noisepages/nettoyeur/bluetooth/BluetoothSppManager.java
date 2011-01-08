/*
 * Derived from DeviceListActivity.java in android-7/samples/BluetoothChat
 *
 * Modifications
 * Copyright (C) 2011 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * Copyright (C) 2009 The Android Open Source Project
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

package com.noisepages.nettoyeur.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


public class BluetoothSppManager {

	private static final String TAG = "BluetoothFoundation";
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // Don't change this.

	public static enum State {
		NONE,
		CONNECTING,
		CONNECTED
	}

	private final BluetoothAdapter btAdapter;
	private final BluetoothSppReceiver receiver;
	private final int bufferSize;
	private volatile State connectionState = State.NONE;
	private ConnectThread connectThread = null;
	private ConnectedThread connectedThread = null;

	public BluetoothSppManager(BluetoothSppReceiver receiver, int bufferSize) throws IOException {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			throw new IOException("Bluetooth unavailable");
		}
		if (!btAdapter.isEnabled()) {
			throw new IOException("Bluetooth disabled");
		}
		this.receiver = receiver;
		this.bufferSize = bufferSize;
	}

	public State getState() {
		return connectionState;
	}

	public synchronized void stop() {
		cancelThreads();
		setState(State.NONE);
	}

	public synchronized void connect(String addr) throws IOException {
		cancelThreads();
		BluetoothDevice device = btAdapter.getRemoteDevice(addr);
		connectThread = new ConnectThread(device);
		connectThread.start();
		setState(State.CONNECTING);
	}

	public void write(byte[] out, int offset, int count) throws IOException {
		ConnectedThread thread;
		synchronized (this) {
			if (connectionState != State.CONNECTED) {
				throw new IOException("Bluetooth not connected");
			}
			thread = connectedThread;
		}
		thread.write(out, offset, count);
	}

	private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) throws IOException {
		connectThread = null;
		cancelConnectedThread();
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		receiver.onDeviceConnected(device);
		setState(State.CONNECTED);
	}

	private void connectionFailed() {
		setState(State.NONE);
		receiver.onConnectionFailed();
	}

	private void connectionLost() {
		setState(State.NONE);
		receiver.onConnectionLost();
	}

	private void cancelThreads() {
		if (connectThread != null) {
			connectThread.cancel(); 
			connectThread = null;
		}
		cancelConnectedThread();
	}

	private void cancelConnectedThread() {
		if (connectedThread != null) {
			connectedThread.cancel(); 
			connectedThread = null;
		}
	}

	private void setState(State state) {
		connectionState = state;
	}
	
	private class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		private ConnectThread(BluetoothDevice device) throws IOException {
			this.device = device;
			this.socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
		}

		@Override
		public void run() {
			try {
				socket.connect();
				connected(socket, device);
			} catch (IOException e1) {
				connectionFailed();
				try {
					socket.close();
				} catch (IOException e2) {
					Log.e(TAG, "Unable to close socket after connection failure", e2);
				}
			}
		}

		private void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "Unable to close socket", e);
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket socket;
		private final InputStream inStream;
		private final OutputStream outStream;

		private ConnectedThread(BluetoothSocket socket) throws IOException {
			this.socket = socket;
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		}

		@Override
		public void run() {
			byte[] buffer = new byte[bufferSize];
			int nBytes;
			while (true) {
				try {
					nBytes = inStream.read(buffer);
					receiver.onBytesReceived(nBytes, buffer);
				} catch (IOException e) {
					connectionLost();
					break;
				}
			}
		}

		private void write(byte[] buffer, int offset, int count) throws IOException {
			outStream.write(buffer, offset, count);
		}

		private void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "Unable to close socket", e);
			}
		}
	}
}
