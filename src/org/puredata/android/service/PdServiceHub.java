/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * the central hub that contains the entire IPC infrastructure; everybody else communicates through
 * proxies provided by this class
 * 
 */

package org.puredata.android.service;

import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class PdServiceHub {

	private static final int START_AUDIO = 1;
	private static final int STOP_AUDIO = 2;
	private static final int SUBSCRIBE = 3;
	private static final int UNSUBSCRIBE = 4;
	private static final int SEND_MESSAGE = 5;
	
	private static final int BANG = 1;
	private static final int FLOAT = 2;
	private static final int SYMBOL = 3;
	private static final int LIST = 4;
	private static final int ANY = 5;

	private static final String VAL = "value";
	private static final String SYM = "symbol";
	private static final String FORMAT = "format";

	private static final String SRATE = "sr";
	private static final String CHANNELS_IN = "nIn";
	private static final String CHANNELS_OUT = "nOut";
	private static final String RESTART = "restart";
	private static final String TICKS_PER_BUFFER = "tpb";
	
	public static class ServiceProxy {
		private final Messenger sender;
		
		public ServiceProxy(Messenger sender) {
			this.sender = sender;
		}
		
		public void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer, boolean restart) throws RemoteException {
			sender.send(startAudioMessage(sampleRate, nIn, nOut, ticksPerBuffer, restart));
		}
		
		public void stopAudio() throws RemoteException {
			sender.send(stopAudioMessage());
		}

		public void subscribe(String symbol, final PdReceiver receiver) throws RemoteException {
			Message message = Message.obtain(null, PdServiceHub.SUBSCRIBE);
			message.obj = symbol;
			message.replyTo = new Messenger(new Handler() {
				@Override
				public void handleMessage(android.os.Message msg) {
					PdServiceHub.evaluateMessage(msg, receiver);
				};
			});
			sender.send(message);
		}
		
		public void sendBang(String dest) throws RemoteException {
			sender.send(bangMessage(dest));
		}
		
		public void sendFloat(String dest, float x) throws RemoteException {
			sender.send(floatMessage(dest, x));
		}
		
		public void sendSymbol(String dest, String symbol) throws RemoteException {
			sender.send(symbolMessage(dest, symbol));
		}
		
		public void sendList(String dest, Object... args) throws RemoteException {
			sender.send(listMessage(dest, args));
		}
		
		public void sendAny(String dest, String symbol, Object... args) throws RemoteException {
			sender.send(anyMessage(dest, symbol, args));
		}
	}
	
	static class ServiceHandler extends Handler {
		
		private final PdService service;
		private final PdReceiver receiver;
		
		public ServiceHandler(PdService service, PdReceiver receiver) {
			this.service = service;
			this.receiver = receiver;
		}
		 
		@Override
		public void handleMessage(android.os.Message message) {
			switch (message.what) {
			case START_AUDIO:
				Bundle b = message.getData();
				int sr = b.getInt(SRATE);
				int nIn = b.getInt(CHANNELS_IN);
				int nOut = b.getInt(CHANNELS_OUT);
				int tpb = b.getInt(TICKS_PER_BUFFER);
				boolean restart = b.getBoolean(RESTART);
				service.startAudio(sr, nIn, nOut, tpb, restart);
				break;
			case STOP_AUDIO:
				service.stopAudio();
				break;
			case SUBSCRIBE:
				String symbol = (String) message.obj;
				Messenger messenger = message.replyTo;
				service.addSubscription(symbol, messenger);
				break;
			case UNSUBSCRIBE:
				// TODO: implement this
				break;
			case SEND_MESSAGE:
				evaluateMessage(message, receiver);
				break;
			default:
				Log.w("Pd Service Handler", "unknown message: " + message.toString());
				break;
			}
		};
	}
	
	static class ClientLink implements PdListener {

		private final String source;
		private final Messenger receiver;

		ClientLink(String symbol, Messenger receiver) {
			this.source = symbol;
			this.receiver = receiver;
		}

		private void complain() {
			Log.e("Pd Client Link", "unresponsive receiver: " + receiver + ", symbol " + source);
		}

		@Override
		public void receiveBang() {
			try {
				receiver.send(PdServiceHub.bangMessage(source));
			} catch (RemoteException e) {
				complain();
			}
		}
		
		@Override
		public void receiveFloat(float x) {
			try {
				receiver.send(PdServiceHub.floatMessage(source, x));
			} catch (RemoteException e) {
				complain();
			}
		}

		@Override
		public void receiveList(Object[] args) {
			try {
				receiver.send(PdServiceHub.listMessage(source, args));
			} catch (RemoteException e) {
				complain();
			}
		}

		@Override
		public void receiveMessage(String symbol, Object[] args) {
			try {
				receiver.send(PdServiceHub.anyMessage(this.source, symbol, args));
			} catch (RemoteException e) {
				complain();
			}
		}

		@Override
		public void receiveSymbol(String symbol) {
			try {
				receiver.send(PdServiceHub.symbolMessage(source, symbol));
			} catch (RemoteException e) {
				complain();
			}
		}
	}
	
	private static Message startAudioMessage(int sampleRate, int nIn, int nOut,
			int ticksPerBuffer, boolean restart) {
		Message msg = Message.obtain(null, START_AUDIO);
		Bundle b = new Bundle(5);
		b.putInt(SRATE, sampleRate);
		b.putInt(CHANNELS_IN, nIn);
		b.putInt(CHANNELS_OUT, nOut);
		b.putInt(TICKS_PER_BUFFER, ticksPerBuffer);
		b.putBoolean(RESTART, restart);
		msg.setData(b);
		return msg;
	}
	
	private static Message stopAudioMessage() {
		return Message.obtain(null, STOP_AUDIO);
	}
	
	private static Message bangMessage(String source) {
		Message msg = Message.obtain(null, SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = BANG;
		msg.setData(null);
		return msg;
	}

	private static Message floatMessage(String source, float x) {
		Message msg = Message.obtain(null, SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = FLOAT;
		Bundle b = new Bundle(1);
		b.putFloat(VAL, x);
		msg.setData(b);
		return msg;
	}

	private static Message symbolMessage(String source, String symbol) {
		Message msg = Message.obtain(null, SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = SYMBOL;
		Bundle b = new Bundle(1);
		b.putString(VAL, symbol);
		msg.setData(b);
		return msg;
	}

	private static Message listMessage(String source, Object... args) {
		Message msg = Message.obtain(null, SEND_MESSAGE);
		msg.obj = source;
		msg.arg1 = LIST;
		Bundle b = new Bundle(args.length+1);
		addArgs(b, args);
		msg.setData(b);
		return msg;
	}

	private static Message anyMessage(String source, String symbol, Object... args) {
		Message msg = Message.obtain(null, SEND_MESSAGE);
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

	private static void evaluateMessage(Message message, PdReceiver receiver) {
		if (message.what != SEND_MESSAGE) return;
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
