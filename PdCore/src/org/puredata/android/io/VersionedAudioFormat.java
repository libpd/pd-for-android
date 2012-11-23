/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.io;

import org.puredata.android.utils.Properties;

import android.media.AudioFormat;
import android.util.Log;

/**
 * 
 * VersionedAudioFormat uses a cute little hack to support audio formats across multiple versions of the Android API,
 * based on an idea from http://android-developers.blogspot.com/2010/07/how-to-have-your-cupcake-and-eat-it-too.html.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 */
public final class VersionedAudioFormat {

	private static final boolean hasEclair = Properties.version >= 5;
	
	private VersionedAudioFormat() {
		// do nothing
	}
	
	private static class FormatEclair {
		
		static {
			Log.i("Pd Version", "loading class for Eclair");
		}
		
		static int getInFormat(int inChannels) {
			switch (inChannels) {
			case 1: return AudioFormat.CHANNEL_IN_MONO;
			case 2: return AudioFormat.CHANNEL_IN_STEREO;
			default: throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
			}
		}

		static int getOutFormat(int outChannels) {
			switch (outChannels) {
			case 1: return AudioFormat.CHANNEL_OUT_MONO;
			case 2: return AudioFormat.CHANNEL_OUT_STEREO;
			case 4: return AudioFormat.CHANNEL_OUT_QUAD;
			case 6: return AudioFormat.CHANNEL_OUT_5POINT1;
			case 8: return AudioFormat.CHANNEL_OUT_7POINT1;
			default: throw new IllegalArgumentException("illegal number of output channels: " + outChannels);
			}
		}
	}
	
	private static class FormatCupcake {
		
		static {
			Log.i("Pd Version", "loading class for Cupcake");
		}
		
		@SuppressWarnings("deprecation")
		static int getInFormat(int inChannels) {
			switch (inChannels) {
			case 1: return AudioFormat.CHANNEL_CONFIGURATION_MONO;
			case 2: return AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			default: throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
			}
		}

		@SuppressWarnings("deprecation")
		static int getOutFormat(int outChannels) {
			switch (outChannels) {
			case 1: return AudioFormat.CHANNEL_CONFIGURATION_MONO;
			case 2: return AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			default: throw new IllegalArgumentException("illegal number of output channels: " + outChannels);
			}
		}
	}
	
	public static int getInFormat(int inChannels) {
		return hasEclair ? FormatEclair.getInFormat(inChannels) : FormatCupcake.getInFormat(inChannels); // crucial: lazy class loading
	}
	
	public static int getOutFormat(int outChannels) {
		return hasEclair ? FormatEclair.getOutFormat(outChannels) : FormatCupcake.getOutFormat(outChannels);
	}
}
