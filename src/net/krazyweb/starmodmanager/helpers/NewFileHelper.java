package net.krazyweb.starmodmanager.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class NewFileHelper {
	
	//Since Java doesn't have unsigned bytes as a primitive, I use chars to represent unsigned bytes.
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

	/*
	 * File signatures found at: http://www.garykessler.net/library/file_sigs.html
	 */
	
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
			fileBytes = NewFileHelper.readBytes(file, signatureBytes.length + byteOffset);
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

	public static String fileToJSON(ArchiveFile file) throws IOException {

		String output = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getData())));

		String line;

		while ((line = in.readLine()) != null) {
			if (!line.trim().startsWith("//")) {
				output += line + "\r\n";
			}
		}

		in.close();

		return output;

	}

	
}
