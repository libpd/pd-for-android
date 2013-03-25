/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import java.io.IOException;

import org.puredata.android.service.R;
import org.puredata.android.utils.Properties;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Process;
import android.util.Log;

/**
 *
 * AudioWrapper wraps {@link AudioTrack} and {@link AudioRecord} objects and manages the main audio rendering
 * thread.  It hides the complexity of working with raw PCM audio; client code only needs to implement a JACK-style
 * audio processing callback (jackaudio.org).
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 *
 */
public abstract class AudioWrapper {

	private static final String AUDIO_WRAPPER = "AudioWrapper";
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final AudioRecordWrapper rec;
	private final AudioTrack track;
	final short outBuf[];
	final int inputSizeShorts;
	final int bufSizeShorts;
	private Thread audioThread = null;

	/**
	 * Constructor; initializes {@link AudioTrack} and {@link AudioRecord} objects
	 * 
	 * @param sampleRate
	 * @param inChannels  number of input channels
	 * @param outChannels number of output channels
	 * @param bufferSizePerChannel  number of samples per buffer per channel
	 * @throws IOException if the audio parameters are not supported by the device
	 */
	public AudioWrapper(int sampleRate, int inChannels, int outChannels, int bufferSizePerChannel) throws IOException {
		int channelConfig = VersionedAudioFormat.getOutFormat(outChannels);
		rec = (inChannels == 0) ? null : new AudioRecordWrapper(sampleRate, inChannels, bufferSizePerChannel);
		inputSizeShorts = inChannels * bufferSizePerChannel;
		bufSizeShorts = outChannels * bufferSizePerChannel;
		outBuf = new short[bufSizeShorts];
		int bufSizeBytes = 2 * bufSizeShorts;
		int trackSizeBytes = 2 * bufSizeBytes;
		int minTrackSizeBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, ENCODING);
		if (minTrackSizeBytes <= 0) {
			throw new IOException("bad AudioTrack parameters; sr: " + sampleRate +", ch: " + outChannels + ", bufSize: " + trackSizeBytes);
		}
		while (trackSizeBytes < minTrackSizeBytes) trackSizeBytes += bufSizeBytes;
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, ENCODING, trackSizeBytes, AudioTrack.MODE_STREAM);
		if (track.getState() != AudioTrack.STATE_INITIALIZED) {
			track.release();
			throw new IOException("unable to initialize AudioTrack instance for sr: " + sampleRate +", ch: " + outChannels + ", bufSize: " + trackSizeBytes);
		}
	}

	/**
	 * Main audio rendering callback, reads input samples and writes output samples; inspired by the process callback of JACK
	 * 
	 * Channels are striped across buffers, i.e., if there are two output channels, then outBuffer[0] will be the first sample
	 * for the left channel, outBuffer[1] will be the first sample for the right channel, outBuffer[2] will be the second sample
	 * for the left channel, etc.
	 * 
	 * @param inBuffer   array of input samples to be processed, e.g., from the microphone
	 * @param outBuffer  array of output samples, e.g., to be sent to the speakers
	 * @return
	 */
	protected abstract int process(short inBuffer[], short outBuffer[]);

	/**
	 * Start the audio rendering thread as well as {@link AudioTrack} and {@link AudioRecord} objects
	 * 
	 * @param context
	 */
	public synchronized void start(Context context) {
		avoidClickHack(context);
		audioThread = new Thread() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				if (rec != null) rec.start();
				track.play();
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
				if (rec != null) rec.stop();
				track.stop();
			}
		};
		audioThread.start();
	}

	/**
	 * Stop the audio thread as well as {@link AudioTrack} and {@link AudioRecord} objects
	 */
	public synchronized void stop() {
		if (audioThread == null) return;
		audioThread.interrupt();
		try {
			audioThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();  // Preserve interrupt flag for caller.
		}
		audioThread = null;
	}

	/**
	 * Release resources held by {@link AudioTrack} and {@link AudioRecord} objects;
	 * stops the audio thread if it is still running
	 */
	public synchronized void release() {
		stop();
		track.release();
		if (rec != null) rec.release();
	}

	/**
	 * @return true if and only if the audio thread is currently running
	 */
	public synchronized boolean isRunning() {
		return audioThread != null && audioThread.getState() != Thread.State.TERMINATED;
	}
	
	/**
	 * @return the audio session ID, for Gingerbread and later; will throw an exception on older versions
	 */
	public synchronized int getAudioSessionId() {
		int version = Properties.version;
		if (version >= 9) {
			return AudioSessionHandler.getAudioSessionId(track);  // Lazy class loading trick.
		} else {
			throw new UnsupportedOperationException("audio sessions not supported in Android " + version);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class AudioSessionHandler {
		private static int getAudioSessionId(AudioTrack track) {
			return track.getAudioSessionId();
		}
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
