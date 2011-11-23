/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import java.io.IOException;
import java.util.Arrays;

import org.puredata.core.PdBase;

import android.content.Context;

/**
 * 
 * PdAudio manages an instance of {@link AudioWrapper} that uses Pure Data for audio processing.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 *
 */
public class PdAudio {
	
	private static AudioWrapper audioWrapper = null;

	private PdAudio() {
		// Do nothing; we just don't want instances of this class.
	}
	
	/**
	 * initialize Pure Data as well as {@link AudioWrapper} instance
	 * 
	 * @param sampleRate
	 * @param inChannels      number of input channels
	 * @param outChannels     number of output channels
	 * @param ticksPerBuffer  number of Pure Data ticks (i.e., blocks of 64 samples) per buffer; choose 1 for minimal latency,
	 *                            or more if performance is a concern
	 * @param restart         flag indicating whether the audio thread should be stopped if it is currently running
	 * @throws IOException    if the audio parameters are not supported by the device
	 */
	public synchronized static void initAudio(int sampleRate, int inChannels, int outChannels, final int ticksPerBuffer, boolean restart)
			throws IOException {
		if (isRunning() && !restart) return;
		if (!AudioParameters.checkParameters(sampleRate, inChannels, outChannels) || ticksPerBuffer <= 0) {
			throw new IOException("bad audio parameters: " + sampleRate + ", " + inChannels + ", " + outChannels + ", " + ticksPerBuffer);
		}
		stopAudio();
		PdBase.openAudio(inChannels, outChannels, sampleRate);
		int bufferSizePerChannel = ticksPerBuffer * PdBase.blockSize();
		audioWrapper = new AudioWrapper(sampleRate, inChannels, outChannels, bufferSizePerChannel) {
			@Override
			protected int process(short[] inBuffer, short[] outBuffer) {
				Arrays.fill(outBuffer, (short) 0);
				return PdBase.process(ticksPerBuffer, inBuffer, outBuffer);
			}
		};
	}
	
	/**
	 * Start audio wrapper
	 * 
	 * @param context  current application context
	 */
	public synchronized static void startAudio(Context context) {
		if (audioWrapper == null) {
			throw new IllegalStateException("audio not initialized");
		}
		PdBase.computeAudio(true);
		audioWrapper.start(context);
	}

	/**
	 * Stop audio wrapper
	 */
	public synchronized static void stopAudio() {
		if (!isRunning()) return;
		audioWrapper.stop();
	}
	
	/**
	 * @return true if and only if the audio wrapper is running
	 */
	public synchronized static boolean isRunning() {
		return audioWrapper != null && audioWrapper.isRunning();
	}
	
	/**
	 * @return the audio session ID, for Gingerbread and later; will throw an exception on older versions
	 */
	public synchronized static int getAudioSessionId() {
		if (audioWrapper == null) {
			throw new IllegalStateException("audio not initialized");
		}
		return audioWrapper.getAudioSessionId();
	}
	
	/**
	 * Release resources held by audio wrapper
	 */
	public synchronized static void release() {
		if (audioWrapper == null) return;
		audioWrapper.release();
		audioWrapper = null;
	}
}
