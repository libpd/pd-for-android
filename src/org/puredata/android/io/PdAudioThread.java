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


public class PdAudioThread extends Thread {

	private final Object lock = new Object();
	private static PdAudioThread thread = null;
	private AudioRecord audioIn = null;
	private AudioTrack audioOut = null;
	private int inBufferSize = 0, outBufferSize = 0, auxBufferSize = 0, auxChunkSize = 0;
	private short inBuffer[], outBuffer[], auxBuffer[];
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
	// reading the audio input in a separate thread seems like a bit of a hack, but it's necessary because
	// AudioRecord.read occasionally seems to block on my Droid X, even though it's not supposed to
	private class InputThread extends Thread {
		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
			int auxReadPos = 0, auxWritePos = 0, nRead = 0;
			while (!Thread.interrupted()) {
				while (nRead < inBufferSize) {
					audioIn.read(auxBuffer, auxWritePos, auxChunkSize);
					nRead += auxChunkSize;
					auxWritePos += auxChunkSize;
					if (auxWritePos >= auxBufferSize) {
						auxWritePos = 0;
					}
				}						
				synchronized (lock) {
					System.arraycopy(auxBuffer, auxReadPos, inBuffer, 0, inBufferSize);
				}
				nRead -= inBufferSize;
				auxReadPos += inBufferSize;
				if (auxReadPos >= auxBufferSize) {
					auxReadPos = 0;
				}
			}
		}
	}

	private PdAudioThread(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws IOException {
		super("Pd_Rendering_Thread");
		int bufferSizePerChannel = PdBase.blockSize() * ticksPerBuffer;
		if (nIn > 0) {
			int inFormat = VersionedAudioFormat.getInFormat(nIn);
			int minRecBufferSize = AudioRecord.getMinBufferSize(sampleRate, inFormat, ENCODING);
			if (minRecBufferSize > 0) {
				inBufferSize = bufferSizePerChannel * nIn;
				auxChunkSize = minRecBufferSize / 2;
				auxBufferSize = leastCommonMultiple(auxChunkSize, inBufferSize);
				int recBufferSize = bufferSize(inBufferSize, minRecBufferSize);
				audioIn = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, inFormat, ENCODING, recBufferSize);
				Log.v("Pd Thread", "rec buf: " + minRecBufferSize+", in buf: " + inBufferSize);
			} else {
				nIn = 0;
				Log.w("PdThread", "unable to open input device; running without audio input");
			}
		}
		if (nOut > 0) {
			int outFormat = VersionedAudioFormat.getOutFormat(nOut);
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
		inBuffer = new short[inBufferSize];
		outBuffer = new short[outBufferSize];
		auxBuffer = new short[auxBufferSize];

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

	private int leastCommonMultiple(int a, int b) {
		int x = a, y = b;
		while (x != y) {
			if (x < y) {
				x += a;
			} else {
				y += b;
			}
		}
		return x;
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
		PdUtils.computeAudio(true);
		thread = new PdAudioThread(sampleRate, inChannels, outChannels, ticksPerBuffer);
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
		InputThread inputThread = null;
		if (audioIn != null) {
			audioIn.startRecording();
			inputThread = new InputThread();
			inputThread.start();
		}
		if (audioOut != null) audioOut.play();
		int err = 0;
		while (!Thread.interrupted() && err == 0) {
			synchronized (lock) {
				// inBuffer is filled in the input thread, hence the lock
				err = PdBase.process(inBuffer, outBuffer);			
			}
			if (audioOut != null) audioOut.write(outBuffer, 0, outBufferSize);
		}

		if (inputThread != null) {
			inputThread.interrupt();
			try {
				inputThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (audioIn != null) audioIn.release();
		if (audioOut != null) audioOut.release();
	}
}
