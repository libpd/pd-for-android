package org.puredata.android.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import android.os.RemoteException;

public class PdUtils {

	public static final String LAUNCH_ACTION = "org.puredata.android.service.LAUNCH";
	public static final String STOP_ACTION   = "org.puredata.android.service.STOP";

	private PdUtils() {
		// not to be instantiated
	}
	
	/**
	 * convenience method using varargs instead of lists
	 * @param service
	 * @param dest
	 * @param args
	 * @throws RemoteException
	 */
	public static void sendList(IPdService service, String dest, Object... args) throws RemoteException {
		service.sendList(dest, Arrays.asList(args));
	}
	
	/**
	 * convenience method using varargs instead of lists
	 * @param service
	 * @param dest
	 * @param symbol
	 * @param args
	 * @throws RemoteException
	 */
	public static void sendMessage(IPdService service, String dest, String symbol, Object... args) throws RemoteException {
		service.sendMessage(dest, symbol, Arrays.asList(args));
	}

	/**
	 * same as "compute audio" checkbox in pd gui
	 * 
	 * @param flag
	 * @throws RemoteException 
	 */
	public static void computeAudio(IPdService service, boolean flag) throws RemoteException {
		sendMessage(service, "pd", "dsp", flag ? 1 : 0);
	}
	
	/**
	 * reads a patch from a file
	 * 
	 * @param service
	 * @param file containing the patch
	 * @return pd symbol representing patch
	 * @throws IOException in case patch fails to open
	 * @throws RemoteException if service connection is lost
	 */
	public static String openPatch(IPdService service, File file) throws IOException, RemoteException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		String folder = file.getParentFile().getAbsolutePath();
		String filename = file.getName();
		String patch = "pd-" + filename;
		if (service.exists(patch)) {
			closePatch(service, patch);
		}
		sendMessage(service, "pd", "open", filename, folder);
		if (!service.exists(patch)) {
			throw new IOException("patch " + file + " failed to open, no idea why");
		}
		return patch;
	}
	
	/**
	 * reads a patch from a file
	 * 
	 * @param service
	 * @param path to the file
	 * @return pd symbol representing patch
	 * @throws RemoteException
	 * @throws IOException
	 */
	public static String openPatch(IPdService service, String path) throws RemoteException, IOException {
		return openPatch(service, new File(path));
	}

	/**
	 * closes a patch
	 * 
	 * @param patch name of patch, as returned by openPatch
	 * @throws RemoteException 
	 */
	public static void closePatch(IPdService service, String patch) throws RemoteException {
		sendMessage(service, patch, "menuclose");
	}
}
