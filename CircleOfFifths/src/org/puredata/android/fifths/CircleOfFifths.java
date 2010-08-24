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

import org.puredata.android.io.PdAudio;
import org.puredata.android.ioutils.IoUtils;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class CircleOfFifths extends Activity implements OnCheckedChangeListener {

	private static final String PD_CIRCLE = "Pd Circle Of Fifths";
	private String patch;
	private RadioGroup options;
	private int option = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		initPd();
	}

	@Override
	protected void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		cleanup();
		super.finish();
	}

	private void initGui() {
		setContentView(R.layout.main);
		CircleView circle = (CircleView) findViewById(R.id.circleview);
		circle.setOwner(this);
		options = (RadioGroup) findViewById(R.id.options);
		options.setOnCheckedChangeListener(this);
	}

	private void initPd() {
		File dir = getFilesDir();
		File patchFile = new File(dir, "chords.pd");
		try {
			IoUtils.extractZipResource(getResources().openRawResource(R.raw.patch), dir, true);
			patch = PdUtils.openPatch(patchFile.getAbsolutePath());
			PdAudio.startAudio(this, 44100, 0, 2, 32, true);
		} catch (IOException e) {
			Log.e(PD_CIRCLE, e.toString() + "; exiting now");
			finish();
		}
	}

	private void cleanup() {
		// make sure to release all resources
		PdAudio.stopAudio();
		PdUtils.closePatch(patch);
		PdBase.release();
	}

	public void playChord(boolean major, int n) {
		PdBase.sendList("playchord", option + (major ? 1 : 0), n);
		option = 0;
		options.clearCheck();
	}

	public void shift(int d) {
		PdBase.sendFloat("shift", d);
	}

	public void endChord() {
		PdBase.sendBang("endchord");
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.domdim:
			option = 2;
			break;
		case R.id.majmin:
			option = 4;
			break;
		case R.id.sixth:
			option = 6;
			break;
		case R.id.susp:
			option = 8;
			break;
		default:
			option = 0;
			break;
		}
	}
}