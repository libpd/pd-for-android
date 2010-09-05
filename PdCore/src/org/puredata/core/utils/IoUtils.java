package org.puredata.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Build;

public class IoUtils {

	private static final boolean v7aFlag = Integer.parseInt(Build.VERSION.SDK) >= 4 && VersionedAbiCheck.hasV7a();
	
	// use lazy class loading to hide Build.CPU_ABI from Cupcake
	private static class VersionedAbiCheck {
		private static boolean hasV7a() {
			return "armeabi-v7a".equals(Build.CPU_ABI);
		}
	}
	
	/**
	 * hackish check for armeabi-v7a
	 * @return true iff armeabi-v7a is available
	 */
	public static boolean hasArmeabiV7a() {
		return v7aFlag;
	}
	
	/**
	 * extracts a resource into a real file
	 * 
	 * @param in typically given as getResources().openRawResource(R.raw.something)
	 * @param name of the resulting file
	 * @param directory target directory
	 * @return the resulting file
	 * @throws IOException
	 */
	public static File extractResource(InputStream in, String filename, File directory) throws IOException {
		int n = in.available();
		byte[] buffer = new byte[n];
		in.read(buffer);
		in.close();
		File file = new File(directory, filename);
		FileOutputStream out = new FileOutputStream(file);
		out.write(buffer);
		out.close();
		return file;
	}
	
	/**
	 * extracts a zip resource into real files and directories, not overwriting existing files
	 * @param in typically given as getResources().openRawResource(R.raw.something)
	 * @param directory target directory
	 * @return list of files that were unpacked, not including files that existed before
	 * @throws IOException
	 */
	public static List<File> extractZipResource(InputStream in, File directory) throws IOException {
		return extractZipResource(in, directory, false);
	}

	
	/**
	 * extracts a zip resource into real files and directories
	 * @param in typically given as getResources().openRawResource(R.raw.something)
	 * @param directory target directory
	 * @param overwrite indicates whether to overwrite existing files
	 * @return list of files that were unpacked (if overwrite is false, this list won't include files that existed before)
	 * @throws IOException
	 */
	public static List<File> extractZipResource(InputStream in, File directory, boolean overwrite) throws IOException {
		final int BUFSIZE = 2048;
		byte buffer[] = new byte[BUFSIZE];
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in, BUFSIZE));
		List<File> files = new ArrayList<File>();
		ZipEntry entry;
		directory.mkdirs();
		while ((entry = zin.getNextEntry()) != null) {
			File file = new File(directory, entry.getName());
			if (overwrite || !file.exists())	{
				files.add(file);
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), BUFSIZE);
					int nRead;
					while ((nRead = zin.read(buffer, 0, BUFSIZE)) > 0) {
						bos.write(buffer, 0, nRead);
					}
					bos.flush();
					bos.close();
				}
			}
		}
		zin.close();
		return files;
	}

	/**
	 * finds all files matching a given pattern in the given directory and below
	 * @param dir
	 * @param pattern
	 * @return
	 */
	public static List<String> find(File dir, String pattern) {
		final List<String> hits = new ArrayList<String>();
		final Pattern p = Pattern.compile(pattern);
		traverseTree(dir, new FileProcessor() {
			@Override
			public void processFile(File file) {
				if (p.matcher(file.getName()).matches()) {
					hits.add(file.getAbsolutePath());
				}
			}
		});
		return hits;
	}

	private interface FileProcessor {
		void processFile(File file);
	}

	private static void traverseTree(File file, FileProcessor fp) {
		fp.processFile(file);
		if (file.isDirectory()) {
			String[] children = file.list();
			for (int i = 0; i < children.length; i++) {
				traverseTree(new File(file, children[i]), fp);
			}
		}
	}
}
