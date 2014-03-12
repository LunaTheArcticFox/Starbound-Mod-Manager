package net.krazyweb.starmodmanager.test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.Mod;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMod {
	
	private static final Logger log = LogManager.getLogger(TestMod.class);
	
	private class ModArchiveTest {

		private String archiveFile;
		private List<String> filesInArchive = new ArrayList<>();
		private List<String> filesInManifest = new ArrayList<>();
		
	}
	
	private static final Path TEST_MOD_FOLDER = Paths.get("testmods"); 
	
	private String[] modsToTest = new String[] {
		"testmod1.",
		"testmod2.",
		//"testmod3.", //This is JTE_Farming, which is so poorly packed at the moment, I don't know how to support it.
		"testmod4.",
		"testmod5.",
		"testmod6.",
		"testmod7.",
		"testmod8."
	};
	
	@Before
	public void createModArchives() throws IOException {
		
		System.setProperty("consolelevel", "DEBUG");
		System.setProperty("filelevel", "OFF");
		
		if (Files.notExists(TEST_MOD_FOLDER)) {
			Files.createDirectory(TEST_MOD_FOLDER);
		}
		
		if (Files.notExists(new TestSettingsFactory().getInstance().getPropertyPath("modsdir"))) {
			Files.createDirectory(new TestSettingsFactory().getInstance().getPropertyPath("modsdir"));
		}
		
		for (String mod : modsToTest) {
			
			FileOutputStream output = new FileOutputStream(TEST_MOD_FOLDER + "/" + mod + "zip");
			InputStream input = TestMod.class.getClassLoader().getResourceAsStream(mod + "zip");
			
			byte[] data = new byte[1024];
			int dataRead = 0;
			
			while ((dataRead = input.read(data)) != -1) {
				output.write(data, 0, dataRead);
			}
			
			output.close();
			
		}
		
	}
	
	@Test
	public void testArchiveParsing() throws IOException {
		
		for (String modFile : modsToTest) {
			
			Mod.load(TEST_MOD_FOLDER.resolve(modFile + "zip"), 0, new TestSettingsFactory(), new TestDatabaseFactory(), new LocalizerFactory());
			
		}
		
		List<ModArchiveTest> mods = new ArrayList<>();
		
		for (String modFile : modsToTest) {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(TestMod.class.getClassLoader().getResourceAsStream(modFile + "manifest")));
			String line;
			ModArchiveTest currentMod = null;
			
			while ((line = reader.readLine()) != null) {
				
				if (line.endsWith(".zip")) {
					
					currentMod = new ModArchiveTest();
					mods.add(currentMod);
					
					currentMod.archiveFile = line;
					
					RandomAccessFile randomAccessFile = null;
			        ISevenZipInArchive inArchive = null;
			        
			        try {
			            randomAccessFile = new RandomAccessFile(new TestSettingsFactory().getInstance().getPropertyPath("modsdir").resolve(Paths.get(currentMod.archiveFile)).toString(), "r");
			            inArchive = SevenZip.openInArchive(null, // autodetect archive type
			                    new RandomAccessFileInStream(randomAccessFile));

			            // Getting simple interface of the archive inArchive
			            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
			            	if (!item.isFolder()) {
			            		currentMod.filesInArchive.add(item.getPath().replaceAll("\\\\", "/"));
			            	}
			            }
			        } catch (Exception e) {
			            System.err.println("Error occurs: " + e);
			            System.exit(1);
			        } finally {
			            if (inArchive != null) {
			                try {
			                    inArchive.close();
			                } catch (SevenZipException e) {
			                    System.err.println("Error closing archive: " + e);
			                }
			            }
			            if (randomAccessFile != null) {
			                try {
			                    randomAccessFile.close();
			                } catch (IOException e) {
			                    System.err.println("Error closing file: " + e);
			                }
			            }
			        }
					
				} else {
				
					currentMod.filesInManifest.add(line);
				
				}
				
			}
			
		}
		
		for (ModArchiveTest mod : mods) {
			
			log.debug("{} - {} -- {}", mod.archiveFile, mod.filesInArchive.size(), mod.filesInManifest.size());
			
			/*List<String> output = new ArrayList<>(mod.filesInManifest);
			output.removeAll(mod.filesInArchive);
			log.debug("Difference: {}", output);*/
			
			/*for (String file : mod.filesInArchive) {
				log.debug("Archive: {}", file);
			}
			
			for (String file : mod.filesInManifest) {
				log.debug("Manifest: {}", file);
			}*/
			
			assert(mod.filesInArchive.size() == mod.filesInManifest.size());
			
			for (String file : mod.filesInManifest) {
				assertThat(mod.filesInArchive, hasItem(file));
			}
			
		}
		
	}
	
	@After
	public void cleanupResources() throws IOException {
		FileHelper.deleteFile(TEST_MOD_FOLDER);
		FileHelper.deleteFile(new TestSettingsFactory().getInstance().getPropertyPath("modsdir"));
	}
	
}