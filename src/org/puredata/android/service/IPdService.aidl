/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * interface for client interaction with the pd service
 *
 */

package org.puredata.android.service;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import java.util.List;
	
interface IPdService {

	/**
	 * subscribe to updates on audio status
	 */
	void addClient(IPdClient client);
	
	/**
	 * cancel subscription
	 */
	void removeClient(IPdClient client);
	
	/**
	 * request audio with the given parameters; starts or restarts the audio thread if necessary
	 */
	int requestAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer);
	
	/**
	 * indicates that this client no longer needs the audio thread; stops the audio thread if no clients are left
	 */
	void releaseAudio();
	
	/**
	 * stops the audio thread regardless of the number of current clients
	 */
	void stop();
	
	/**
	 * indicates whether the audio thread is running
	 */
	boolean isRunning();
	
	/**
	 * checks whether an object exists in pd
	 */
	boolean objectExists(String symbol);
	
	/**
	 * subscribe to messages sent to a symbol in pd
	 */
	void subscribe(in String symbol, in IPdListener client);
	
	/**
	 * unsubscribe from messages sent to a symbol in pd
	 */
	void unsubscribe(in String symbol, in IPdListener client);
	
	void sendBang(in String dest);
	void sendFloat(in String dest, float x);
	void sendSymbol(in String dest, in String symbol);
	void sendList(in String dest, in List args);
	void sendMessage(in String dest, in String symbol, in List args);
}
	