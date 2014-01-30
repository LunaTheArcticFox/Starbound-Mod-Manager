package net.krazyweb.starmodmanager.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class Archive {
	
	private File file;
	private String modBaseDirectory;
	private HashSet<ArchiveFile> files = new HashSet<ArchiveFile>();
	
	public Archive(final File file) {
		this.file = file;
	}
	
	public ArchiveFile getFile(String fileName) {
		
		for (ArchiveFile file : files) {
			if (file.getPath().endsWith(fileName)) {
				return file;
			}
		}
		
		return null;
		
	}
	
	public HashSet<ArchiveFile> getFiles() {
		return files;
	}
	
	public boolean extract() {
		
		try {
			
			long time = System.currentTimeMillis();
			
			if (!NewFileHelper.verify(file)) {
				/* 
				 * TODO Inform user of invalid filetype.
				 * Change boolean return to error codes to be passed along to the UI.
				 */
				return false;
			}
			
			RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "r");
			ISevenZipInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			
			for (final ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
				
				final ArchiveFile file = new ArchiveFile();
				
				modBaseDirectory = "";
				
				file.setPath(item.getPath().replaceAll("\\\\", "/"));
				
				if (item.isFolder()) {
					file.setFolder(true);
					files.add(file);
					continue;
				}
				
				Path tempDir = Paths.get(file.getPath());
				
				if (file.getPath().endsWith(".modinfo") && tempDir.getParent() != null) {
					modBaseDirectory = Paths.get(file.getPath()).getParent().toString() + "/";
					System.out.println("\n\n:::::" + modBaseDirectory + " ---- " + file.getPath());
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
				
				if (result != ExtractOperationResult.OK) {
					System.err.println("Something went wrong!");
					return false;
				}
				
				file.setData(outputData);
				
				files.add(file);
				
			}
			
			inArchive.close();
			randomAccessFile.close();
			
			System.out.println("Time to extract '" + file.getName() + "' to memory: " + (System.currentTimeMillis() - time) + "ms");
			
			return true;
		
		} catch (IOException | SevenZipException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void clean() {
		
		HashSet<ArchiveFile> filesToRemove = new HashSet<ArchiveFile>();
		
		for (ArchiveFile file : files) {
			
			if (!file.getPath().startsWith(modBaseDirectory)) {
				filesToRemove.add(file);
			} else {
				file.setPath(file.getPath().substring(modBaseDirectory.length(), file.getPath().length()));
			}
			
		}
		
	}
	
	public boolean writeToFile(final File file) {
		
		try {
			
			long time = System.currentTimeMillis();
		
			FileOutputStream fileOutput = new FileOutputStream(file);
			ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);
			
			for (ArchiveFile archiveFile : files) {
				
				if (archiveFile.isFolder()) {
					continue;
				}
				
				ZipEntry entry = new ZipEntry(archiveFile.getPath());
				zipOutput.putNextEntry(entry);
				
				zipOutput.write(archiveFile.getData());
				
				zipOutput.flush();
				zipOutput.closeEntry();
				
			}
			
			zipOutput.close();
			fileOutput.close();
			
			System.out.println("Time to write '" + file.getName() + "': " + (System.currentTimeMillis() - time) + "ms");
			
			return true;
		
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean extractToFolder(final File folder) {
		
		try {
		
			for (ArchiveFile file : files) {
				
				if (file.isFolder()) {
					continue;
				}
				
				File destination = new File(folder.getAbsolutePath() + File.separator + file.getPath());
				destination.getParentFile().mkdirs();
				
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination));
				out.write(file.getData());
				out.flush();
				out.close();
				
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public String getFileName() {
		return file.getName();
	}
	
}