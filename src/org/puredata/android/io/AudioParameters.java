package org.puredata.android.io;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;


public class AudioParameters {
	
	private static final int COMMON_RATE = 8000;
	private static final int MAX_CHANNELS = 256;
	private static int sampleRate = 0, inputChannels = 0, outputChannels = 0, ticksPerBuffer = 0;

	static {
		init();
	}

	public static int getSampleRate() { return sampleRate; }
	public static int getInputChannels() { return inputChannels; }
	public static int getOutputChannels() { return outputChannels; }
	public static int getTicksPerBuffer() { return ticksPerBuffer; }

	private static void init() {
		for (int n = 1; n < MAX_CHANNELS; n++) {
			try {
				int b = AudioTrack.getMinBufferSize(COMMON_RATE, VersionedAudioFormat.getOutFormat(n), AudioFormat.ENCODING_PCM_16BIT);
				if (b > 0) {
					outputChannels = n;
				}
			} catch (Exception e) {
				// impossible number of channels
			}
		}
		if (outputChannels == 0) return; // no audio output found; give up
		for (int n = 0; n < 256; n++) {
			try {
				int b = AudioRecord.getMinBufferSize(8000, VersionedAudioFormat.getInFormat(n), AudioFormat.ENCODING_PCM_16BIT);
				if (b > 0) {
					inputChannels = n;
				}
			} catch (Exception e) {
				// impossible number of channels
			}
		}
		for (int sr: new int[] {COMMON_RATE, 11025, 22050, 44100}) {
			int b = AudioTrack.getMinBufferSize(sr, VersionedAudioFormat.getOutFormat(outputChannels), AudioFormat.ENCODING_PCM_16BIT);
			if (b <= 0) break;
			b = AudioRecord.getMinBufferSize(sr, VersionedAudioFormat.getInFormat(inputChannels), AudioFormat.ENCODING_PCM_16BIT);
			if (b <= 0) break;
			sampleRate = sr;
		}
		ticksPerBuffer = 1;
		while (sampleRate / ticksPerBuffer > 1024) ticksPerBuffer *= 2;  // conservative choice...
	}
}
