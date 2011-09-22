/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.processing;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import org.puredata.processing.PureDataP5Base;

import processing.core.PApplet;


/**
 * An implementation of Android-specific functionality for Pd and Processing.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class PureDataP5Android extends PureDataP5Base {

	private final PApplet parent;
	
	/**
	 * Constructor.
	 * 
	 * @param parent instance of PApplet
	 * @param sampleRate desired sample rate
	 * @param nIn desired number of input channels
	 * @param nOut desired number of output channels
	 */
	public PureDataP5Android(PApplet parent, int sampleRate, int nIn, int nOut) {
		super(parent);
		this.parent = parent;
		try {
			PdAudio.initAudio(sampleRate, nIn, nOut, 8, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void start() {
		PdAudio.startAudio(parent);
	}
	
	@Override
	public void stop() {
		PdAudio.stopAudio();
	}
	
	public void release() {
		stop();
		PdAudio.release();
		PdBase.release();
	}
	
	@Override
	public void dispose() {
		release();
		super.dispose();
	}
	
	/**
	 * Unpacks a zip resource and opens a patch contained in it.
	 * 
	 * (Note: This method is experimental and may disappear without
	 * warning if it turns out to be at odds with the Processing way.)
	 * 
	 * @param zipId Android id of zip resource, e.g., R.raw.patch
	 * @param patchName main patch file, e.g., "drone.pd"
	 * @return handle of patch, suitable for passing to closePatch
	 */
	public int unpackAndOpenPatch(int zipId, String patchName) {
		File dir = parent.getFilesDir();
		try {
			IoUtils.extractZipResource(parent.getResources().openRawResource(zipId), dir, true);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return openPatch(new File(dir, patchName));
	}		
}
