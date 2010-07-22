package org.puredata.android.service;

import org.puredata.android.service.IPdListener;
import java.util.List;
	
interface IPdService {
	int requestAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer);
	void releaseAudio();
	boolean isRunning();
	boolean objectExists(String symbol);
	void subscribe(in String symbol, in IPdListener client);
	void unsubscribe(in String symbol, in IPdListener client);
	void sendBang(in String dest);
	void sendFloat(in String dest, float x);
	void sendSymbol(in String dest, in String symbol);
	void sendList(in String dest, in List args);
	void sendMessage(in String dest, in String symbol, in List args);
}
	