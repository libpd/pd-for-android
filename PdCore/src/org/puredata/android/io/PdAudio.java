/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import java.io.IOException;
import java.util.Arrays;

import org.puredata.core.PdBase;
import org.puredata.core.utils.PdUtils;

import android.content.Context;


public class PdAudio {
	
	private static AudioWrapper audioWrapper = null;

	public synchronized static void initAudio(int sampleRate, int inChannels, int outChannels, int ticksPerBuffer, boolean restart)
			throws IOException {
		if (isRunning() && !restart) return;
		if (!AudioParameters.checkParameters(sampleRate, inChannels, outChannels) || ticksPerBuffer <= 0) {
			throw new IOException("bad audio parameters: " + sampleRate + ", " + inChannels + ", " + outChannels + ", " + ticksPerBuffer);
		}
		stopAudio();
		PdBase.openAudio(inChannels, outChannels, sampleRate, ticksPerBuffer);
		int bufferSizePerChannel = ticksPerBuffer * PdBase.blockSize();
		audioWrapper = new AudioWrapper(sampleRate, inChannels, outChannels, bufferSizePerChannel) {
			@Override
			protected int process(short[] inBuffer, short[] outBuffer) {
				Arrays.fill(outBuffer, (short) 0);
				return PdBase.process(inBuffer, outBuffer);
			}
		};
	}
	
	public synchronized static void startAudio(Context context) {
		if (audioWrapper == null) {
			throw new IllegalStateException("audio not initialized");
		}
		PdUtils.computeAudio(true);
		audioWrapper.start(context);
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
