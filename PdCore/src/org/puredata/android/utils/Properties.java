/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.utils;

import android.os.Build;

/**
 *
 * Properties is a utility class that checks whether armeabi-v7a is available.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public class Properties {

	/**
	 * Android version as an integer (e.g., 8 for FroYo)
	 */
	public static final int version = Integer.parseInt(Build.VERSION.SDK);
	
	/**
	 * true if and only if armeabi-v7a is available
	 */
	public static final boolean hasArmeabiV7a = version >= 4 && VersionedAbiCheck.hasV7a();
	
	// use lazy class loading to hide Build.CPU_ABI from Cupcake
	private static class VersionedAbiCheck {
		private static boolean hasV7a() {
			return "armeabi-v7a".equals(Build.CPU_ABI);
		}
	}
}
