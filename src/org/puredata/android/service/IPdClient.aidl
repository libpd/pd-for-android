package org.puredata.android.service;

oneway interface IPdClient {
	void handleStart(int sampleRate, int nIn, int nOut, int ticksPerBuffer);
	void handleStop();
}