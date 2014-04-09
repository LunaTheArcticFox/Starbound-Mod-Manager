package net.krazyweb.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import net.krazyweb.helpers.FileCopier.TreeCopier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class FileHelper {
	
	private static final Logger log = LogManager.getLogger(FileHelper.class);
	
	/*
	 * File signatures found at: http://www.garykessler.net/library/file_sigs.html
	 */
	private static final char[] SIG_SEVENZIP = new char[] { 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C };
	private static final char[] SIG_RAR = new char[] { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00 };
	private static final char[] SIG_ZIP = new char[] { 0x50, 0x4B, 0x03, 0x04 };
	private static final char[] SIG_PAK = new char[] { 0x53, 0x42, 0x42, 0x46, 0x30, 0x32 };
	
	private static final char[][] SIGNATURES = new char[][] { SIG_SEVENZIP, SIG_RAR, SIG_ZIP, SIG_PAK };
	
	private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList("zip", "7z", "rar", "pak"));

	public static boolean copyFile(Path src, Path dest) {
		
		dest = Files.isDirectory(src) ? dest.resolve(src) : dest;
		
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TreeCopier tc = new TreeCopier(src, dest, false, false);
        
        try {
			Files.walkFileTree(src, opts, Integer.MAX_VALUE, tc);
			return true;
		} catch (IOException e) {
			log.error(new ParameterizedMessage("Copying file/folder: {} to {}", src, dest), e);
		}
        
        return false;
		
	}
	
	public static final String identifyType(final byte[] data) {
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		String extension = "";
		
		if (buffer.remaining() < 7) {
			return "";
		}
		
		for (char[] array : SIGNATURES) {
			
			char[] signatureBytes = array;
			
			boolean typeFound = false;
			
			for (int i = 0; i < signatureBytes.length; i++) {
				
				if (buffer.get() != (char) signatureBytes[i]) {
					buffer.rewind();
					break;
				}
				
				if (i == signatureBytes.length - 1) {
					typeFound = true;
				}
				
			}
			
			if (typeFound) {
				
				if (signatureBytes == SIG_SEVENZIP) {
					extension = "7z";
				} else if (signatureBytes == SIG_RAR) {
					extension = "rar";
				} else if (signatureBytes == SIG_ZIP) {
					extension = "zip";
				} else if (signatureBytes == SIG_PAK) {
					extension = "pak";
				}
				
				break;
				
			}
			
		}
		
		return extension;
		
	}
	
	public static final String identifyType(final Path path, final boolean suppressLogging) {
		
		if (path == null) {
			return null;
		}
		
		String extension = "";
		
		//TODO Clean this up and close the resources properly.
		try {
		
			ByteChannel b = Files.newByteChannel(path, StandardOpenOption.READ);
			ByteBuffer buffer = ByteBuffer.allocate(10);
			b.read(buffer);
			buffer.position(0);
			
			for (char[] array : SIGNATURES) {
				
				char[] signatureBytes = array;
				
				boolean typeFound = false;
				
				for (int i = 0; i < signatureBytes.length; i++) {
					
					byte get = buffer.get();
					
					if (get != (byte) signatureBytes[i]) {
						buffer.position(0);
						break;
					}
					
					if (i == signatureBytes.length - 1) {
						typeFound = true;
						break;
					}
					
				}
				
				if (typeFound) {
					
					if (signatureBytes == SIG_SEVENZIP) {
						extension = "7z";
					} else if (signatureBytes == SIG_RAR) {
						extension = "rar";
					} else if (signatureBytes == SIG_ZIP) {
						extension = "zip";
					} else if (signatureBytes == SIG_PAK) {
						extension = "pak";
					}
					
					break;
					
				}
				
			}
	
			if (!suppressLogging) {
				log.debug("File '{}' identified as '{}'", path, extension);
			}
			
			b.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		return extension;
		
	}
	
	public static final boolean isSupported(final Path path, final boolean suppressLogging) {
		
		if (path == null) {
			return false;
		}
		
		return SUPPORTED_TYPES.contains(identifyType(path, suppressLogging));
		
	}
	
	public static final boolean isSupported(final Path path) {
		return isSupported(path, false);
	}
	
	public static long getChecksum(final Path path) throws IOException {
		
		FileInputStream inputFile = new FileInputStream(path.toFile());
		CheckedInputStream checkedStream = new CheckedInputStream(inputFile, new Adler32());
		BufferedInputStream input = new BufferedInputStream(checkedStream);
		
		while (input.read() != -1) {
			//Do nothing; simply reading file contents.
		}
		
		input.close();
		checkedStream.close();
		inputFile.close();
		
		long checksum = checkedStream.getChecksum().getValue();
		
		log.debug("Checksum ({}) created for file: {}", checksum, path);
		
		return checksum;
		
	}
	
	public static Set<Path> listFiles(final String directory, final Set<Path> paths) {
		return listFiles(Paths.get(directory), paths);
	}
	
	public static Set<Path> listFiles(final Path directory, final Set<Path> paths) {
		
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory.toUri()))) {
            for (Path path : directoryStream) {
            	paths.add(path);
            }
        } catch (IOException e) {
        	log.error(new ParameterizedMessage("Listing files in directory: {}", directory), e);
        }
		
        return paths;
        
	}
	
	public static void deleteFile(final Path path) throws IOException {
		
		//TODO Change to fully use nio
		
		if (Files.isDirectory(path)) {
			
			File[] children = path.toFile().listFiles();
			
			if (children != null) {
				for (File child : path.toFile().listFiles()) {
					deleteFile(child.toPath());
				}
			}
			
		}
		
		Files.deleteIfExists(path);
		
	}
	
	public static boolean isJSON(final Path file) {
		
		if (getExtension(file).equals("png") || getExtension(file).equals("wav") || getExtension(file).equals("ogg") || getExtension(file).equals("txt") || getExtension(file).equals("lua") || getExtension(file).equals("ttf")) {
			return false;
		}
		
		return true;
		
	}
	
	public static String fileToString(File file) {
		
		// TODO update to use nio
	
		String output = "";
		
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {

			String line;

			while ((line = in.readLine()) != null) {
				output += line + "\r\n";
			}
			
		} catch (IOException e) {
			log.error(new ParameterizedMessage("Reading file '{}' to a string.", file), e);
			return null;
		}

		return output;
	
	}
	
	public static String fileToString(final InputStream file) {
		
		// TODO update to use nio
	
		String output = "";
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(file))) {

			String line;

			while ((line = in.readLine()) != null) {
				output += line + "\r\n";
			}
			
		} catch (IOException e) {
			log.error(new ParameterizedMessage("Reading file '{}' to a string.", file), e);
			return null;
		}

		return output;
	
	}
	
	public static String getExtension(final Path path) {
		
		int i = path.toString().lastIndexOf(".");
		
		if (i >= 0) {
			return path.toString().substring(i + 1);
		}
		
		return "";
		
	}

}