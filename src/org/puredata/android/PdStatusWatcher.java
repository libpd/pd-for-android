/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PdStatusWatcher extends BroadcastReceiver {

	private static final String PD_STATUS_WATCHER = "Pd Status Watcher";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("org.puredata.android.service.START_AUDIO")) {
			int sr  = intent.getIntExtra("org.puredata.android.service.SAMPLE_RATE", 0);
			int nic = intent.getIntExtra("org.puredata.android.service.IN_CHANNELS", 0);
			int noc = intent.getIntExtra("org.puredata.android.service.OUT_CHANNELS", 0);
			int tpb = intent.getIntExtra("org.puredata.android.service.TICKS_PER_BUFFER", 0);
			handleStart(sr, nic, noc, tpb);
		} else if (action.equals("org.puredata.android.service.STOP_AUDIO")) {
			handleStop();
		} else {
			Log.i(PD_STATUS_WATCHER, intent.toString());
		}
	}

	private void handleStart(int sr, int nic, int noc, int tpb) {
		Log.i(PD_STATUS_WATCHER, "pd audio thread started");
		Log.i(PD_STATUS_WATCHER, "sample rate: " + sr + ", input channels: " + nic + ", output channels: " + noc + ", ticks per buffer: " + tpb);
	}

	private void handleStop() {
		Log.i(PD_STATUS_WATCHER, "pd audio thread stopped");
	}
}
