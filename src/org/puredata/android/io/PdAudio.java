/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import org.puredata.core.PdBase;


public class PdAudio {
	
	private static AudioWrapper audioWrapper = null;
	
	public synchronized static void startAudio(int sampleRate, int inChannels, int outChannels,
			int ticksPerBuffer, boolean restart) {
		if (isRunning()) {
			if (restart) {
				stopAudio();
			} else {
				return;
			}
		}
		PdBase.openAudio(inChannels, outChannels, sampleRate, ticksPerBuffer);
		int notificationPeriod = ticksPerBuffer * PdBase.blockSize();
		audioWrapper = new AudioWrapper(sampleRate, inChannels, outChannels, notificationPeriod);
		audioWrapper.start();
	}

	public synchronized static void stopAudio() {
		if (audioWrapper == null) return;
		audioWrapper.release();
		audioWrapper = null;
	}
	
	public synchronized static boolean isRunning() {
		return audioWrapper != null;
	}
}
