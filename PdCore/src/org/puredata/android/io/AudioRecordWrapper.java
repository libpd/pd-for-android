/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

/**
 * 
 * AudioRecordWrapper is a wrapper for {@link AudioRecord}.  It is an auxiliary class for {@link AudioWrapper};
 * the purpose of the bizarre queuing mechanism is to work around the AudioRecord.read blocking problem on Droid X,
 * without messing things up on other devices.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 *
 */
public class AudioRecordWrapper {

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecord rec;
	private final int bufSizeShorts;
	private final BlockingQueue<short[]> queue = new SynchronousQueue<short[]>();
	private Thread inputThread = null;

	public AudioRecordWrapper(int sampleRate, int inChannels, int bufferSizePerChannel) throws IOException {
		int channelConfig = VersionedAudioFormat.getInFormat(inChannels);
		bufSizeShorts = inChannels * bufferSizePerChannel;
		int bufSizeBytes = 2 * bufSizeShorts;
		int recSizeBytes = 2 * bufSizeBytes;
		int minRecSizeBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		if (minRecSizeBytes <= 0) {
			throw new IOException("bad AudioRecord parameters; sr: " + sampleRate + ", ch: " + inChannels + ", bufSize: " + bufferSizePerChannel);
		}
		while (recSizeBytes < minRecSizeBytes) recSizeBytes += bufSizeBytes;
		rec = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, ENCODING, recSizeBytes);
		if (rec != null && rec.getState() != AudioRecord.STATE_INITIALIZED) {
			rec.release();
			throw new IOException("unable to initialize AudioRecord instance for sr: " + sampleRate + ", ch: " + inChannels + ", bufSize: " + bufferSizePerChannel);
		}
	}

	public synchronized void start() {
		inputThread = new Thread() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				rec.startRecording();
				short buf[] = new short[bufSizeShorts];
				short auxBuf[] = new short[bufSizeShorts];
				while (!Thread.interrupted()) {
					int nRead = 0;
					while (nRead < bufSizeShorts && !Thread.interrupted()) {
						nRead += rec.read(buf, nRead, bufSizeShorts - nRead);
					}
					if (nRead < bufSizeShorts) break;
					try {
						queue.put(buf);
					} catch (InterruptedException e) {
						break;
					}
					short tmp[] = buf;
					buf = auxBuf;
					auxBuf = tmp;
				}
				rec.stop();
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
			Thread.currentThread().interrupt();  // Preserve interrupt flag for caller.
		}
		inputThread = null;
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
