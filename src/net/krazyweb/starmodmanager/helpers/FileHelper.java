package net.krazyweb.starmodmanager.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class FileHelper {
	
	/*
	 * File signatures found at: http://www.garykessler.net/library/file_sigs.html
	 */
	private static final char[] SIG_SEVENZIP = new char[] { 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C };
	private static final char[] SIG_RAR = new char[] { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00 };
	private static final char[] SIG_ZIP = new char[] { 0x50, 0x4B, 0x03, 0x04 };

	public static void copyFile(File src, File dest) throws IOException {

		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);

		byte[] buffer = new byte[1024];

		int length;

		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.close();

	}
	
	public static final char[] readBytes(final File file, int amount) throws IOException {
		
		InputStream in = new FileInputStream(file);
		
		byte[] bytes = new byte[amount];
		
		in.read(bytes);
		in.close();
		
		char[] output = new String(bytes).toCharArray();
		
		return output;
		
	}

	
	public static final boolean verify(final File file) {
		
		if (file == null) {
			return false;
		}
		
		String fileName = file.getName();
		String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
		
		int byteOffset = 0;
		char[] signatureBytes;
		
		switch (extension) {
			case ".7z":
				signatureBytes = SIG_SEVENZIP;
				break;
			case ".rar":
				signatureBytes = SIG_RAR;
				break;
			case ".zip":
				signatureBytes = SIG_ZIP;
				break;
			default:
				return false;
		}
		
		char[] fileBytes = null;
		
		try {
			fileBytes = FileHelper.readBytes(file, signatureBytes.length + byteOffset);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if (fileBytes == null) {
			return false;
		}
		
		for (int i = 0; i < signatureBytes.length; i++) {
			if (fileBytes[i + byteOffset] != (char) signatureBytes[i]) {
				return false;
			}
		}
		
		return true;
		
	}
	
	public static long getChecksum(final File file) throws IOException {
		
		FileInputStream inputFile = new FileInputStream(file);
		CheckedInputStream checkedStream = new CheckedInputStream(inputFile, new Adler32());
		BufferedInputStream input = new BufferedInputStream(checkedStream);
		
		while (input.read() != -1) {
			//Do nothing; simply reading file contents.
		}
		
		input.close();
		checkedStream.close();
		inputFile.close();
		
		return checkedStream.getChecksum().getValue();
		
	}
	
	public static HashSet<File> listFiles(final String directory, final HashSet<File> files) {
		return listFiles(new File(directory), files);
	}
	
	public static HashSet<File> listFiles(final File directory, final HashSet<File> files) {
		
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listFiles(file.getAbsolutePath(), files);
			}
		}
		
		return files;
		
	}
	
	public static void deleteFile(File file) throws IOException {
		
		if (file.isDirectory()) {
			
			File[] children = file.listFiles();
			
			if (children != null) {
				for (File child : file.listFiles()) {
					deleteFile(child);
				}
			}
			
		}
		
		if (!file.delete()) {
			throw new FileNotFoundException("Failed to delete file: " + file);
		}
		
	}
	
	public static boolean isJSON(String filename) {
		
		if (filename.endsWith(".png") || filename.endsWith(".wav") || filename.endsWith(".ogg") || filename.endsWith(".txt") || filename.endsWith(".lua") || filename.endsWith(".ttf")) {
			return false;
		}
		
		return true;
		
	}
	
	public static String fileToString(File file) throws IOException {
	
		String output = "";
		BufferedReader in = new BufferedReader(new FileReader(file));

		String line;

		while ((line = in.readLine()) != null) {
			output += line + "\r\n";
		}

		in.close();

		return output;
	
	}

}