package net.krazyweb.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class Archive {
	
	private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(Archive.class);
	
	private Path path;
	private Set<ArchiveFile> files = new HashSet<>();
	
	public Archive(final Path path) {
		this.path = path;
	}
	
	public Archive(final String path) {
		this(Paths.get(path));
	}
	
	public ArchiveFile getFile(String fileName) {
		
		for (ArchiveFile file : files) {
			if (file.getPath().toString().endsWith(fileName)) {
				return file;
			}
		}
		
		return null;
		
	}
	
	public ArchiveFile getFile(final Path filePath) {
		
		for (ArchiveFile file : files) {
			if (file.getPath().equals(filePath)) {
				return file;
			}
		}
		
		return null;
		
	}
	
	public Set<ArchiveFile> getFiles() {
		return files;
	}
	
	public boolean extract() {
		
		log.debug("Extracting: {}", path);
		
		try {
			
			long time = System.currentTimeMillis();
			
			if (!FileHelper.isSupported(path, false)) {
				/* 
				 * TODO Inform user of invalid filetype.
				 * Change boolean return to error codes to be passed along to the UI.
				 */
				return false;
			}
			
			RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r");
			ISevenZipInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			
			for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
				
				final ArchiveFile file = new ArchiveFile();
				
				file.setPath(Paths.get(item.getPath()));
				
				if (item.isFolder()) {
					log.debug("{}", item.getPath());
					file.setFolder(true);
					files.add(file);
					continue;
				}
				
				ExtractOperationResult result;

				final byte[] outputData = new byte[item.getSize().intValue()];
				
				result = item.extractSlow(new ISequentialOutStream() {

				    int offset = 0;

				    public int write(byte[] data) throws SevenZipException {
				        for (int i = 0; i < data.length; i++) {
				            outputData[i + offset] = data[i];
				        }
				        offset += data.length;
				        return data.length;
				    }

				});
				
				file.setData(outputData);
				
				if (result != ExtractOperationResult.OK) {
					return false;
				}
				
				files.add(file);
				
			}
			
			inArchive.close();
			randomAccessFile.close();
			
			log.debug("Time to extract '{}' to memory: {}ms", path, (System.currentTimeMillis() - time));
			
			return true;
		
		} catch (IOException | SevenZipException e) {
			log.error(new ParameterizedMessage("Extracting archive: ", path), e);
			return false;
		}
		
	}
	
	public boolean writeToFile(final File file) {
		
		//TODO Explore updating this to use the Java 7 filesystem.
		
		try {
			
			long time = System.currentTimeMillis();
		
			FileOutputStream fileOutput = new FileOutputStream(file);
			ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);
			
			for (ArchiveFile archiveFile : files) {
				
				if (archiveFile.isFolder()) {
					continue;
				}
				
				log.trace("Writing file to {}: {}", file.getName(), archiveFile.getPath());
				
				ZipEntry entry = new ZipEntry(archiveFile.getPath().toString());
				zipOutput.putNextEntry(entry);
				
				zipOutput.write(archiveFile.getData());
				
				zipOutput.closeEntry();
				
			}
			
			zipOutput.close();
			fileOutput.close();

			log.debug("Time to write '{}': {}ms", file.getName(), (System.currentTimeMillis() - time));
			
			return true;
		
		} catch (IOException e) {
			e.printStackTrace(); //TODO Better Log
			return false;
		}
		
	}
	
	public boolean writeToFile() {
		return writeToFile(path.toFile()); //TODO use path
	}
	
	public boolean extractToFolder(final Path folder) {
		
		try {
		
			for (ArchiveFile file : files) {
				
				if (file.isFolder()) {
					continue;
				}
				
				Files.createDirectories(folder.resolve(file.getPath()).getParent());
				
				OutputStream output = Files.newOutputStream(folder.resolve(file.getPath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				output.write(file.getData());
				output.close();
				
			}
			
			return true;
			
		} catch (IOException e) {
			log.error("Extracting archive to folder.", e);
			return false;
		}
		
	}
	
	public boolean extractFileToFolder(final Path file, final Path folder) {
		
		Path newPath = folder.resolve(file);
		
		try {
			
			Files.createDirectories(newPath.getParent());
			
			OutputStream output = Files.newOutputStream(newPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			output.write(getFile(file).getData());
			output.close();
		
		} catch (IOException e) {
			log.error("Extracting file to folder.", e);
			return false;
		}
		
		return true;
		
	}
	
	public String getFileName() {
		return path.getFileName().toString().replace(".rar", ".zip").replace(".7z", ".zip");
	}
	
	public void addFile(final ArchiveFile file) {
		files.add(file);
	}
	
}