/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.puredata.android.scenes.SceneDataBase.SceneColumn;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lamerman.FileDialog;

public class SceneSelection extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

	private static final String TAG = "Scene Selection";
	private static final int FILE_SELECT_CODE = 1;
	private ListView sceneView;
	private Button updateButton;
	private Button downloadButton;
	private SceneDataBase db;
	private Cursor cursor = null;
	private Toast toast = null;
	
	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
				toast.setText(msg);
				toast.show();
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		db = new SceneDataBase(this);
		try {
			// Many thanks to Frank Barknecht for providing Atsuke as a sample scene for inclusion in this package!
			addSceneDirectory(getResources().openRawResource(R.raw.atsuke));
		} catch (Exception e) {
			// Do nothing.
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		final Uri uri = intent.getData();
		if (uri.getScheme().equals(("rjdj")) || uri.getScheme().equals("http")) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						URL url = new URL("http:" + uri.toString().substring(5));
						URLConnection connection = url.openConnection();
						connection.connect();
						addSceneDirectory(connection.getInputStream());
					} catch (Exception e) {
						toast("Unable to open URI " + uri);
					}
					return null;
				}
			}.execute();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		db.close();
	}

	private void initGui() {
		setContentView(R.layout.scene_selection);
		sceneView = (ListView) findViewById(R.id.scene_selection);
		updateButton = new Button(this);
		updateButton.setText(getResources().getString(R.string.update_label));
        downloadButton = new Button(this);
        downloadButton.setText(getResources().getString(R.string.download_label));
		sceneView.addFooterView(updateButton);
//		sceneView.addFooterView(downloadButton);
		sceneView.setOnItemClickListener(this);
		sceneView.setOnItemLongClickListener(this);
		updateButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
	}

	private void updateList() {
		if (cursor != null) {
			cursor.close();
		}
		cursor = db.getAllScenes();
		SceneListCursorAdapter adapter = new SceneListCursorAdapter(SceneSelection.this, cursor);
		sceneView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent = new Intent(this, ScenePlayer.class);
		intent.putExtra(SceneColumn.ID.getLabel(), id);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, final long id) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(getResources().getString(R.string.delete_scene_title));
		dialog.setMessage(getResources().getString(R.string.delete_scene_message));
		dialog.setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					db.deleteScene(id);
				} catch (IOException e) {
					toast(getResources().getString(R.string.delete_scene_fail));
				}
				updateList();
			}
		});
		dialog.setNegativeButton(getResources().getString(android.R.string.no), null);
		dialog.show();
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(updateButton)) {
			Intent intent = new Intent(this, FileDialog.class);
			SharedPreferences prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
			String startPath = prefs.getString(FileDialog.START_PATH,
					Environment.getExternalStorageDirectory().getPath());
			intent.putExtra(FileDialog.START_PATH, startPath);
			intent.putExtra(FileDialog.SELECT_PATTERN, ".*\\.(rj|rjz)");
			intent.putExtra(FileDialog.ACCEPT_FOLDER, true);
			intent.putExtra(FileDialog.ACCEPT_FILE, true);
			startActivityForResult(intent, FILE_SELECT_CODE);
		} else if (v.equals(downloadButton)) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getResources().getString(R.string.download_url)));
			startActivity(intent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == FILE_SELECT_CODE) {
			String path = data.getStringExtra(FileDialog.RESULT_PATH);
			File file = new File(path);
			try {
				if (file.isDirectory()) {
					addSceneDirectory(file);
				} else {
					addSceneDirectory(new FileInputStream(file));
				}
				SharedPreferences prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
				prefs.edit().putString(FileDialog.START_PATH, file.getParent()).commit();
			} catch (Exception e) {
				toast(getResources().getString(R.string.open_scene_fail) + " " + path);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.selection_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		switch (item.getItemId()) {
		case R.id.selection_about_item:
			ad.setTitle(R.string.selection_about_title);
			ad.setMessage(R.string.selection_about_msg);
			break;
		case R.id.selection_help_item:
			ad.setTitle(R.string.selection_help_title);
			ad.setMessage(R.string.selection_help_msg);
			break;
		default:
			break;
		}
		ad.setNeutralButton(android.R.string.ok, null);
		ad.setCancelable(true);
		ad.show();
		return true;
	}

	private void addSceneDirectory(File file) throws IOException {
		db.addScene(file);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateList();
			}
		});
	}

	private void addSceneDirectory(InputStream in) throws IOException {
		List<File> files = IoUtils.extractZipResource(in, getDir("scenes", Context.MODE_PRIVATE), true);
		if (files.isEmpty()) {
			toast("Received no files from stream.");
			return;
		}
		File file = files.get(0);
		while (!file.isDirectory() && !file.getName().endsWith(".rj")) {
			file = file.getParentFile();
		}
		addSceneDirectory(file);
	}
}
