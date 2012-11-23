/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.fifths;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.Toast;


public class CircleOfFifths extends Activity implements OnClickListener {

	private static final String TAG = "Pd Circle Of Fifths";
	private static final String TOP = "top";
	private static final int MIN_SAMPLE_RATE = 44100;
	private RadioGroup options;
	private int option = 0;

	private Toast toast = null;
	
	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
				toast.setText(TAG + ": " + msg);
				toast.show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		try {
			initPd();
		} catch (IOException e) {
			toast(e.toString());
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		PdAudio.startAudio(this);
	}
	
	@Override
	protected void onStop() {
		PdAudio.stopAudio();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	private void initGui() {
		setContentView(R.layout.main);
		CircleView circle = (CircleView) findViewById(R.id.circleview);
		circle.setOwner(this);
		int top = getPreferences(MODE_PRIVATE).getInt(TOP, 0);
		circle.setTopSegment(top);
		options = (RadioGroup) findViewById(R.id.options);
		findViewById(R.id.domdim).setOnClickListener(this);
		findViewById(R.id.majmin).setOnClickListener(this);
		findViewById(R.id.sixth).setOnClickListener(this);
		findViewById(R.id.susp).setOnClickListener(this);
	}

	private void initPd() throws IOException {
		AudioParameters.init(this);
		int srate = Math.max(MIN_SAMPLE_RATE, AudioParameters.suggestSampleRate());
		PdAudio.initAudio(srate, 0, 2, 1, true);
		
		File dir = getFilesDir();
		File patchFile = new File(dir, "chords.pd");
		IoUtils.extractZipResource(getResources().openRawResource(R.raw.patch), dir, true);
		PdBase.openPatch(patchFile.getAbsolutePath());
	}

	private void cleanup() {
		// make sure to release all resources
		PdAudio.release();
		PdBase.release();
	}

	public void playChord(boolean major, int n) {
		PdBase.sendList("playchord", option + (major ? 1 : 0), n);
	}
	
	public void endChord() {
		PdBase.sendBang("endchord");
		resetOptions();
	}

	public void setTop(int top) {
		PdBase.sendFloat("shift", top);
		getPreferences(MODE_PRIVATE).edit().putInt(TOP, top).commit();
	}

	@Override
	public void onClick(View v) {
		int newOption;
		switch (v.getId()) {
		case R.id.domdim:
			newOption = 2;
			break;
		case R.id.majmin:
			newOption = 4;
			break;
		case R.id.sixth:
			newOption = 6;
			break;
		case R.id.susp:
			newOption = 8;
			break;
		default:
			newOption = 0;
			break;
		}
		if (option == newOption) {
			resetOptions();
		} else {
			option = newOption;
		}
	}

	private void resetOptions() {
		option = 0;
		options.clearCheck();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.circle_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		switch (item.getItemId()) {
		case R.id.about_item:
			ad.setTitle(R.string.about_title);
			ad.setMessage(R.string.about_msg);
			break;
		case R.id.help_item:
			ad.setTitle(R.string.help_title);
			ad.setMessage(R.string.help_msg);
			break;
		default:
			break;
		}
		ad.setNeutralButton(android.R.string.ok, null);
		ad.setCancelable(true);
		ad.show();
		return true;
	}
}
