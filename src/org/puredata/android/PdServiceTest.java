/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * simple test case for {@link PdService}
 * 
 */

package org.puredata.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import org.puredata.android.service.IPdService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PdServiceTest extends Activity {

	private static final String PD_TEST = "Pd Test";
	private IPdService proxy = null;
	
	private String folder, filename, patch;
	private int sampleRate, nIn, nOut, ticksPerBuffer;
	
	private final IPdClient.Stub client = new IPdClient.Stub() {
		
		@Override
		public void handleStop() throws RemoteException {
			Log.i(PD_TEST, "audio stopped");
			finish();
		}
		
		@Override
		public void handleStart(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws RemoteException {
			Log.i(PD_TEST, "audio started: " + sampleRate + ", " + nIn + ", " + nOut + ", " + ticksPerBuffer);
		}
	};

	private static class Receiver extends IPdListener.Stub {

		private final String tag = PD_TEST + " Receiver";
		
		@Override
		public void receiveBang() throws RemoteException {
			Log.i(tag, "bang!");
		}

		@Override
		public void receiveFloat(float x) throws RemoteException {
			Log.i(tag, "float: " + x);
		}
		
		@Override
		public void receiveSymbol(String symbol) throws RemoteException {
			Log.i(tag, "symbol: " + symbol);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void receiveList(List args) throws RemoteException {
			Log.i(tag, "args: " + args.toString());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void receiveMessage(String symbol, List args)
				throws RemoteException {
			Log.i(tag, "symbol: " + symbol + ", args: " + args.toString());
		}
	};
	
	private Receiver recv = new Receiver();
	
	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			proxy = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			proxy = IPdService.Stub.asInterface(service);
			try {
				proxy.addClient(client);
				proxy.subscribe("android", recv);
				proxy.sendMessage("pd", "open", Arrays.asList(new Object[] {filename, folder}));
				int err = proxy.requestAudio(sampleRate, nIn, nOut, ticksPerBuffer);
				if (err != 0) {
					Log.e(PD_TEST, "unable to start audio");
					finish();
				}
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
			}
		}
	};
	
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources();
		sampleRate = res.getInteger(R.integer.sampleRate);
		nIn = res.getInteger(R.integer.inChannels);
		nOut = res.getInteger(R.integer.outChannels);
		ticksPerBuffer = res.getInteger(R.integer.ticksPerBuffer);
		try {
			InputStream in = res.openRawResource(R.raw.test);
			int n = in.available();
			byte[] buffer = new byte[n];
			in.read(buffer);
			in.close();
			Log.i(PD_TEST, "read file");
			filename = "_test.pd";
			patch = "pd-" + filename;
			FileOutputStream out = openFileOutput(filename, Context.MODE_PRIVATE);
			out.write(buffer);
			out.close();
			Log.i(PD_TEST, "wrote file");
			folder = getFilesDir().getAbsolutePath();
		} catch (IOException e) {
			Log.e(PD_TEST, e.toString());
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent("org.puredata.android.service.LAUNCH"), connection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		cleanup();
		unbindService(connection);
	}

	@SuppressWarnings("unchecked")
	private void cleanup() {
		boolean success = deleteFile(filename);
		Log.i(PD_TEST, success ? "deleted file" : "unable to delete file");
		if (proxy != null) {
			try {
				proxy.removeClient(client);
				proxy.sendMessage(patch, "menuclose", new ArrayList());
				proxy.unsubscribe("android", recv);
				proxy.releaseAudio();
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
			}
		}
	}
}
