/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.utils;

import org.puredata.core.utils.PdDispatcher;

import android.os.Handler;
import android.util.Log;

/**
 * Subclass of {@link PdDispatcher} for executing callbacks on the main UI thread
 * of an Android app.  It is actually more general than that; instances of this
 * class will execute their callbacks in whichever thread they were created in,
 * but in practice it really only makes sense to create instances of this class
 * in the main UI thread.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class PdUiDispatcher extends PdDispatcher {

	private final static String TAG = PdUiDispatcher.class.getSimpleName();
	private final Handler handler;
	private final Thread target;

	/**
	 * Constructor; invoke from the main UI thread
	 */
	public PdUiDispatcher() {
		handler = new Handler();
		target = Thread.currentThread();
	}

	@Override
	public void print(String s) {
		Log.i(TAG, "print: " + s);
	}

	@Override
	public synchronized void receiveBang(final String source) {
		if (Thread.currentThread().equals(target)) {
			PdUiDispatcher.super.receiveBang(source);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdUiDispatcher.super.receiveBang(source);
				}
			});
		}
	}

	@Override
	public synchronized void receiveFloat(final String source, final float x) {
		if (Thread.currentThread().equals(target)) {
			PdUiDispatcher.super.receiveFloat(source, x);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdUiDispatcher.super.receiveFloat(source, x);
				}
			});
		}
	}

	@Override
	public synchronized void receiveSymbol(final String source, final String symbol) {
		if (Thread.currentThread().equals(target)) {
			PdUiDispatcher.super.receiveSymbol(source, symbol);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdUiDispatcher.super.receiveSymbol(source, symbol);
				}
			});
		}
	}

	@Override
	public synchronized void receiveList(final String source, final Object... args) {
		if (Thread.currentThread().equals(target)) {
			PdUiDispatcher.super.receiveList(source, args);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdUiDispatcher.super.receiveList(source, args);
				}
			});
		}
	}

	@Override
	public synchronized void receiveMessage(final String source, final String symbol, final Object... args) {
		if (Thread.currentThread().equals(target)) {
			PdUiDispatcher.super.receiveMessage(source, symbol, args);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					PdUiDispatcher.super.receiveMessage(source, symbol, args);
				}
			});
		}
	}
}
