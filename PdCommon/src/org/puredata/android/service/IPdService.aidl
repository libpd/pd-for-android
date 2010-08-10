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
	 * adds an entry to the search path for pd externals
	 */
	void addToSearchPath(in String s);
	
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
	 *
	 * returns an error code, 0 on success
	 */
	int requestAudio(int sampleRate, int inputChannels, int outputChannels, float bufferSizeMillis);
	
	/**
	 * indicates that this client no longer needs the audio thread; stops the audio thread if no clients are left
	 */
	void releaseAudio();
	
	/**
	 * attempt to stop pd service; success depends on cooperation of all clients bound to the service
	 */
	void stop();
	
	/**
	 * indicates whether the audio thread is running
	 */
	boolean isRunning();
	
	/**
	 * unpack a zip file belonging to the client in the cache directory of the service, by invoking
	 * openAssetFileDescriptor on a content provider in the client; it's a bit convoluted, but that's
	 * what it takes to get around security constraints on Android.  See ScenePlayer sample for an
	 * example of how to use this.
	 */
	int installExternals(in String uri);
	
	int getSampleRate();
	int getInputChannels();
	int getOutputChannels();
	float getBufferSizeMillis();
	
	/**
	 * checks whether a symbol refers to something in pd
	 */
	boolean exists(String symbol);
	
	/**
	 * subscribe to messages sent to a symbol in pd
	 */
	void subscribe(in String symbol, in IPdListener listener);
	
	/**
	 * unsubscribe from messages sent to a symbol in pd
	 */
	void unsubscribe(in String symbol, in IPdListener listener);
	
	void sendBang(in String dest);
	void sendFloat(in String dest, float x);
	void sendSymbol(in String dest, in String symbol);
	void sendList(in String dest, in List args);
	void sendMessage(in String dest, in String symbol, in List args);
}
	