/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * Some convenience methods for interacting with pd
 * 
 */

package org.puredata.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.puredata.core.PdBase;

public class PdUtils {
	
	/**
	 * same as "compute audio" checkbox in pd gui
	 * 
	 * @param flag
	 */
	public static void computeAudio(boolean flag) {
		PdBase.sendMessage("pd", "dsp", flag ? 1 : 0);
	}
	
	/**
	 * reads a patch from a file
	 * 
	 * @param filename
	 * @param directory
	 * @return  pd symbol representing patch
	 * @throws IOException in case patch fails to open
	 */
	public static String openPatch(String filename, String directory) throws IOException {
		File file = new File(directory, filename);
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		int err = PdBase.sendMessage("pd", "open", filename, directory);
		if (err != 0) {
			throw new IOException("unable to open patch, error code " + err);
		}
		String patch = "pd-" + filename;
		if (!PdBase.exists(patch)) {
			throw new IOException("patch " + file.getPath() + " didn't open, no idea why");
		}
		return patch;
	}
	
	/**
	 * closes a patch
	 * 
	 * @param patch name of patch, as returned by openPatch
	 */
	public static void closePatch(String patch) {
		PdBase.sendMessage(patch, "menuclose");
	}	
}
