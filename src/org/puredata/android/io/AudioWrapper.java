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

import org.puredata.core.PdBase;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;


public class AudioWrapper {

	private static final String PD_AUDIO_WRAPPER = "Pd AudioWrapper";
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecordWrapper rec;
	private final AudioTrack track;
	final short outBuf[];
	final int inputSizeShorts;
	final int bufSizeShorts;
	private Thread audioThread = null;

	public AudioWrapper(int sampleRate, int inChannels, int outChannels, int ticksPerBuffer) {
		int channelConfig = VersionedAudioFormat.getOutFormat(outChannels);
		rec = (inChannels == 0) ? null : new AudioRecordWrapper(sampleRate, inChannels, ticksPerBuffer);
		inputSizeShorts = inChannels * ticksPerBuffer;
		bufSizeShorts = outChannels * ticksPerBuffer;
		outBuf = new short[bufSizeShorts];
		int bufSizeBytes = 2 * bufSizeShorts;
		int trackSizeBytes = 2 * bufSizeBytes;
		int minTrackSizeBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		while (trackSizeBytes < minTrackSizeBytes) trackSizeBytes += bufSizeBytes;
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, ENCODING, trackSizeBytes, AudioTrack.MODE_STREAM);
	}

	public void start() {
		if (rec != null) rec.start();
		audioThread = new Thread() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				track.play();
				short inBuf[] = new short[inputSizeShorts];
				while (!Thread.interrupted()) {
					if (rec != null) {
						short newBuf[] = rec.poll();
						if (newBuf != null) {
							inBuf = newBuf;
						} else {
							Log.w(PD_AUDIO_WRAPPER, "no input buffer available");
						}
					}
					PdBase.process(inBuf, outBuf);
					track.write(outBuf, 0, bufSizeShorts);
				}
				track.stop();
			}
		};
		audioThread.start();
	}

	public void stop() {
		if (rec != null) rec.stop();
		if (audioThread == null) return;
		audioThread.interrupt();
		try {
			audioThread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
		audioThread = null;
	}

	public void release() {
		stop();
		track.release();
		if (rec != null) rec.release();
	}
}
