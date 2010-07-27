/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 * interface for reporting status changes from the pd service to its clients
 *
 */

package org.puredata.android.service;

oneway interface IPdClient {
	
	/**
	 * announce a (re)start of the audio thread to client
	 */
	void handleStart(int sampleRate, int nIn, int nOut, int ticksPerBuffer);
	
	/**
	 * announce a stop of the audio thread to client
	 */
	void handleStop();
	
	/**
	 * print from pd
	 */
	void print(String s);
}