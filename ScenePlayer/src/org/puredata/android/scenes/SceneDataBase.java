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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SceneDataBase {

	public static final String TABLE_SCENES = "scenes";
	public static final String TABLE_RECORDINGS = "recordings";

	public interface Column {
		String getLabel();
	}
	
	public static enum SceneColumn implements Column {
		ID("_id", "integer primary key autoincrement, "),
		SCENE_ARTIST("author", "text not null, "),
		SCENE_TITLE("name", "text not null, "),
		SCENE_INFO("description", "text not null, "),
		SCENE_CATEGORY("category", "text, "),
		SCENE_ID("sceneId", "text, "),
		SCENE_DIRECTORY("directory", "text unique not null");

		private final String label;
		private final String type;

		private SceneColumn(String label, String type) {
			this.label = label;
			this.type = type;
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

	public static enum RecordingColumn implements Column {
		ID("_id", "integer primary key autoincrement, "),
		RECORDING_PATH("path", "text not null, "),
		RECORDING_TIMESTAMP("time", "integer not null, "),  // Unix time
		RECORDING_DURATION("duration", "integer not null, "),
		RECORDING_DESCRIPTION("description", "text, "),
		RECORDING_LATITUDE("latitude", "real, "),
		RECORDING_LONGITUDE("longitude", "real, "),
		SCENE_ID("scene_id", "integer not null");

		private final String label;
		private final String type;

		private RecordingColumn(String label, String type) {
			this.label = label;
			this.type = type;
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

	private final SQLiteDatabase db;

	public SceneDataBase(Context context) {
		SceneDataBaseHelper helper = new SceneDataBaseHelper(context);
		db = helper.getWritableDatabase();
	}
	
	public void close() {
		db.close();
	}

	public long addScene(File sceneFolder) throws IOException {
		Map<String, String> sceneInfo;
		try {
			sceneInfo = readInfo(sceneFolder);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		ContentValues values = new ContentValues();
		for (SceneColumn column : SceneColumn.values()) {
			String name = column.label;
			values.put(name, sceneInfo.get(name));
		}
		values.put(SceneColumn.SCENE_DIRECTORY.label, sceneFolder.getAbsolutePath());
		return db.insert(TABLE_SCENES, null, values);
	}
	
	public long addRecording(String path, long time, long duration, double longitude, double latitude, long sceneId) {
		ContentValues values = new ContentValues();
		values.put(RecordingColumn.RECORDING_PATH.label, path);
		values.put(RecordingColumn.RECORDING_TIMESTAMP.label, time);
		values.put(RecordingColumn.RECORDING_DURATION.label, duration);
		values.put(RecordingColumn.RECORDING_LONGITUDE.label, longitude);
		values.put(RecordingColumn.RECORDING_LATITUDE.label, latitude);
		values.put(RecordingColumn.SCENE_ID.label, sceneId);
		return db.insert(TABLE_RECORDINGS, null, values);
	}

	public void deleteScene(long id) throws IOException {
		Cursor cursor = getScene(id);
		String path = getString(cursor, SceneColumn.SCENE_DIRECTORY);
		cursor.close();
		if (new File(path).exists()) {
			Runtime.getRuntime().exec("rm -r " + path);
		}
		db.delete(TABLE_SCENES, sceneIdClause(id), null);
	}
	
	public void deleteRecording(long id) throws IOException {
		Cursor cursor = getRecording(id);
		String path = getString(cursor, RecordingColumn.RECORDING_PATH);
		cursor.close();
		if (new File(path).exists()) {
			Runtime.getRuntime().exec("rm " + path);
		}
		db.delete(TABLE_RECORDINGS, recordingIdClause(id), null);
	}

	public Cursor getAllScenes() {
		return db.query(TABLE_SCENES, null, null, null, null, null, SceneColumn.SCENE_TITLE.label);
	}
	
	public Cursor getAllRecordings() {
		return db.query(TABLE_RECORDINGS, null, null, null, null, null, RecordingColumn.RECORDING_TIMESTAMP.label);
	}
	
	public Cursor getScene(long id) {
		Cursor cursor = db.query(TABLE_SCENES, null, sceneIdClause(id), null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	private String sceneIdClause(long id) {
		return SceneColumn.ID.label + " = " + id;
	}

	public Cursor getRecording(long id) {
		Cursor cursor = db.query(TABLE_RECORDINGS, null, recordingIdClause(id), null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	private String recordingIdClause(long id) {
		return RecordingColumn.ID.label + " = " + id;
	}
	
	public void setRecordingDescription(long id, String description) {
		ContentValues values = new ContentValues();
		values.put(RecordingColumn.RECORDING_DESCRIPTION.label, description);
		db.update(TABLE_RECORDINGS, values, recordingIdClause(id), null);
	}

	public static String getString(Cursor cursor, Column column) {
		return getString(cursor, column.getLabel());
	}
	
	public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}
	
	public static long getLong(Cursor cursor, Column column) {
		return getLong(cursor, column.getLabel());
	}

	public static long getLong(Cursor cursor, String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}

	public static double getDouble(Cursor cursor, Column column) {
		return getDouble(cursor, column.getLabel());
	}
	
	public static double getDouble(Cursor cursor, String column) {
		return cursor.getDouble(cursor.getColumnIndex(column));
	}

	private static class SceneDataBaseHelper extends SQLiteOpenHelper {

		public static final String DATABASE_NAME = "scenedb";
		public static final int DATABASE_VERSION = 1001;
		
		public SceneDataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuilder create = new StringBuilder("create table " + TABLE_SCENES + " (");
			for (SceneColumn column: SceneColumn.values()) {
				create.append(column.label);
				create.append(" ");
				create.append(column.type);
			}
			create.append(");");
			db.execSQL(create.toString());
			
			create = new StringBuilder("create table " + TABLE_RECORDINGS + " (");
			for (RecordingColumn column: RecordingColumn.values()) {
				create.append(column.label);
				create.append(" ");
				create.append(column.type);
			}
			create.append(");");
			db.execSQL(create.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (newVersion != oldVersion) {
				db.execSQL("drop table if exists " + TABLE_SCENES + ";");
				db.execSQL("drop table if exists " + TABLE_RECORDINGS + ";");
				onCreate(db);
			}
		}
	}

	private Map<String, String> readInfo(File sceneFolder)
			throws ParserConfigurationException, SAXException, IOException {
		final Map<String, String> values = new HashMap<String, String>();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		sp.parse(new File(sceneFolder, "Info.plist"), new DefaultHandler() {
			private String key = "", val = "";
			private boolean expectKey;

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				expectKey = localName.equalsIgnoreCase("key");
				if (expectKey) {
					key = val = "";
				}
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				String s = new String(ch, start, length);
				if (expectKey) {
					key += s;
				} else {
					val += s;
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				key = key.trim();
				val = val.trim();
				if (key.length() > 0) {
					values.put(key, val);
				}
			}
		});
		return values;
	}
}
