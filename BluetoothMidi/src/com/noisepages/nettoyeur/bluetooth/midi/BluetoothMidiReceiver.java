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

import android.bluetooth.BluetoothDevice;

public interface BluetoothMidiReceiver {

	void onDeviceConnected(BluetoothDevice device);

	void onConnectionFailed();

	void onConnectionLost();

	void onNoteOff(int channel, int key, int velocity);

	void onNoteOn(int channel, int key, int velocity);

	void onPolyAftertouch(int channel, int key, int velocity);

	void onControlChange(int channel, int controller, int value);

	void onProgramChange(int channel, int program);

	void onAftertouch(int channel, int velocity);

	void onPitchBend(int channel, int value);

}
