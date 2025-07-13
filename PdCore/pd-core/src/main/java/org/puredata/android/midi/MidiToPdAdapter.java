/** 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.midi;

import org.puredata.core.PdBase;
import android.media.midi.MidiReceiver;

/**
 * Adapter class for connecting output from AndroidMidi to MIDI input for Pd.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * @author Antoine Rousseau (antoine@metalu.net)
 */
public class MidiToPdAdapter extends MidiReceiver {
	private final int port;
	private static enum State {
		NOTE_OFF, NOTE_ON, POLY_TOUCH, CONTROL_CHANGE, PROGRAM_CHANGE, AFTERTOUCH, PITCH_BEND, NONE
	}
	private State midiState = State.NONE;
	private int channel;
	private int firstByte;

/**
 * Create an adapter for a specific port, to connect to a MidiOutputPort
 * @param port starting at 0; Midi messages sent to Pd will have the channel increased by (16 * port).
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
			if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
				MidiOutputPort outputPort = device.openOutputPort(portInfo.getPortNumber());
				if (outputPort != null) {
					outputPort.connect(new MidiToPdAdapter(0)); // Map the device to Pd Midi port 0 (channel 0-15)
					break; // Only connect to the first available output port
				}
			}
		}
	}
}
 * }</pre>
 */
	public MidiToPdAdapter(int port) {
		this.port = port;
	}

	@Override
	public void onSend(byte[] msg, int offset, int count, long timestamp) {
		while(count-- != 0) processByte(msg[offset++]);
	}

	private void processByte(int b) {
		if (b < 0) {
			midiState = State.values()[(b >> 4) & 0x07];
			if (midiState != State.NONE) {
				channel = b & 0x0f + 16 * port;
				firstByte = -1;
			} else {
				PdBase.sendMidiByte(0, b);
			}
		} else {
			switch (midiState) {
			case NOTE_OFF:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					PdBase.sendNoteOn(channel, firstByte, 0);
					firstByte = -1;
				}
				break;
			case NOTE_ON:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					PdBase.sendNoteOn(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case POLY_TOUCH:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					PdBase.sendPolyAftertouch(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case CONTROL_CHANGE:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					PdBase.sendControlChange(channel, firstByte, b);
					firstByte = -1;
				}
				break;
			case PROGRAM_CHANGE:
				PdBase.sendProgramChange(channel, b);
				break;
			case AFTERTOUCH:
				PdBase.sendAftertouch(channel, b);
				break;
			case PITCH_BEND:
				if (firstByte < 0) {
					firstByte = b;
				} else {
					PdBase.sendPitchBend(channel, ((b << 7) | firstByte) - 8192);
					firstByte = -1;
				}
				break;
				default /* State.NONE */:
				PdBase.sendMidiByte(0, b);
				break;
			}
		}
	}
}
