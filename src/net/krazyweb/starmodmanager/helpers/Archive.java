package net.krazyweb.starmodmanager.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	private HashSet<ArchiveFile> files = new HashSet<ArchiveFile>();
	
	public Archive(final File file) {
		this.file = file;
	}
	
	public boolean extract() {
		
		try {
			
			long time = System.currentTimeMillis();
			
			if (!FileHelper.verify(file)) {
				//TODO Inform user of invalid filetype.
				return false;
			}
			
			RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "r");
			ISevenZipInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			
			for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
				
				ArchiveFile file = new ArchiveFile();
				
				file.setPath(item.getPath());
				
				if (item.isFolder()) {
					file.setFolder(true);
					files.add(file);
					continue;				
				}
				
				ExtractOperationResult result;
	            
	            final byte[] output = new byte[item.getSize().intValue()];
	            
	            result = item.extractSlow(new ISequentialOutStream() {
	                public int write(byte[] data) throws SevenZipException {
	                	for (int i = 0; i < data.length; i++) {
	                		output[i] = data[i];
	                	}
	                    return data.length;
	                }
	            });
	
	            if (result == ExtractOperationResult.OK) {
	            	file.setData(output);
	            }
	            
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
	
	public boolean writeToFile(final File f) {
		
		try {
			
			long time = System.currentTimeMillis();
		
			FileOutputStream fileOutput = new FileOutputStream(f);
			ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);
			
			for (ArchiveFile file : files) {
				
				if (file.isFolder()) {
					continue;
				}
				
				ZipEntry entry = new ZipEntry(file.getPath());
				zipOutput.putNextEntry(entry);
				
				zipOutput.write(file.getData());
				
				zipOutput.closeEntry();
				
			}
			
			zipOutput.close();
			fileOutput.close();
			
			System.out.println("Time to write '" + f.getName() + "': " + (System.currentTimeMillis() - time) + "ms");
			
			return true;
		
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
}