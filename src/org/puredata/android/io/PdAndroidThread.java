/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * This class takes care of audio rendering, managing the rendering thread as well
 * as Android resources such as audio input and output
 * 
 */

package org.puredata.android.io;

import java.io.IOException;

import org.puredata.core.PdBase;
import org.puredata.core.utils.PdUtils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;


public class PdAndroidThread extends Thread {

	private static PdAndroidThread thread = null;
	private AudioRecord audioIn = null;
	private AudioTrack audioOut = null;
	private int inBufferSize = 0, outBufferSize = 0;
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private PdAndroidThread(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws IOException {
		int bufferSizePerChannel = PdBase.blockSize() * ticksPerBuffer;
		if (nIn > 0) {
			int inFormat = getInFormat(nIn);
			int minRecBufferSize = AudioRecord.getMinBufferSize(sampleRate, inFormat, ENCODING);
			if (minRecBufferSize > 0) {
				inBufferSize = bufferSizePerChannel * nIn;
				int recBufferSize = bufferSize(inBufferSize, minRecBufferSize);
				audioIn = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, inFormat, ENCODING, recBufferSize);
				Log.v("Pd Thread", "rec buf: "+recBufferSize+", in buf: " + inBufferSize);
			} else {
				nIn = 0;
				Log.w("PdThread", "unable to open input device; running without audio input");
			}
		}
		if (nOut > 0) {
			int outFormat = getOutFormat(nOut);
			int minTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, outFormat, ENCODING);
			if (minTrackBufferSize > 0) {
				outBufferSize = bufferSizePerChannel * nOut;
				int trackBufferSize = bufferSize(outBufferSize, minTrackBufferSize);
				audioOut = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
						outFormat, ENCODING, trackBufferSize, AudioTrack.MODE_STREAM);
				Log.v("Pd Thread", "track buf: "+trackBufferSize+", out buf: " + outBufferSize);
			} else {
				nOut = 0;
				Log.w("PdThread", "unable to open output device; running without audio output");
			}
		}
		
		int err = PdBase.openAudio(nIn, nOut, sampleRate, ticksPerBuffer);
		if (err != 0) {
			throw new IOException("Pd error code " + err);
		}
	}

	private int bufferSize(int baseSize, int minSize) throws IOException {
		int size = minSize;
		while (size < 2 * baseSize) {
			size *= 2;
		}
		return size;
	}

	private int getInFormat(int inChannels) {
		switch (inChannels) {
		case 1: return AudioFormat.CHANNEL_IN_MONO;
		case 2: return AudioFormat.CHANNEL_IN_STEREO;
		default: throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
		}
	}

	private int getOutFormat(int outChannels) {
		switch (outChannels) {
		case 1: return AudioFormat.CHANNEL_OUT_MONO;
		case 2: return AudioFormat.CHANNEL_OUT_STEREO;
		case 4: return AudioFormat.CHANNEL_OUT_QUAD;
		case 6: return AudioFormat.CHANNEL_OUT_5POINT1;
		case 8: return AudioFormat.CHANNEL_OUT_7POINT1;
		default: throw new IllegalArgumentException("illegal number of output channels: " + outChannels);
		}
	}
	
	/**
	 * 
	 * @return true if and only if there is a running audio thread
	 */
	public synchronized static boolean isRunning() {
		return thread != null && thread.getState() != Thread.State.TERMINATED;
	}
	
	/**
	 * initializes audio i/o for Pd and Android, launches audio rendering thread
	 * 
	 * @param sampleRate
	 * @param inChannels      number of input channels
	 * @param outChannels     number of output channels
	 * @param ticksPerBuffer  number of pd ticks per buffer
	 * @param restart         if there is a currently active rendering thread, a value of true will force a restart;
	 * 	                      false will leave the current thread intact and return
	 * @throws IOException
	 */
	public synchronized static void startThread(int sampleRate, int inChannels, int outChannels,
			int ticksPerBuffer, boolean restart) throws IOException {
		if (isRunning() && !restart) return;
		stopThread();
		thread = new PdAndroidThread(sampleRate, inChannels, outChannels, ticksPerBuffer);
		thread.start();
	}

	/**
	 * stops the audio rendering thread, if there is one
	 */
	public synchronized static void stopThread() {
		if (thread == null) return;
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
		thread = null;
	}
 
	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
		PdUtils.computeAudio(true);
		if (audioIn != null) audioIn.startRecording();
		if (audioOut != null) audioOut.play();
		short[] inBuffer = new short[inBufferSize];
		short[] outBuffer = new short[outBufferSize];
		int err = 0;
		while (!Thread.interrupted() && err == 0) {
			if (audioIn != null) audioIn.read(inBuffer, 0, inBufferSize);
			err = PdBase.process(inBuffer, outBuffer);
			if (audioOut != null) audioOut.write(outBuffer, 0, outBufferSize);
		}
		if (audioIn != null) audioIn.release();
		if (audioOut != null) audioOut.release();
	}
}
