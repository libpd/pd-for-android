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
import org.puredata.core.utils.PdUtils;


public class PdAudio {
	
	private static AudioWrapper audioWrapper = null;
	
	public synchronized static void startAudio(int sampleRate, int inChannels, int outChannels,
			int ticksPerBuffer, boolean restart) {
		if (isRunning() && !restart) return;
		stopAudio();
		PdBase.openAudio(inChannels, outChannels, sampleRate, ticksPerBuffer);
		PdUtils.computeAudio(true);
		int bufferSizePerChannel = ticksPerBuffer * PdBase.blockSize();
		audioWrapper = new AudioWrapper(sampleRate, inChannels, outChannels, bufferSizePerChannel) {
			@Override
			protected int process(short[] inBuffer, short[] outBuffer) {
				return PdBase.process(inBuffer, outBuffer);
			}
		};
		audioWrapper.start();
	}

	public synchronized static void stopAudio() {
		if (!isRunning()) return;
		audioWrapper.release();
		audioWrapper = null;
	}
	
	public synchronized static boolean isRunning() {
		return audioWrapper != null && audioWrapper.isRunning();
	}
}
