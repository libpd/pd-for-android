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
	@SuppressWarnings("deprecation")
	public static final int version = Integer.parseInt(Build.VERSION.SDK);
	
}
