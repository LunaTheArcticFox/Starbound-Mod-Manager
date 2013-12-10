package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
			
			File[] files = file.listFiles();
			
			if (files != null) {
				
				for (File f : files) {
					deleteFile(f);
				}
				
			}
			
			Files.delete(file.toPath());
			
		} else {
			Files.delete(file.toPath());
		}
		
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
	
}