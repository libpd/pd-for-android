/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * Interface for printing and receiving messages from pd to be used with setReceiver in {@link PdBase}
 *
 */

package org.puredata.core;



public interface PdReceiver {
	
	public void print(String s);
	
	public void receiveBang(String source);
	
	public void receiveFloat(String source, float x);
	
	public void receiveSymbol(String source, String symbol);
	
	/**
	 * 
	 * @param source
	 * @param args  elements may be of type Integer, Float, or String
	 */
	public void receiveList(String source, Object[] args);
	
	/**
	 * 
	 * @param source
	 * @param symbol
	 * @param args  elements may be of type Integer, Float, or String
	 */
	public void receiveMessage(String source, String symbol, Object[] args);
	
}
