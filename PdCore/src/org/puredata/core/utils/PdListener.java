/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * Interface and adapter class for handling dispatches from {@link PdDispatcher}
 * 
 */

package org.puredata.core.utils;


public interface PdListener {
	
	public void receiveBang();
	
	public void receiveFloat(float x);
	
	public void receiveSymbol(String symbol);
	
	/**
	 * 
	 * @param source
	 * @param args  elements may be of type Integer, Float, or String
	 */
	public void receiveList(Object... args);
	
	/**
	 * 
	 * @param source
	 * @param symbol
	 * @param args  elements may be of type Integer, Float, or String
	 */
	public void receiveMessage(String symbol, Object... args);
	
	
	public class Adapter implements PdListener {
		@Override
		public void receiveBang() {}
		@Override
		public void receiveFloat(float x) {}
		@Override
		public void receiveSymbol(String symbol) {}
		@Override
		public void receiveList(Object... args) {}
		@Override
		public void receiveMessage(String symbol, Object... args) {}
	}
}
