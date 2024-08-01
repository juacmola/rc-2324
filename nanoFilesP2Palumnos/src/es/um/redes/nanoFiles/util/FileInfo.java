package es.um.redes.nanoFiles.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import es.um.redes.nanoFiles.shell.NFShell;

public class FileInfo {
	public String fileHash;
	public String fileName;
	public String filePath;
	public String fileNick;
	public long fileSize = -1;

	public FileInfo(String hash, String name, long size, String path) {
		fileHash = hash;
		fileName = name;
		fileSize = size;
		filePath = path;
	}

	public FileInfo() {
	}

	public void setNick(String nick) {
		this.fileNick = nick;
	}
	
	public String getHash() {
		return fileHash;
	}
	
	public String toPublish() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(fileName);
		strBuf.append("+");
		strBuf.append(fileSize);
		strBuf.append("+");
		strBuf.append(fileHash);
		return strBuf.toString();
	}
	
	public String toFileList(String nick) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(fileName);
		strBuf.append("+");
		strBuf.append(fileSize);
		strBuf.append("+");
		strBuf.append(fileHash);
		strBuf.append("+");
		strBuf.append(nick);
		return strBuf.toString();
	}
	
	public static FileInfo[] fromPublish(String buff, int numFiles) {
		if (buff.endsWith(",")) buff = buff.substring(0, buff.length() - 1);
		
		FileInfo[] files = new FileInfo[numFiles];
		String[] entries = buff.split(","); // Posición del delimitador
		
		for (int i=0; i<numFiles; i++) {
			String[] parts = entries[i].split("\\+");
			files[i] = new FileInfo(parts[2], parts[0], Long.parseLong(parts[1]) , "");
		}
		return files;
	}
	
	public static FileInfo[] fromFileList(String buff, int numFiles) {
		if (buff.endsWith(",")) buff = buff.substring(0, buff.length() - 1);
		
		FileInfo[] files = new FileInfo[numFiles];
		String[] entries = buff.split(","); // Posición del delimitador
		
		for (int i=0; i<numFiles; i++) {
			String[] parts = entries[i].split("\\+");
			files[i] = new FileInfo(parts[2], parts[0], Long.parseLong(parts[1]) , "");
			files[i].setNick(parts[3]);
		}
		return files;
	}
	
	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(String.format("%1$-30s", fileName));
		strBuf.append(String.format("%1$10s", fileSize));
		strBuf.append(String.format(" %1$-45s", fileHash));
		return strBuf.toString();
	}

	public static void printToSysout(FileInfo[] files) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(String.format("%1$-30s", "Name"));
		strBuf.append(String.format("%1$10s", "Size"));
		strBuf.append(String.format(" %1$-45s", "Hash"));
		System.out.println(strBuf);
		for (FileInfo file : files) {
			System.out.println(file);
		}
	}
	
	public static void printToSysoutFileList(FileInfo[] files) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(String.format("%1$-30s", "Name"));
		strBuf.append(String.format("%1$10s", "Size"));
		strBuf.append(String.format(" %1$-45s", "Hash"));
		strBuf.append(String.format("%1$10s", "Nickname del servidor"));
		System.out.println(strBuf);
		for (FileInfo file : files) {
			System.out.println(file + file.fileNick);
		}
	}

	/**
	 * Scans the given directory and returns an array of FileInfo objects, one for
	 * each file recursively found in the given folder and its subdirectories.
	 * 
	 * @param sharedFolderPath The folder to be scanned
	 * @return An array of file metadata (FileInfo) of all the files found
	 */
	public static FileInfo[] loadFilesFromFolder(String sharedFolderPath) {
		File folder = new File(sharedFolderPath);

		Map<String, FileInfo> files = loadFileMapFromFolder(folder);

		FileInfo[] fileinfoarray = new FileInfo[files.size()];
		Iterator<FileInfo> itr = files.values().iterator();
		int numFiles = 0;
		while (itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	/**
	 * Scans the given directory and returns a map of <filehash,FileInfo> pairs.
	 * 
	 * @param folder The folder to be scanned
	 * @return A map of the metadata (FileInfo) of all the files recursively found
	 *         in the given folder and its subdirectories.
	 */
	protected static Map<String, FileInfo> loadFileMapFromFolder(final File folder) {
		Map<String, FileInfo> files = new HashMap<String, FileInfo>();
		scanFolderRecursive(folder, files);
		return files;
	}

	private static void scanFolderRecursive(final File folder, Map<String, FileInfo> files) {
		if (folder.exists() == false) {
			System.err.println("scanFolder cannot find folder " + folder.getPath());
			return;
		}
		if (folder.canRead() == false) {
			System.err.println("scanFolder cannot access folder " + folder.getPath());
			return;
		}

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				scanFolderRecursive(fileEntry, files);
			} else {
				String fileName = fileEntry.getName();
				String filePath = fileEntry.getPath();
				String fileHash = FileDigest.computeFileChecksumString(filePath);
				long fileSize = fileEntry.length();
				if (fileSize > 0) {
					files.put(fileHash, new FileInfo(fileHash, fileName, fileSize, filePath));
				} else {
					if (fileName.equals(NFShell.FILENAME_TEST_SHELL)) {
						NFShell.enableVerboseShell();
						System.out.println("[Enabling verbose shell]");
					} else {
						System.out.println("Ignoring empty file found in shared folder: " + filePath);
					}
				}
			}
		}
	}

	public static FileInfo[] lookupHashSubstring(FileInfo[] files, String hashSubstr) {
		String needle = hashSubstr.toLowerCase();
		Vector<FileInfo> matchingFiles = new Vector<FileInfo>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].fileHash.toLowerCase().contains(needle)) {
				matchingFiles.add(files[i]);
			}
		}
		FileInfo[] result = new FileInfo[matchingFiles.size()];
		matchingFiles.toArray(result);
		return result;
	}
}
