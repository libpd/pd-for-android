package org.puredata.android.service;

import java.util.List;

oneway interface IPdListener {
	void receiveBang();
	void receiveFloat(float x);
	void receiveSymbol(in String symbol);
	void receiveList(in List args);
	void receiveMessage(in String symbol, in List args);
}