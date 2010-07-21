package org.puredata.android.service;

import java.io.IOException;

import org.puredata.android.io.PdAudioThread;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PdService extends Service {

	private final PdInterface.Stub binder = new PdInterface.Stub() {
		
		@Override
		public int sendSymbol(String dest, String symbol) throws RemoteException {
			return PdBase.sendSymbol(dest, symbol);
		}
		
		@Override
		public int sendFloat(String dest, float x) throws RemoteException {
			return PdBase.sendFloat(dest, x);
		}
		
		@Override
		public int sendBang(String dest) throws RemoteException {
			return PdBase.sendBang(dest);
		}
		
		@Override
		public String openPatch(String patch, String directory) throws RemoteException {
			try {
				return PdUtils.openPatch(patch, directory);
			} catch (IOException e) {
				return null;
			}
		}
		
		@Override
		public void closePatch(String patch) throws RemoteException {
			PdUtils.closePatch(patch);
		}
		
		@Override
		public int openAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws RemoteException {
			try {
				PdAudioThread.startThread(sampleRate, nIn, nOut, ticksPerBuffer, false);
				return 0;
			} catch (IOException e) {
				return -1;
			}
			
		}
		
		@Override
		public void closeAudio() throws RemoteException {
			PdAudioThread.stopThread();
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PdBase.setReceiver(new PdReceiver() {
			
			private final String TAG = "Pd Service";
			
			@Override
			public void receiveSymbol(String source, String symbol) {
				Log.i(TAG, "source: " + source + ", symbol: " + symbol);
			}
			
			@Override
			public void receiveMessage(String source, String symbol, Object[] args) {
				Log.i(TAG, "source: " + source + ", symbol: " + symbol + ", args: " + args);
			}
			
			@Override
			public void receiveList(String source, Object[] args) {
				Log.i(TAG, "source: " + source + ", args: " + args);
			}
			
			@Override
			public void receiveFloat(String source, float x) {
				Log.i(TAG, "source: " + source + ", float: " + x);
			}
			
			@Override
			public void receiveBang(String source) {
				Log.i(TAG, "source: " + source + ", bang!");
			}
			
			@Override
			public void print(String s) {
				Log.i(TAG, s);
			}
		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		PdBase.release();
	}
}
