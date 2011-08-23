package com.noisepages.nettoyeur.processing.sample;

import org.puredata.android.processing.PureDataPApplet;


public class PdP5Sample extends PureDataPApplet {

	public void setup() {
		init(44100, 0, 2);
		int zipId = com.noisepages.nettoyeur.processing.sample.R.raw.patch; // Processing masks R
		pd().loadPatch(zipId, "test1.pd");
//		pd().subscribe("foo");  // Uncomment if you want to receive messages sent to the receive symbol "foo" in Pd.
	}
	
	public void draw() {
		background(0);
		fill(200, 0, 0);
		stroke(255, 0, 0);
		ellipseMode(CENTER);
		ellipse(mouseX, mouseY, 100, 100);
		pd().sendFloat("pitch", (float)mouseX / (float)width); // Send float message to symbol "pitch" in Pd.
		pd().sendFloat("volume", (float)mouseY / (float)height);
	}
	
	/*
	// Implement methods like the following if you want to receive messages from Pd.
	// You'll also need to subscribe to receive symbols you're interested if you want
	// to receive messages.
	
	public void pdPrint(String s) {
		// Handle string s, printed by Pd
	}
	
	public void receiveBang(String source) {
		// Handle bang sent to symbol source in Pd
	}
	
	public void receiveFloat(String source, float x) {
		// Handle float x sent to symbol source in Pd
	}
	
	public void receiveSymbol(String source, String sym) {
		// Handle symbol sym sent to symbol source in Pd
	}
	*/
}