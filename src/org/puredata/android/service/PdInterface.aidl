package org.puredata.android.service;

interface PdInterface {
	int openAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer);
	void closeAudio();
	String openPatch(in String patch, in String directory);
	void closePatch(in String patch);
	int sendBang(in String dest);
	int sendFloat(in String dest, float x);
	int sendSymbol(in String dest, in String symbol);
}