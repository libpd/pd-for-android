package org.puredata.android.service;

import org.puredata.android.service.IPdClient;
import java.util.List;
	
oneway interface IPdService {
	void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer, boolean restart);
	void stopAudio();
	void subscribe(in String symbol, in IPdClient client);
	void unsubscribe(in String symbol, in IPdClient client);
	void sendBang(in String dest);
	void sendFloat(in String dest, float x);
	void sendSymbol(in String dest, in String symbol);
	void sendList(in String dest, in List args);
	void sendMessage(in String dest, in String symbol, in List args);
}
	