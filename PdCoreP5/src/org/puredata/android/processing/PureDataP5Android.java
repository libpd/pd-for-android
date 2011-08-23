package org.puredata.android.processing;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.PdAudio;
import org.puredata.core.utils.IoUtils;
import org.puredata.processing.PureDataP5Base;

import processing.core.PApplet;

public class PureDataP5Android extends PureDataP5Base {

	private final PApplet parent;
	
	public PureDataP5Android(PApplet parent, int sampleRate, int nIn, int nOut) {
		super(parent);
		this.parent = parent;
		parent.registerDispose(this);
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
	
	public int loadPatch(int zipId, String patchName) {
		File dir = parent.getFilesDir();
		try {
			IoUtils.extractZipResource(parent.getResources().openRawResource(zipId), dir, true);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return openPatch(new File(dir, patchName));
	}		
}
