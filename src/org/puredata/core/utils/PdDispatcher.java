/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * Implementation of {@link PdReceiver} that dispatches messages from pd to instances of {@link PdListener}
 * based on the pd symbol they originate from.  Instances of this class automatically handle subscriptions
 * to pd symbols.
 * 
 */

package org.puredata.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;


public abstract class PdDispatcher implements PdReceiver {

	private final Map<String, Set<PdListener>> listeners = new HashMap<String, Set<PdListener>>();

	public synchronized void addListener(String symbol, PdListener listener) {
		int err = PdBase.subscribe(symbol);
		if (err != 0) {
			throw new IllegalArgumentException("bad symbol: " + symbol);
		}
		Set<PdListener> selected = listeners.get(symbol);
		if (selected == null) {
			selected = new HashSet<PdListener>();
			listeners.put(symbol, selected);
		}
		selected.add(listener);
	}
	
	public synchronized void removeListener(String symbol, PdListener listener) {
		Set<PdListener> selected = listeners.get(symbol);
		if (selected == null) return;
		selected.remove(listener);
		if (selected.isEmpty()) {
			PdBase.unsubscribe(symbol);
			listeners.remove(symbol);
		}
	}
	
	public synchronized void release() {
		for (String symbol: listeners.keySet()) {
			PdBase.unsubscribe(symbol);
		}
		listeners.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		release();
	}
	
	@Override
	public abstract void print(String s);

	@Override
	public synchronized void receiveBang(String source) {
		Set<PdListener> selected = listeners.get(source);
		if (selected != null) {
			for (PdListener listener: selected) {
				listener.receiveBang();
			}
		}
	}

	@Override
	public synchronized void receiveFloat(String source, float x) {
		Set<PdListener> selected = listeners.get(source);
		if (selected != null) {
			for (PdListener listener: selected) {
				listener.receiveFloat(x);
			}
		}
	}
	
	@Override
	public synchronized void receiveSymbol(String source, String symbol) {
		Set<PdListener> selected = listeners.get(source);
		if (selected != null) {
			for (PdListener listener: selected) {
				listener.receiveSymbol(symbol);
			}
		}
	}

	@Override
	public synchronized void receiveList(String source, Object[] args) {
		Set<PdListener> selected = listeners.get(source);
		if (selected != null) {
			for (PdListener listener: selected) {
				listener.receiveList(args);
			}
		}
	}

	@Override
	public synchronized void receiveMessage(String source, String symbol, Object[] args) {
		Set<PdListener> selected = listeners.get(source);
		if (selected != null) {
			for (PdListener listener: selected) {
				listener.receiveMessage(symbol, args);
			}
		}
	}
}
