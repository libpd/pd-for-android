/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.io;

import org.puredata.android.utils.Properties;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/**
 * 
 * AudioParameters sniffs out the number of audio input and output channels as well as
 * supported sample rates. For Android 2.3 and later, it also recommends buffer sizes and
 * checks whether the device claims low-latency features for audio.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public class AudioParameters {

	private static final String TAG = "AudioParameters";
	private static AudioParametersImpl impl = null;

	/**
	 * If you want this class to provide parameters that enable low-latency features, you need to call this method with the current context (i.e., service
	 * or activity) before you invoke any other methods of this class.
	 * 
	 * @param context activity or service that calls this method
	 */
	public static synchronized void init(Context context) {
		if (impl != null) return;
		if (Properties.version > 16 && context != null) {
			impl = JellyBeanMR1OpenSLParameters.getParameters(context);
		} else if (Properties.version > 16) {
			Log.w("AudioParameters", "Initializing audio parameters with null context on Android 4.2 or later.");
			impl = new BasicOpenSLParameters(64, 64);
		} else if (Properties.version == 16) {
			impl = JellyBeanOpenSLParameters.getParameters();
		} else if (Properties.version > 8) {
			impl = new BasicOpenSLParameters(64, 64);
		} else {
			impl = new JavaAudioParameters();
		}
	}

	/**
	 * @return true if and only if the device claims low-latency capabilities
	 */
	public static boolean supportsLowLatency() {
		init(null);
		return impl.supportsLowLatency();
	}

	/**
	 * @return a reasonable sample rate that the device supports, 0 if audio output is unavailable
	 */
	public static int suggestSampleRate() {
		init(null);
		return impl.suggestSampleRate();
	}

	/**
	 * @return the largest number of input channels that the device supports
	 */
	public static int suggestInputChannels() {
		init(null);
		return impl.suggestInputChannels();
	}

	/**
	 * @return the largest number of output channels that the device supports
	 */
	public static int suggestOutputChannels() {
		init(null);
		return impl.suggestOutputChannels();
	}

	/**
	 * @return the recommended input buffer size in frames (for OpenSL only)
	 */
	public static int suggestInputBufferSize(int sampleRate) {
		init(null);
		return impl.suggestInputBufferSize(sampleRate);
	}

	/**
	 * @return the recommended output buffer size in frames (for OpenSL only)
	 */
	public static int suggestOutputBufferSize(int sampleRate) {
		init(null);
		return impl.suggestOutputBufferSize(sampleRate);
	}

	/**
	 * @param srate sample rate
	 * @param nin   number of input channels
	 * @param nout  number of output channels
	 * @return true if and only if the device supports the given set of parameters
	 */
	public static boolean checkParameters(int srate, int nin, int nout) {
		return checkInputParameters(srate, nin) && checkOutputParameters(srate, nout);
	}

	/**
	 * @param srate sample rate
	 * @param nin   number of input channels
	 * @return true if and only if the device supports the given set of parameters
	 */
	public static boolean checkInputParameters(int srate, int nin) {
		init(null);
		return impl.checkInputParameters(srate, nin);
	}

	/**
	 * @param srate sample rate
	 * @param nout  number of output channels
	 * @return true if and only if the device supports the given set of parameters
	 */
	public static boolean checkOutputParameters(int srate, int nout) {
		init(null);
		return impl.checkOutputParameters(srate, nout);
	}

	private static interface AudioParametersImpl {
		boolean supportsLowLatency();
		boolean checkOutputParameters(int srate, int nout);
		boolean checkInputParameters(int srate, int nin);
		int suggestOutputBufferSize(int sampleRate);
		int suggestInputBufferSize(int sampleRate);
		int suggestOutputChannels();
		int suggestInputChannels();
		int suggestSampleRate();
	}
	
	// Audio parameters for Java audio API (SDK 8 and earlier).
	private static class JavaAudioParameters implements AudioParametersImpl {

		private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
		private static final int COMMON_RATE = 8000; // 8kHz seems to work everywhere, including the simulator
		private static final int MAX_CHANNELS = 8;
		private final int sampleRate;
		private final int inputChannels;
		private final int outputChannels;

		JavaAudioParameters() {
			int oc = 0;
			for (int n = 1; n < MAX_CHANNELS; n++) {
				if (checkOutputParameters(COMMON_RATE, n)) oc = n;
			}
			outputChannels = oc;
			int ic = 0;
			for (int n = 0; n < MAX_CHANNELS; n++) {
				if (checkInputParameters(COMMON_RATE, n)) ic = n;
			}
			inputChannels = ic;
			int sr = COMMON_RATE;
			for (int n: new int[] {11025, 16000, 22050, 32000, 44100}) {
				if (checkInputParameters(n, inputChannels) && checkOutputParameters(n, outputChannels))  sr = n;
			}
			sampleRate = sr;
		}

		@Override public boolean supportsLowLatency() { return false; }
		@Override public int suggestSampleRate() { return sampleRate; }
		@Override public int suggestInputChannels() { return inputChannels; }
		@Override public int suggestOutputChannels() { return outputChannels; }
		@Override public int suggestInputBufferSize(int sampleRate) { return -1; }
		@Override public int suggestOutputBufferSize(int sampleRate) { return -1; }

		@Override
		public boolean checkInputParameters(int srate, int nin) {
			try {
				return nin == 0 || AudioRecord.getMinBufferSize(srate, VersionedAudioFormat.getInFormat(nin), ENCODING) > 0;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean checkOutputParameters(int srate, int nout) {
			try {
				return AudioTrack.getMinBufferSize(srate, VersionedAudioFormat.getOutFormat(nout), ENCODING) > 0;
			} catch (Exception e) {
				return false;
			}
		}
	}

	private static class BasicOpenSLParameters  implements AudioParametersImpl {
		private final int inputBufferSize;
		private final int outputBufferSize;

		BasicOpenSLParameters(int inputBufferSize, int outputBufferSize) {
			this.inputBufferSize = inputBufferSize;
			this.outputBufferSize = outputBufferSize;
		}

		@Override public boolean supportsLowLatency() { return false; }
		@Override public int suggestSampleRate() { return 44100; }
		@Override public int suggestInputChannels() { return 1; }
		@Override public int suggestOutputChannels() { return 2; }
		@Override public int suggestInputBufferSize(int sampleRate) { return inputBufferSize; }
		@Override public int suggestOutputBufferSize(int sampleRate) { return outputBufferSize; }

		@Override
		public boolean checkInputParameters(int srate, int nin) {
			return srate > 0 && nin >= 0 && nin <= 2;
		}

		@Override
		public boolean checkOutputParameters(int srate, int nout) {
			return srate > 0 && nout >= 0 && nout <= 2;
		}
	}

	private static class JellyBeanOpenSLParameters extends BasicOpenSLParameters {
		private final int nativeBufferSize;
		private final boolean lowLatency;

		JellyBeanOpenSLParameters(int inputBufferSize, int outputBufferSize, int nativeBufferSize, boolean lowLatency) {
			super(inputBufferSize, outputBufferSize);
			this.nativeBufferSize = nativeBufferSize;
			this.lowLatency = lowLatency;
		}

		static JellyBeanOpenSLParameters getParameters() {
			boolean lowLatency = Build.MODEL.equals("Galaxy Nexus");
			return new JellyBeanOpenSLParameters(64, 64, lowLatency ? 384 : 64, lowLatency);  // 384 is the magic number for GN + JB (Android 4.1).
		}
		
		@Override
		public int suggestOutputBufferSize(int sampleRate) {
			return (sampleRate == suggestSampleRate()) ? nativeBufferSize : super.suggestOutputBufferSize(sampleRate);
		}
		
		@Override
		public boolean supportsLowLatency() {
			return lowLatency;
		}
	}

	@TargetApi(17)  // Using lazy class loading trick to hide new features from old devices.
	private static class JellyBeanMR1OpenSLParameters extends JellyBeanOpenSLParameters {
		private final int sampleRate;
		
		JellyBeanMR1OpenSLParameters(int sampleRate, int inputBufferSize, int outputBufferSize, int nativeBufferSize, boolean lowLatency) {
			super(inputBufferSize, outputBufferSize, nativeBufferSize, lowLatency);
			this.sampleRate = sampleRate;
		}
		
		@Override public int suggestSampleRate() { return sampleRate; }
		
		static JellyBeanMR1OpenSLParameters getParameters(Context context) {
			PackageManager pm = context.getPackageManager();
			boolean lowLatency = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			int sr = 44100;
			int bs = 64;
			try {
				sr = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
				bs = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
				Log.i(TAG, "sample rate: " + sr + ", buffer size: " + bs);
			} catch (Exception e) {
				Log.e(TAG, "Missing or malformed audio property: " + e.toString());
			}
			return new JellyBeanMR1OpenSLParameters(sr, 64, 64, bs, lowLatency);
		}
	}
}
