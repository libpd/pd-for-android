/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * wrapper for AudioTrack plus the main audio rendering thread
 * 
 */

package org.puredata.android.io;

import org.puredata.android.R;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Process;
import android.util.Log;


public abstract class AudioWrapper {

	private static final String AUDIO_WRAPPER = "AudioWrapper";
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecordWrapper rec;
	private final AudioTrack track;
	final short outBuf[];
	final int inputSizeShorts;
	final int bufSizeShorts;
	private Thread audioThread = null;

	public AudioWrapper(int sampleRate, int inChannels, int outChannels, int bufferSizePerChannel) {
		int channelConfig = VersionedAudioFormat.getOutFormat(outChannels);
		rec = (inChannels == 0) ? null : new AudioRecordWrapper(sampleRate, inChannels, bufferSizePerChannel);
		inputSizeShorts = inChannels * bufferSizePerChannel;
		bufSizeShorts = outChannels * bufferSizePerChannel;
		outBuf = new short[bufSizeShorts];
		int bufSizeBytes = 2 * bufSizeShorts;
		int trackSizeBytes = 2 * bufSizeBytes;
		int minTrackSizeBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		while (trackSizeBytes < minTrackSizeBytes) trackSizeBytes += bufSizeBytes;
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, ENCODING, trackSizeBytes, AudioTrack.MODE_STREAM);
	}

	protected abstract int process(short inBuffer[], short outBuffer[]);

	public synchronized void start(Context context) {
		avoidClickHack(context);
		if (rec != null) rec.start();
		track.play();
		audioThread = new Thread() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				short inBuf[];
				try {
					inBuf = (rec != null) ? rec.take() : new short[inputSizeShorts];
				} catch (InterruptedException e) {
					return;
				}
				while (!Thread.interrupted()) {
					if (process(inBuf, outBuf) != 0) break;
					track.write(outBuf, 0, bufSizeShorts);
					if (rec != null) {
						short newBuf[] = rec.poll();
						if (newBuf != null) {
							inBuf = newBuf;
						} else {
							Log.w(AUDIO_WRAPPER, "no input buffer available");
						}
					}
				}
			}
		};
		audioThread.start();
	}

	public synchronized void stop() {
		if (rec != null) rec.stop();
		if (audioThread == null) return;
		audioThread.interrupt();
		try {
			audioThread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
		audioThread = null;
		track.stop();
	}

	public synchronized void release() {
		stop();
		track.release();
		if (rec != null) rec.release();
	}

	public boolean isRunning() {
		return audioThread != null && audioThread.getState() != Thread.State.TERMINATED;
	}

	// weird little hack; eliminates the nasty click when AudioTrack (dis)engages by playing
	// a few milliseconds of silence before starting AudioTrack
	private void avoidClickHack(Context context) {
		try {
			MediaPlayer mp = MediaPlayer.create(context, R.raw.silence);
			mp.start();
			Thread.sleep(10);
			mp.stop();
			mp.release();
		} catch (Exception e) {
			Log.e(AUDIO_WRAPPER, e.toString());
		}
	}
}
