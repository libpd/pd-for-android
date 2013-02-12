/** 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.midi;

import org.puredata.core.PdBase;

import com.noisepages.nettoyeur.midi.MidiReceiver;

/**
 * Adapter class for connecting output from AndroidMidi to MIDI input for Pd.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class MidiToPdAdapter implements MidiReceiver {

	@Override
	public void onRawByte(byte value) {
		PdBase.sendMidiByte(0, value);
	}

	@Override
	public void onProgramChange(int channel, int program) {
		PdBase.sendProgramChange(channel, program);
	}

	@Override
	public void onPolyAftertouch(int channel, int key, int velocity) {
		PdBase.sendPolyAftertouch(channel, key, velocity);
	}

	@Override
	public void onPitchBend(int channel, int value) {
		PdBase.sendPitchBend(channel, value);
	}

	@Override
	public void onNoteOn(int channel, int key, int velocity) {
		PdBase.sendNoteOn(channel, key, velocity);
	}

	@Override
	public void onNoteOff(int channel, int key, int velocity) {
		PdBase.sendNoteOn(channel, key, 0);
	}

	@Override
	public void onControlChange(int channel, int controller, int value) {
		PdBase.sendControlChange(channel, controller, value);
	}

	@Override
	public void onAftertouch(int channel, int velocity) {
		PdBase.sendAftertouch(channel, velocity);
	}

	@Override
	public boolean beginBlock() {
		return false;
	}

	@Override
	public void endBlock() {}
}