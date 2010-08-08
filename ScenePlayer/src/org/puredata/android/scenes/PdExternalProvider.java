/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;


public class PdExternalProvider extends ContentProvider {

	@SuppressWarnings("unused")
	private static final String PD_EXTERNAL_PROVIDER = "Pd External Provider";
	
	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {
		AssetManager am = getContext().getAssets();
		String path = uri.getPath().substring(1); // remove leading slash
		try {
			return am.openNonAssetFd(path);
		} catch (IOException e) {
			throw new FileNotFoundException(e.toString());
		}
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
	@Override
	public String getType(Uri uri) {
		return null;
	}
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
