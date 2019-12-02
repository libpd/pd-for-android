/** 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.midi;

import org.puredata.core.PdMidiReceiver;

import com.noisepages.nettoyeur.midi.MidiReceiver;

/**
 * Adapter class for connecting MIDI output from Pd to input for AndroidMidi.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class PdToMidiAdapter implements PdMidiReceiver {

	private final MidiReceiver receiver;
	
	/**
	 * Constructor. Note that instances of this class still need to be installed with
	 * PdBase.setMidiReceiver.
	 * 
	 * @param receiver to forward MIDI messages to
	 */
	public PdToMidiAdapter(MidiReceiver receiver) {
		this.receiver = receiver;
	}
	
	@Override
	public void receiveProgramChange(int channel, int value) {
		receiver.onProgramChange(channel, value);
	}
	
	@Override
	public void receivePolyAftertouch(int channel, int pitch, int value) {
		receiver.onPolyAftertouch(channel, pitch, value);
	}
	
	@Override
	public void receivePitchBend(int channel, int value) {
		receiver.onPitchBend(channel, value);
	}
	
	@Override
	public void receiveNoteOn(int channel, int pitch, int velocity) {
		receiver.onNoteOn(channel, pitch, velocity);
	}
	
	@Override
	public void receiveMidiByte(int port, int value) {
		receiver.onRawByte((byte) value);
	}
	
	@Override
	public void receiveControlChange(int channel, int controller, int value) {
		receiver.onControlChange(channel, controller, value);
	}
	
	@Override
	public void receiveAftertouch(int channel, int value) {
		receiver.onAftertouch(channel, value);
	}

	@Override
	public boolean beginBlock() {
		return receiver.beginBlock();
	}

	@Override
	public void endBlock() {
		receiver.endBlock();
	}
}
