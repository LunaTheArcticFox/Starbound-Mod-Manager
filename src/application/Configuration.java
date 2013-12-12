package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Configuration {
	
	public static class KeyValuePair {
		
		public String key;
		public String value;
		
		public KeyValuePair(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
	}
	
	private static final File configFile = new File("config.ini");
	
	public static String systemType;
	
	public static File modsFolder;
	public static File backupFolder;
	public static File starboundFolder;
	public static File bootstrapFile;
	public static File modsInstallFolder;
	public static File modsPatchesFolder;

	public static HashSet<String> fileTypesToIgnore;
	
	public static void load(Stage primaryStage) {
		
		if (!configFile.exists()) {
			firstRun(primaryStage);
		}
		
		systemType = getProperty("systemtype", "Windows");
		modsFolder = new File(getProperty("modsfolder", "null"));
		backupFolder = new File(getProperty("backupfolder", "null"));
		starboundFolder = new File(getProperty("gamefolder", "null"));
		modsInstallFolder = new File(starboundFolder.getAbsolutePath() + File.separator + "mods");
		modsPatchesFolder = new File(starboundFolder.getAbsolutePath() + File.separator + "mods" + File.separator + "patches");
		
		if (!backupFolder.exists()) {
			backupFolder.mkdir();
		}
		
		File playerBackup = new File(backupFolder.getAbsolutePath() + File.separator + "player");
		File universeBackup = new File(backupFolder.getAbsolutePath() + File.separator + "universe");
		
		if (!playerBackup.exists()) {
			playerBackup.mkdir();
		}
		
		if (!universeBackup.exists()) {
			universeBackup.mkdir();
		}
		
		if (!modsFolder.exists()) {
			modsFolder.mkdir();
		}
		
		switch (systemType) {
			default:
			case "Windows":
				bootstrapFile = new File(starboundFolder.getAbsolutePath() + File.separator + "win32" + File.separator + "bootstrap.config");
				break;
			case "Linux (32-Bit)":
				bootstrapFile = new File(starboundFolder.getAbsolutePath() + File.separator + "linux32" + File.separator + "bootstrap.config");
				break;
			case "Linux (64-Bit)":
				bootstrapFile = new File(starboundFolder.getAbsolutePath() + File.separator + "linux64" + File.separator + "bootstrap.config");
				break;
			case "Mac OS":
				bootstrapFile = new File(starboundFolder.getAbsolutePath() + File.separator + "Starbound.app" + File.separator + "Contents" + File.separator + "MacOS" + File.separator + "bootstrap.config");
				break;
		}
		
		ArrayList<File> modFiles = new ArrayList<File>();
		FileHelper.listFiles(getProperty("modsfolder", "null"), modFiles);
		
		for (KeyValuePair kvp : getProperties("mods")) {
			if (!modFiles.contains(new File(modsFolder.getAbsolutePath() + File.separator + kvp.key))) {
				removeProperty(kvp.key);
			}
		}
		
		for (File f : modFiles) {
			if (getProperty(f.getName(), "modnotfound").equals("modnotfound")) {
				if (f.getAbsolutePath().endsWith(".zip")) {
					addProperty("mods", f.getName(), "false");
				}
			}
		}
		
		fileTypesToIgnore = new HashSet<String>();
		fileTypesToIgnore.add(".png");
		fileTypesToIgnore.add(".wav");
		fileTypesToIgnore.add(".ogg");
		fileTypesToIgnore.add(".lua");
		fileTypesToIgnore.add(".ttf");
		fileTypesToIgnore.add(".dat");
		fileTypesToIgnore.add(".json");
		
	}
	
	private static void firstRun(Stage primaryStage) {
		
		String systemType = (new FXDialogueDropdown("Select your system: ", new String[] { "Windows", "Linux (32-Bit)", "Linux (64-Bit)", "Mac OS" }, 0)).show();
		
		new FXDialogueConfirm("Please locate your game folder.").show();
		
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select Game Folder");
		
		final File gameDirectory = dirChooser.showDialog(primaryStage);
		
		if (gameDirectory == null) {
			new FXDialogueConfirm("Please choose a folder — The Mod Manager will now Exit.").show();
			System.exit(0);
		}
		
		try {
			
			FileWriter writer = new FileWriter(configFile);
			BufferedReader defaultConfig = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("defaultconfig.ini")));
			
			String line = null;
			
			while ((line = defaultConfig.readLine()) != null) {
				line = line.replace("%%systemtype%%", systemType);
				line = line.replace("%%gamepath%%", gameDirectory.getAbsolutePath());
				line = line.replace("%%modsfolder%%", new File("mods").getAbsolutePath());
				line = line.replace("%%backupfolder%%", new File("backups").getAbsolutePath());
				line = line.replace("%%manifestlocation%%", new File("assets.manifest").getAbsolutePath());
				writer.append(line + "\r\n");
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!new File(getProperty("modsfolder", "null")).exists()) {
			new File(getProperty("modsfolder", "null")).mkdir();
		}
		
		ArrayList<File> modFiles = new ArrayList<File>();
		FileHelper.listFiles(getProperty("modsfolder", "null"), modFiles);
		
		for (File f : modFiles) {
			if (f.getAbsolutePath().endsWith(".zip")) {
				addProperty("mods", f.getName(), "false");
			}
		}
		
	}
	
	public static void addProperty(String header, String key, String value) {
		
		ArrayList<String> configData = new ArrayList<String>();

		try {
			
			BufferedReader config = new BufferedReader(new FileReader(configFile));
			String line;
			
			while ((line = config.readLine()) != null) {
				configData.add(line);
			}
			
			config.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			
			FileWriter configOutput = new FileWriter(configFile);
			
			boolean exists = false;
			
			for (String line : configData) {
				if (line.startsWith(key)) {
					exists = true;
				}
			}
			
			boolean headerFound = false;
			
			for (String line : configData) {
				
				if (exists && line.startsWith(key)) {
					
					configOutput.append(key + ": \"" + value + "\"\r\n");
					
				} else {
					
					if (!exists && line.contains("[" + header + "]")) {
						headerFound = true;
					}
				
					configOutput.append(line + "\r\n");
					
					if (headerFound && (line.trim().isEmpty() || configData.indexOf(line) == configData.size() - 1)) {
						configOutput.append(key + ": \"" + value + "\"\r\n");
					}
				
				}
				
			}
			
			configOutput.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void addPropertyHeader(String header) {
		
		ArrayList<String> configData = new ArrayList<String>();

		try {
			
			BufferedReader config = new BufferedReader(new FileReader(configFile));
			String line;
			
			while ((line = config.readLine()) != null) {
				configData.add(line);
			}
			
			config.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			
			FileWriter configOutput = new FileWriter(configFile);
			
			for (int i = 0; i < configData.size(); i++) {
				
				configOutput.append(configData.get(i) + "\r\n");
				
				if (i == configData.size() - 1) {
					configOutput.append("\r\n[" + header + "]");
				}
				
			}
			
			configOutput.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getProperty(String key, String defaultValue) {
		
		String value = null;

		try {
			
			BufferedReader config = new BufferedReader(new FileReader(configFile));
			String line;
			
			while ((line = config.readLine()) != null) {
				if (line.startsWith(key)) {
					value = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
				}
			}
			
			config.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
		
	}
	
	public static ArrayList<KeyValuePair> getProperties(String header) {
		
		ArrayList<KeyValuePair> values = new ArrayList<KeyValuePair>();

		try {
			
			BufferedReader config = new BufferedReader(new FileReader(configFile));
			String line;
			boolean inHeader = false;
			
			while ((line = config.readLine()) != null) {
				
				if (line.startsWith("[" + header + "]")) {
					inHeader = true;
				} else if (line.startsWith("[")) {
					inHeader = false;
				}
				
				if (inHeader && line.contains(":")) {
					values.add(new KeyValuePair(line.substring(0, line.indexOf(":")), line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""))));
				}
				
			}
			
			config.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return values;
		
	}
	
	public static void removeProperty(String key) {
		
		ArrayList<String> configData = new ArrayList<String>();

		try {
			
			BufferedReader config = new BufferedReader(new FileReader(configFile));
			String line;
			
			while ((line = config.readLine()) != null) {
				configData.add(line);
			}
			
			config.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			
			FileWriter configOutput = new FileWriter(configFile);
			
			for (String line : configData) {
				
				if (line.startsWith(key)) {
					continue;
				} else {
					configOutput.append(line + "\r\n");
				}
				
			}
			
			configOutput.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void updateBootstrap(ArrayList<Mod> installedMods) {
		
		Collections.reverse(installedMods);
		
		try {
			
			FileWriter bootstrapOutput = new FileWriter(Configuration.bootstrapFile);
			
			bootstrapOutput.append("{\r\n");
			bootstrapOutput.append("  \"assetSources\" : [\r\n");

			if (installedMods.size() == 0) {
				bootstrapOutput.append("    \"../assets\"\r\n");
			} else {
				bootstrapOutput.append("    \"../assets\",\r\n");
			}

			for (int i = 0; i < installedMods.size(); i++) {
				bootstrapOutput.append("    \"../" + Configuration.modsInstallFolder.getName() + "/" + installedMods.get(i).name + "\"");
				if (i != installedMods.size() - 1) {
					bootstrapOutput.append(",");
				} else {
					if (modsPatchesFolder.exists()) {
						bootstrapOutput.append(",");
						bootstrapOutput.append("\r\n    \"../" + Configuration.modsInstallFolder.getName() + "/" + Configuration.modsPatchesFolder.getName() + "\"");
					}
				}
				bootstrapOutput.append("\r\n");
			}

			bootstrapOutput.append("  ],\r\n");
			bootstrapOutput.append("  \"storageDirectory\" : \"..\"\r\n");
			bootstrapOutput.append("}\r\n");
			
			bootstrapOutput.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}