/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * wrapper for AudioRecord; the purpose of the weird queuing mechanism is to work around the
 * AudioRecord.read blocking problem on Droid X, without messing things up on other devices
 * 
 */

package org.puredata.android.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

public class AudioRecordWrapper {

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecord rec;
	private final int bufSizeShorts;
	private final BlockingQueue<short[]> queue = new SynchronousQueue<short[]>();
	private Thread inputThread = null;

	public AudioRecordWrapper(int sampleRate, int inChannels, int ticksPerBuffer) {
		int channelConfig = VersionedAudioFormat.getInFormat(inChannels);
		bufSizeShorts = inChannels * ticksPerBuffer;
		int bufSizeBytes = 2 * bufSizeShorts;
		int recSizeBytes = 2 * bufSizeBytes;
		int minRecSizeBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		while (recSizeBytes < minRecSizeBytes) recSizeBytes += bufSizeBytes;
		rec = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, ENCODING, recSizeBytes);
	}

	public synchronized void start() {
		rec.startRecording();
		inputThread = new Thread() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				short buf1[] = new short[bufSizeShorts];
				short buf2[] = new short[bufSizeShorts];
				while (!Thread.interrupted()) {
					rec.read(buf1, 0, bufSizeShorts);
					try {
						queue.put(buf1);
					} catch (InterruptedException e) {
						break;
					}
					short tmp[] = buf1;
					buf1 = buf2;
					buf2 = tmp;
				}
			};
		};
		inputThread.start();
	}

	public synchronized void stop() {
		if (inputThread == null) return;
		inputThread.interrupt();
		try {
			inputThread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
		inputThread = null;
		rec.stop();
	}

	public synchronized void release() {
		stop();
		rec.release();
		queue.clear();
	}

	public short[] poll() {
		return queue.poll();
	}

	public short[] take() throws InterruptedException {
		return queue.take();
	}
}
