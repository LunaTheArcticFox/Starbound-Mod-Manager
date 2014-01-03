package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileHelper {

	public static void deleteFile(String file) throws IOException {
		deleteFile(new File(file));
	}

	public static void deleteFile(File file) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				deleteFile(child);
		}
		if (!file.delete())
			throw new FileNotFoundException("Failed to delete file: " + file);
	}

	public static void listFiles(String directoryName, ArrayList<File> files) {

		File directory = new File(directoryName);

		File[] fileList = directory.listFiles();

		for (File file : fileList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listFiles(file.getAbsolutePath(), files);
			}
		}

	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			if (!dest.exists()) {
				dest.mkdir();
			}

			String files[] = src.list();

			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile, destFile);
			}

		} else {
			copyFile(src, dest);
		}

	}

	public static final void copyDirectory(String src, String dst)
			throws IOException {
		copyDirectory(new File(src), new File(dst));
	}

	public static final void copyDirectory(File source, File destination)
			throws IOException {
		if (!source.isDirectory()) {
			throw new IllegalArgumentException("Source (" + source.getPath()
					+ ") must be a directory.");
		}

		if (!source.exists()) {
			throw new IllegalArgumentException("Source directory ("
					+ source.getPath() + ") doesn't exist.");
		}

		destination.mkdirs();
		File[] files = source.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				copyDirectory(file, new File(destination, file.getName()));
			} else {
				copyFile(file, new File(destination, file.getName()));
			}
		}
	}

	public static void copyFile(String src, String dest) throws IOException {
		copyFile(new File(src), new File(dest));
	}

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

	public static void copyFolder(String src, String dest) throws IOException {
		copyFolder(new File(src), new File(dest));
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

	public static String fileToJSON(File file) throws IOException {

		String output = "";
		BufferedReader in = new BufferedReader(new FileReader(file));

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