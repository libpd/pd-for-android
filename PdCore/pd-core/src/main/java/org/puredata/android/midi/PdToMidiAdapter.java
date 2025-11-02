/** 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.midi;

import org.puredata.core.PdMidiReceiver;
import android.media.midi.MidiInputPort;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Adapter class for connecting MIDI output from Pd to input for AndroidMidi.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * @author Antoine Rousseau (antoine@metalu.net)
 */
public class PdToMidiAdapter implements PdMidiReceiver {
	private Map<Integer, MidiInputPort> inputPorts = new HashMap<Integer, MidiInputPort>();

/**
 * Send Midi messages from Pd to a Midi device
 * @param inputPort input port of the Midi ouput device, as returned by MidiDevice.openInputPort()
 * @param pdPort starting at 0; Midi messages received from Pd whose channel is between
 * (16 * pdPort) and (16 * pdPort + 15) will be sent to the device with the channel reduced by (16 * pdPort)
 * <br><br>
 * Example code:
 * <pre>{@code 
MidiManager midiManager = (MidiManager) getSystemService(MIDI_SERVICE);
final MidiDeviceInfo[] infos = midiManager.getDevices();
if (infos.length == 0) return;
final MidiDeviceInfo info = infos[0]; // Select the first available device
midiManager.openDevice(info, new MidiManager.OnDeviceOpenedListener() {
	@Override
	public void onDeviceOpened(MidiDevice device) {
		if (device == null) return;
		for (MidiDeviceInfo.PortInfo portInfo : device.getInfo().getPorts()) {
			if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
				MidiInputPort inputPort = device.openInputPort(portInfo.getPortNumber());
				if (inputPort != null) {
					pdToMidiAdapter.open(inputPort, 0); // Map the device to Pd Midi port 0 (channel 0-15)
					break; // Only connect to the first available input port
				}
			}
		}
	}
}
 * }</pre>
 */
	public void open(MidiInputPort inputPort, int pdPort) {
		close(inputPort);
		close(pdPort);
		inputPorts.put(pdPort, inputPort);
	}

/**
 * Close the connection to a device port
 * @param inputPort input port of the Midi ouput device, that needs to be closed
 */
	public void close(MidiInputPort inputPort) {
		if (!inputPorts.containsValue(inputPort)) {
			return;
		}
		for (Entry<Integer, MidiInputPort> entry : inputPorts.entrySet()) {
			if (entry.getValue().equals(inputPort)) {
				close(entry.getKey());
			}
		}
	}

/**
 * Close the connection from a Pd Midi port
 * @param pdPort starting at 0; Midi messages coming from Pd for this port will be ignored, and the associated MidiInputPort will be closed
 */
	public void close(int pdPort) {
		MidiInputPort inputPort = inputPorts.get(pdPort);
		if (inputPort != null) {
			try {
				inputPort.close();
			} catch (Exception e) {
				// ignore
			}
			inputPorts.remove(pdPort);
		}
	}

/** @hidden to javadoc*/
	@Override
	public void receiveNoteOn(int channel, int pitch, int velocity) {
		write(0x90, channel, pitch, velocity);
	}

/** @hidden to javadoc*/
	@Override
	public void receivePolyAftertouch(int channel, int pitch, int value) {
		write(0xa0, channel, pitch, value);
	}

/** @hidden to javadoc*/
	@Override
	public void receiveControlChange(int channel, int controller, int value) {
		write(0xb0, channel, controller, value);
	}

/** @hidden to javadoc*/
	@Override
	public void receiveProgramChange(int channel, int program) {
		write(0xc0, channel, program);
	}

/** @hidden to javadoc*/
	@Override
	public void receiveAftertouch(int channel, int value) {
		write(0xd0, channel, value);
	}

/** @hidden to javadoc*/
	@Override
	public void receivePitchBend(int channel, int value) {
		value += 8192;
		write(0xe0, channel, (value & 0x7f), (value >> 7));
	}

/** @hidden to javadoc*/
	@Override
	public void receiveMidiByte(int port, int value) {
		final byte[] message = {(byte) value};
		writeMessage(port, message);
	}

	private static byte firstByte(int msg, int ch) {
		return (byte) (msg | (ch & 0x0f));
	}

	private void write(int msg, int ch, int a) {
		final byte[] message = {firstByte(msg, ch), (byte) a};
		writeMessage(ch, message);
	}

	private void write(int msg, int ch, int a, int b) {
		final byte[] message = {firstByte(msg, ch), (byte) a, (byte) b};
		writeMessage(ch, message);
	}

	private void writeMessage(int channel, byte[] message) {
		MidiInputPort inputPort = inputPorts.get(channel / 16);
		if (inputPort != null) try {
			inputPort.send(message, 0, message.length);
		} catch (Exception e) {
			// ignore
		}
	}

/** @hidden to javadoc*/
	@Override
	public boolean beginBlock() {
		return false;
	}

/** @hidden to javadoc*/
	@Override
	public void endBlock() {
	}
}
