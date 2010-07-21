/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * a crude utility for translating pd messages into Android messages and vice versa
 * 
 */

package org.puredata.android.service;

import org.puredata.core.PdReceiver;

import android.os.Bundle;
import android.os.Message;

public class PdMessage {

	private static final int BANG = 1;
	private static final int FLOAT = 2;
	private static final int SYMBOL = 3;
	private static final int LIST = 4;
	private static final int ANY = 5;

	private static final String VAL = "value";
	private static final String SYM = "symbol";
	private static final String FORMAT = "format";

	public static Message bangMessage(String source) {
		Message msg = Message.obtain(null, PdService.SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = BANG;
		msg.setData(null);
		return msg;
	}

	public static Message floatMessage(String source, float x) {
		Message msg = Message.obtain(null, PdService.SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = FLOAT;
		Bundle b = new Bundle(1);
		b.putFloat(VAL, x);
		msg.setData(b);
		return msg;
	}

	public static Message symbolMessage(String source, String symbol) {
		Message msg = Message.obtain(null, PdService.SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = SYMBOL;
		Bundle b = new Bundle(1);
		b.putString(VAL, symbol);
		msg.setData(b);
		return msg;
	}

	public static Message listMessage(String source, Object... args) {
		Message msg = Message.obtain(null, PdService.SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = LIST;
		Bundle b = new Bundle(args.length+1);
		addArgs(b, args);
		msg.setData(b);
		return msg;
	}

	public static Message anyMessage(String source, String symbol, Object... args) {
		Message msg = Message.obtain(null, PdService.SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = ANY;
		Bundle b = new Bundle(args.length+2);
		b.putString(SYM, symbol);
		addArgs(b, args);
		msg.setData(b);
		return msg;
	}

	private static void addArgs(Bundle b, Object[] args) {
		String format = "";
		for (int i = 0; i < args.length; i++) {
			String key = "" + i;
			if (args[i] instanceof Integer) {
				format += 'f';
				b.putFloat(key, (Integer) args[i]);
			} else if (args[i] instanceof Float) {
				format += 'f';
				b.putFloat(key, (Float) args[i]);
			} else if (args[i] instanceof String) {
				format += 's';
				b.putString(key, (String) args[i]);
			}
		}
		b.putString(FORMAT, format);
	}

	private static Object[] getArgs(Bundle b) {
		String format = b.getString(FORMAT);
		Object[] args = new Object[format.length()];
		for (int i = 0; i < format.length(); i++) {
			String key = "" + i;
			args[i] = (format.charAt(i) == 'f') ? b.getFloat(key) : b.getString(key);
		}
		return args;
	}

	public static void evaluateMessage(Message message, PdReceiver receiver) {
		if (message.what != PdService.SEND_MESSAGE) return;
		String source = (String) message.obj;
		Bundle b = message.getData();
		switch (message.arg1) {
		case BANG:
			receiver.receiveBang(source);
			break;
		case FLOAT:
			receiver.receiveFloat(source, b.getFloat(VAL));
			break;
		case SYMBOL:
			receiver.receiveSymbol(source, b.getString(VAL));
			break;
		case LIST:
			receiver.receiveList(source, getArgs(b));
			break;
		case ANY:
			String symbol = b.getString(SYM);
			receiver.receiveMessage(source, symbol, getArgs(b));
			break;
		default:
			break;
		}
	}
}
