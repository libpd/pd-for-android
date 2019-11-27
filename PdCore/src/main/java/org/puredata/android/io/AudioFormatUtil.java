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
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 */
public class AudioFormatUtil {

	private AudioFormatUtil() {
		// do nothing
	}
	
	public static int getInFormat(int inChannels) {
		switch (inChannels) {
			case 1: return AudioFormat.CHANNEL_IN_MONO;
			case 2: return AudioFormat.CHANNEL_IN_STEREO;
			default: throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
		}
	}
	
	public static int getOutFormat(int outChannels) {
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
