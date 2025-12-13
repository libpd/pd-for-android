/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.android.io;

import android.content.Context;
import android.app.Activity;
import android.media.AudioManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.util.Log;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class AudioDevices {

	private static final String TAG = "AudioDevices";
	private AudioManager mAudioManager;

	private class AudioDeviceList {
		private ListPreference mPref;
		private List<String> mEntries = new ArrayList<>();
		private List<String> mValues = new ArrayList<>();

		private CharSequence[] listToArray(List<String> l) {
			String[] array = new String[l.size()];
			l.toArray(array);
			return array;
		}
		AudioDeviceList(PreferenceFragment prefFragment, String key) {
			mPref = (ListPreference) prefFragment.findPreference(key);
			mEntries.add("Default");
			mValues.add("-1");
		}
		public void add(AudioDeviceInfo device) {
			String name = device.getProductName().toString() + " " + typeToString(device.getType());
			mEntries.add(name);
			mValues.add(Integer.toString(device.getId()));
			mPref.setEntries(listToArray(mEntries));
			mPref.setEntryValues(listToArray(mValues));
		}
	}

	private AudioDeviceList mInputDevices = null;
	private AudioDeviceList mOutputDevices = null;

	/**
	 * @param context activity or service that calls this method
	 */
	public AudioDevices(Activity context) {
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		PreferenceFragment prefFragment = (PreferenceFragment) context.getFragmentManager().findFragmentByTag("prefFragment");
		Resources res = context.getResources();
		final String inputKey = res.getString(org.puredata.android.service.R.string.pref_key_indevice);
		mInputDevices = new AudioDeviceList(prefFragment, inputKey);
		final String outputKey = res.getString(org.puredata.android.service.R.string.pref_key_outdevice);
		mOutputDevices = new AudioDeviceList(prefFragment, outputKey);
		setupAudioDeviceCallback();
	}

	/**
	 * Converts the value from {@link AudioDeviceInfo#getType()} into a human
	 * readable string
	 * @param type One of the {@link AudioDeviceInfo}.TYPE_* values
	 *             e.g. AudioDeviceInfo.TYPE_BUILT_IN_SPEAKER
	 * @return string which describes the type of audio device
	 */
	static String typeToString(int type){
		switch (type) {
			case AudioDeviceInfo.TYPE_AUX_LINE:
				return "auxiliary line-level connectors";
			case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
				return "Bluetooth device supporting the A2DP profile";
			case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
				return "Bluetooth device typically used for telephony";
			case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
				return "built-in earphone speaker";
			case AudioDeviceInfo.TYPE_BUILTIN_MIC:
				return "built-in microphone";
			case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
				return "built-in speaker";
			case AudioDeviceInfo.TYPE_BUS:
				return "BUS";
			case AudioDeviceInfo.TYPE_DOCK:
				return "DOCK";
			case AudioDeviceInfo.TYPE_FM:
				return "FM";
			case AudioDeviceInfo.TYPE_FM_TUNER:
				return "FM tuner";
			case AudioDeviceInfo.TYPE_HDMI:
				return "HDMI";
			case AudioDeviceInfo.TYPE_HDMI_ARC:
				return "HDMI audio return channel";
			case AudioDeviceInfo.TYPE_IP:
				return "IP";
			case AudioDeviceInfo.TYPE_LINE_ANALOG:
				return "line analog";
			case AudioDeviceInfo.TYPE_LINE_DIGITAL:
				return "line digital";
			case AudioDeviceInfo.TYPE_TELEPHONY:
				return "telephony";
			case AudioDeviceInfo.TYPE_TV_TUNER:
				return "TV tuner";
			case AudioDeviceInfo.TYPE_USB_ACCESSORY:
				return "USB accessory";
			case AudioDeviceInfo.TYPE_USB_DEVICE:
				return "USB device";
			case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
				return "wired headphones";
			case AudioDeviceInfo.TYPE_WIRED_HEADSET:
				return "wired headset";
			default:
			case AudioDeviceInfo.TYPE_UNKNOWN:
				return "unknown";
		}
	}

	private void setupAudioDeviceCallback(){
		// Note that we will immediately receive a call to onDevicesAdded with the list of
		// devices which are currently connected.
		mAudioManager.registerAudioDeviceCallback(new AudioDeviceCallback() {
			@Override
			public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
				for (AudioDeviceInfo device : addedDevices){
					if (device.isSource()) mInputDevices.add(device);
					else if (device.isSink()) mOutputDevices.add(device);
				}
			}

			public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
			}
		}, null);
	}
}
