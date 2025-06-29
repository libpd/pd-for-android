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
	private static enum State {
		NOTE_OFF, NOTE_ON, POLY_TOUCH, CONTROL_CHANGE, PROGRAM_CHANGE, AFTERTOUCH, PITCH_BEND, NONE
	}
	private State midiState = State.NONE;
	private int channel;
	private int firstByte;

	@Override
	public void onSend(byte[] msg, int offset, int count, long timestamp) {
		while(count-- != 0) processByte(msg[offset++]);
	}

	private void processByte(int b) {
		if (b < 0) {
			midiState = State.values()[(b >> 4) & 0x07];
			if (midiState != State.NONE) {
				channel = b & 0x0f;
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
