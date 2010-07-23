package org.puredata.android.io;

import org.puredata.core.PdBase;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.util.Log;

public class AudioWrapper {

	private static final String PD_AUDIO_WRAPPER = "Pd AudioWrapper";
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecordWrapper rec;
	private final AudioTrack track;
	final short outBuf[];
	
	public AudioWrapper(int sampleRate, int inChannels, int outChannels, int notificationPeriod) {
		int channelConfig = VersionedAudioFormat.getOutFormat(outChannels);
		rec = (inChannels == 0) ? null : new AudioRecordWrapper(sampleRate, inChannels, notificationPeriod);
		final short dummyBuf[] = new short[inChannels * notificationPeriod];
		final int bufSizeShorts = outChannels * notificationPeriod;
		outBuf = new short[bufSizeShorts];
		int bufSizeBytes = 2 * bufSizeShorts;
		int trackSizeBytes = bufSizeBytes;
		int minTrackSizeBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		while (trackSizeBytes < minTrackSizeBytes) trackSizeBytes += bufSizeBytes;
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, ENCODING, trackSizeBytes, AudioTrack.MODE_STREAM);
		track.setPositionNotificationPeriod(notificationPeriod);
		track.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
			@Override
			public void onPeriodicNotification(AudioTrack track) {
				short inBuf[] = dummyBuf;
				if (rec != null) {
					short newBuf[] = rec.poll();
					if (newBuf != null) {
						inBuf = newBuf;
					} else {
						Log.w(PD_AUDIO_WRAPPER, "no input buffer available; substituting dummy");
					}
				}
				PdBase.process(inBuf, outBuf);
				track.write(outBuf, 0, bufSizeShorts);
				Log.i(PD_AUDIO_WRAPPER, "periodic write notification");
			}
			
			@Override
			public void onMarkerReached(AudioTrack track) {
				// irrelevant here
			}
		});
	}
	
	public void start() {
		if (rec != null) rec.start();
		track.play();
		track.write(outBuf, 0, outBuf.length);
	}
	
	public void stop() {
		track.stop();
		if (rec != null) rec.stop();
	}
	
	public void release() {
		stop();
		track.release();
		if (rec != null) rec.release();
	}
}
