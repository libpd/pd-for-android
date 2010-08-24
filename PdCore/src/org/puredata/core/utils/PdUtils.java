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
	 * @param path to patch
	 * @return pd symbol representing patch
	 * @throws IOException in case patch fails to open
	 */
	public static String openPatch(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}
		String folder = file.getParentFile().getAbsolutePath();
		String filename = file.getName();
		String patch = "pd-" + filename;
		if (PdBase.exists(patch)) {
			throw new IOException("patch is already open; close first, then reload");
		}
		PdBase.sendMessage("pd", "open", filename, folder);
		if (!PdBase.exists(patch)) {
			throw new IOException("patch " + path + " failed to open, no idea why");
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
