/** 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.midi;

import org.puredata.core.PdMidiReceiver;
import android.media.midi.MidiInputPort;

/**
 * Adapter class for connecting MIDI output from Pd to input for AndroidMidi.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * @author Antoine Rousseau (antoine@metalu.net)
 */
public class PdToMidiAdapter implements PdMidiReceiver {
	private final MidiInputPort inputPort;

	public PdToMidiAdapter(MidiInputPort inputPort) {
		this.inputPort = inputPort;
	}

	@Override
	public void receiveNoteOn(int channel, int pitch, int velocity) {
		write(0x90, channel, pitch, velocity);
	}

	@Override
	public void receivePolyAftertouch(int channel, int pitch, int value) {
		write(0xa0, channel, pitch, value);
	}

	@Override
	public void receiveControlChange(int channel, int controller, int value) {
		write(0xb0, channel, controller, value);
	}

	@Override
	public void receiveProgramChange(int channel, int program) {
		write(0xc0, channel, program);
	}

	@Override
	public void receiveAftertouch(int channel, int value) {
		write(0xd0, channel, value);
	}

	@Override
	public void receivePitchBend(int channel, int value) {
		value += 8192;
		write(0xe0, channel, (value & 0x7f), (value >> 7));
	}

	@Override
	public void receiveMidiByte(int port, int value) {
		final byte[] message = {(byte) value};
		writeMessage(message);
	}

	private static byte firstByte(int msg, int ch) {
		return (byte) (msg | (ch & 0x0f));
	}

	private void write(int msg, int ch, int a) {
		final byte[] message = {firstByte(msg, ch), (byte) a};
		writeMessage(message);
	}

	private void write(int msg, int ch, int a, int b) {
		final byte[] message = {firstByte(msg, ch), (byte) a, (byte) b};
		writeMessage(message);
	}

	private void writeMessage(byte[] message) {
		try {
			inputPort.send(message, 0, message.length);
		} catch(Exception e) {}
	}

	@Override
	public boolean beginBlock() {
		return false;
	}

	@Override
	public void endBlock() {
	}
}
