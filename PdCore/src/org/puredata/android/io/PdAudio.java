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
import android.os.Handler;
import android.os.Looper;

/**
 * 
 * PdAudio manages an instance of {@link AudioWrapper} that uses Pure Data for audio processing.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 *
 */
public class PdAudio {

	private static AudioWrapper audioWrapper = null;
	private static final Handler handler = new Handler(Looper.getMainLooper());
	private static final Runnable pollRunner = new Runnable() {
		@Override
		public void run() {
			PdBase.pollMidiQueue();
			PdBase.pollPdMessageQueue();
			handler.postDelayed(this, 5);
		}
	};

	private PdAudio() {
		// Do nothing; we just don't want instances of this class.
	}

	/**
	 * Initializes Pure Data as well as audio components.
	 * 
	 * @param sampleRate
	 * @param inChannels      number of input channels
	 * @param outChannels     number of output channels
	 * @param ticksPerBuffer  number of Pure Data ticks (i.e., blocks of 64 samples) per buffer; choose 1 for minimal latency,
	 *                            or more if performance is a concern; for Java audio only (Android 2.2 or earlier),
	 *                            ignored by OpenSL components
	 * @param restart         flag indicating whether the audio thread should be stopped if it is currently running
	 * @throws IOException    if the audio parameters are not supported by the device
	 */
	public synchronized static void initAudio(int sampleRate, int inChannels, int outChannels, final int ticksPerBuffer, boolean restart)
			throws IOException {
		if (isRunning() && !restart) return;
		stopAudio();
		if (PdBase.openAudio(inChannels, outChannels, sampleRate, null) != 0) {
			throw new IOException("unable to open Pd audio: " + sampleRate + ", " + inChannels + ", " + outChannels);
		}
		if (!PdBase.implementsAudio()) {
			if (!AudioParameters.checkParameters(sampleRate, inChannels, outChannels) || ticksPerBuffer <= 0) {
				throw new IOException("bad Java audio parameters: " + sampleRate + ", " + inChannels + ", " + outChannels + ", " + ticksPerBuffer);
			}
			int bufferSizePerChannel = ticksPerBuffer * PdBase.blockSize();
			audioWrapper = new AudioWrapper(sampleRate, inChannels, outChannels, bufferSizePerChannel) {
				@Override
				protected int process(short[] inBuffer, short[] outBuffer) {
					Arrays.fill(outBuffer, (short) 0);
					int err = PdBase.process(ticksPerBuffer, inBuffer, outBuffer);
					PdBase.pollMidiQueue();
					PdBase.pollPdMessageQueue();
					return err;
				}
			};
		}
	}

	/**
	 * Starts the audio components.
	 * 
	 * @param context  current application context
	 */
	public synchronized static void startAudio(Context context) {
		PdBase.computeAudio(true);
		if (PdBase.implementsAudio()) {
			handler.post(pollRunner);
			PdBase.startAudio();
		} else {
			if (audioWrapper == null) {
				throw new IllegalStateException("audio not initialized");
			}
			audioWrapper.start(context);
		}
	}

	/**
	 * Stops the audio components.
	 */
	public synchronized static void stopAudio() {
		if (PdBase.implementsAudio()) {
			PdBase.pauseAudio();
			handler.removeCallbacks(pollRunner);
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdBase.pollMidiQueue();  // Flush pending messages.
					PdBase.pollPdMessageQueue();
				}
			});
		} else {
			if (!isRunning()) return;
			audioWrapper.stop();
		}
	}

	/**
	 * @return true if and only if the audio wrapper is running
	 */
	public synchronized static boolean isRunning() {
		if (PdBase.implementsAudio()) {
			return PdBase.isRunning();
		} else {
			return audioWrapper != null && audioWrapper.isRunning();
		}
	}

	/**
	 * Releases resources held by the audio components.
	 */
	public synchronized static void release() {
		stopAudio();
		if (PdBase.implementsAudio()) {
			PdBase.closeAudio();
		} else {
			if (audioWrapper == null) return;
			audioWrapper.release();
			audioWrapper = null;
		}
	}
}
