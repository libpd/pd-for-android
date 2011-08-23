package org.puredata.android.processing;

import processing.core.PApplet;

public class PureDataPApplet extends PApplet {

	private PureDataP5Android pd = null;
	
	public void init(int sampleRate, int nIn, int nOut) {
		pd = new PureDataP5Android(this, sampleRate, nIn, nOut);
		pd.start();
	}
	
	public PureDataP5Android pd() {
		return pd;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (pd != null) {
			pd.stop();
		}
	}
	
	// boilerplate
	public int sketchWidth() { return this.screenWidth; }
	public int sketchHeight() { return this.screenHeight; }
	public String sketchRenderer() { return PApplet.OPENGL; }
}
