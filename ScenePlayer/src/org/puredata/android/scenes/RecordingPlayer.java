package org.puredata.android.scenes;

import java.io.File;
import java.util.concurrent.Semaphore;

import org.puredata.android.scenes.SceneDataBase.RecordingColumn;
import org.puredata.android.scenes.SceneDataBase.SceneColumn;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This Activity provides the primary playback interface for scene recordings.
 * 
 * @author Martin Roth (mhroth@rjdj.me)
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 */
public class RecordingPlayer extends Activity {

	private SceneDataBase db;
	private long recordingId;
	private long sceneId;
	private MediaPlayer mediaPlayer;
	private SeekbarUpdateRunnable seekbarUpdateRunnable;
	private volatile boolean mediaPlayerWasPlayingBeforeTouchSeek;

	private static final String SEEKBAR_UPDATE_THREAD_NAME = "Seekbar Update Thread";

	/**
	 * Ensures that the seekbar update thread does not attempt to update the
	 * seekbar position while the seekbar is being manipulated by the user.
	 */
	private Semaphore seekbarUpdateSemaphore;
	private static final long SEEKBAR_UPDATE_INTERVAL_MS = 50;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.recording_player);
		db = new SceneDataBase(this);
		recordingId = this.getIntent().getLongExtra(
				RecordingColumn.ID.getLabel(), -1);
		if (recordingId >= 0) {
		} else {
			finish();
		}
		String durationFormat = this.getString(R.string.duration_format);
		String dateFormat = this.getString(R.string.date_format);
		Cursor recCursor = db.getRecording(recordingId);
		sceneId = SceneDataBase.getLong(recCursor, RecordingColumn.SCENE_ID);
		Cursor sceneCursor = db.getScene(sceneId);
		TextView titleView = (TextView) findViewById(R.id.recording_play_title);
		titleView.setText(SceneDataBase.getString(sceneCursor,
				SceneColumn.SCENE_TITLE));
		TextView artistView = (TextView) findViewById(R.id.recording_play_artist);
		artistView.setText(SceneDataBase.getString(sceneCursor,
				SceneColumn.SCENE_ARTIST));
		seekbarUpdateSemaphore = new Semaphore(1); // only one permit available
													// (binary exclusion)
		final SeekBar seekBar = (SeekBar) findViewById(R.id.recording_play_seekbar);
		seekBar.setMax((int) SceneDataBase.getLong(recCursor,
				RecordingColumn.RECORDING_DURATION));
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					mediaPlayer.seekTo(progress);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				try {
					seekbarUpdateSemaphore.acquire();
					mediaPlayerWasPlayingBeforeTouchSeek = mediaPlayer
							.isPlaying();
					mediaPlayer.pause();
				} catch (InterruptedException ie) {
					Log.e(RecordingPlayer.class.getSimpleName(), "", ie);
				}
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mediaPlayerWasPlayingBeforeTouchSeek) {
					mediaPlayer.start();
				}
				seekbarUpdateSemaphore.release();
			}
		});
		seekbarUpdateRunnable = new SeekbarUpdateRunnable(seekBar);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setLooping(true); // recording will loop endlessly
		String recPath = SceneDataBase.getString(recCursor, RecordingColumn.RECORDING_PATH);
		try {
			mediaPlayer.setDataSource(recPath);
			mediaPlayer.prepare();
		} catch (Exception e) {
			// TODO: Handle this...
		}
		String scenePath = SceneDataBase.getString(sceneCursor,
				SceneColumn.SCENE_DIRECTORY);
		ImageView imageView = (ImageView) findViewById(R.id.recording_play_image);
		imageView.setImageBitmap(BitmapFactory.decodeFile(new File(scenePath,
				"image.jpg").getAbsolutePath()));
		TextView descriptionText = (TextView) findViewById(R.id.recording_play_description_text);
		descriptionText.setText(SceneDataBase.getString(recCursor,
				RecordingColumn.RECORDING_DESCRIPTION));
		TextView durationText = (TextView) findViewById(R.id.recording_play_duration_text);
		durationText.setText(DateFormat.format(durationFormat, SceneDataBase
				.getLong(recCursor, RecordingColumn.RECORDING_DURATION)));
		TextView dateText = (TextView) findViewById(R.id.recording_play_date_text);
		dateText.setText(DateFormat.format(dateFormat, SceneDataBase.getLong(
				recCursor, RecordingColumn.RECORDING_TIMESTAMP)));
		final ToggleButton playButton = (ToggleButton) findViewById(R.id.recording_play_play_button);
		playButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					startAudioPlayback();
				} else {
					stopAudioPlayback();
				}
			}
		});
		ImageButton gotoSceneButton = (ImageButton) findViewById(R.id.recording_play_scene_button);
		gotoSceneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mediaPlayer.isPlaying()) {
					playButton.toggle();
				}
				Intent intent = new Intent(RecordingPlayer.this,
						ScenePlayer.class);
				intent.putExtra(SceneColumn.ID.getLabel(), sceneId);
				startActivity(intent);
			}
		});
		playButton.setChecked(true);
		startAudioPlayback();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// shut down the media player
		try {
			seekbarUpdateRunnable.stopUpdating();
			seekbarUpdateSemaphore.acquire(); // ensure that the seekbar update
												// thread really is done
			mediaPlayer.stop();
			mediaPlayer.release();
			seekbarUpdateSemaphore.release();
		} catch (InterruptedException ie) {
			Log.e(RecordingPlayer.class.getSimpleName(), "", ie);
		}
	}

	private void startAudioPlayback() {
		mediaPlayer.start();
		seekbarUpdateRunnable.reset();
		Thread thread = new Thread(seekbarUpdateRunnable);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName(SEEKBAR_UPDATE_THREAD_NAME);
		thread.start();
	}

	private void stopAudioPlayback() {
		mediaPlayer.pause();
		seekbarUpdateRunnable.stopUpdating();
	}

	private class SeekbarUpdateRunnable implements Runnable {

		private final SeekBar seekBar;
		private volatile boolean shouldContinueUpdatingSeekbar;

		public SeekbarUpdateRunnable(SeekBar seekBar) {
			this.seekBar = seekBar;
			shouldContinueUpdatingSeekbar = true;
		}

		public void run() {
			while (shouldContinueUpdatingSeekbar) {
				try {
					seekbarUpdateSemaphore.acquire();
					seekBar.setProgress(mediaPlayer.getCurrentPosition());
					seekbarUpdateSemaphore.release();
					Thread.sleep(SEEKBAR_UPDATE_INTERVAL_MS);
				} catch (InterruptedException ie) {
					// whatever, just get on with it
					Log.e(RecordingPlayer.class.getSimpleName(),
							SEEKBAR_UPDATE_THREAD_NAME
									+ " interrupted during update sleep.", ie);
				}
			}
		}

		public void reset() {
			shouldContinueUpdatingSeekbar = true;
		}

		public void stopUpdating() {
			shouldContinueUpdatingSeekbar = false;
		}
	}
}
