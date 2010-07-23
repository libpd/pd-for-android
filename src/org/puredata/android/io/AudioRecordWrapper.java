package org.puredata.android.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.util.Log;

public class AudioRecordWrapper {

	private static final String PD_AUDIO_RECORD_WRAPPER = "Pd AudioRecordWrapper";
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecord rec;
	private final int bufSizeShorts;
	private final BlockingQueue<short[]> queue;
	
	public AudioRecordWrapper(int sampleRate, int inChannels, int notificationPeriod) {
		queue = new ArrayBlockingQueue<short[]>(1);
		int channelConfig = VersionedAudioFormat.getInFormat(inChannels);
		bufSizeShorts = inChannels * notificationPeriod;
		int bufSizeBytes = 2 * bufSizeShorts;
		int recSizeBytes = bufSizeBytes;
		int minRecSizeBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		while (recSizeBytes < minRecSizeBytes) recSizeBytes += bufSizeBytes;
		rec = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, ENCODING, recSizeBytes);
		rec.setPositionNotificationPeriod(notificationPeriod);
		rec.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener() {
			@Override
			public void onPeriodicNotification(AudioRecord recorder) {
				readBuffer();
			}
	
			@Override
			public void onMarkerReached(AudioRecord recorder) {
				// irrelevant here
			}
		});
	}

	private void readBuffer() {
		short buf[] = new short[bufSizeShorts];
		rec.read(buf, 0, bufSizeShorts);
		if (!queue.offer(buf)) {
			Log.w(PD_AUDIO_RECORD_WRAPPER, "queue full; discarding buffer");
		}
		Log.i(PD_AUDIO_RECORD_WRAPPER, "periodic read notification");
	}
	
	public void start() {
		rec.startRecording();
		readBuffer();
	}
	
	public void stop() {
		rec.stop();
	}
	
	public void release() {
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
