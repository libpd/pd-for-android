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

	public static enum SceneColumn {
		ID("_id", "integer primary key autoincrement, "),
		SCENE_ARTIST("author", "text not null, "),
		SCENE_TITLE("name", "text not null, "),
		SCENE_INFO( "description", "text not null, "),
		SCENE_CATEGORY("category", "text, "),
		SCENE_ID("sceneId", "text, "),
		SCENE_DIRECTORY( "directory", "text unique not null");

		private final String label;
		private final String type;

		private SceneColumn(String label, String type) {
			this.label = label;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}

	public static final String TABLE_SCENES = "scenes";

	private final SceneDataBaseHelper helper;
	private final SQLiteDatabase db;

	public SceneDataBase(Context context) {
		helper = new SceneDataBaseHelper(context);
		db = helper.getWritableDatabase();
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

	public void delete(int id) throws IOException {
		Cursor cursor = getScene(id);
		String path = getString(cursor, SceneColumn.SCENE_DIRECTORY);
		if (new File(path).exists()) {
			Runtime.getRuntime().exec("rm -r " + path);
		}
		db.delete(TABLE_SCENES, idClause(id), null);
	}

	public Cursor getAllScenes() {
		return db.query(TABLE_SCENES, new String[] {SceneColumn.ID.label, SceneColumn.SCENE_ARTIST.label,
			SceneColumn.SCENE_TITLE.label, SceneColumn.SCENE_DIRECTORY.label}, null, null, null, null, SceneColumn.SCENE_TITLE.label);
	}
	
	public Cursor getScene(int id) {
		Cursor cursor = db.query(TABLE_SCENES, null, idClause(id), null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	private String idClause(int id) {
		return SceneColumn.ID.label + " = " + id;
	}

	public static String getString(Cursor cursor, SceneColumn column) {
		return getString(cursor, column.label);
	}

	public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}

	private static class SceneDataBaseHelper extends SQLiteOpenHelper {

		public static final String DATABASE_NAME = "scenedb";
		public static final int DATABASE_VERSION = 15;
		
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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (newVersion != oldVersion) {
				db.execSQL("drop table " + TABLE_SCENES + ";");
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
