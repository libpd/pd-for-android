/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * interface for sending pd events to clients
 *
 */

package org.puredata.android.service;

import java.util.List;

oneway interface IPdListener {
	void receiveBang();
	void receiveFloat(float x);
	void receiveSymbol(in String symbol);
	void receiveList(in List args);
	void receiveMessage(in String symbol, in List args);
}