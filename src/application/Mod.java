package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

public class Mod {
	
	private static final String installedImage = application.Mod.class.getResource("installed.png").toExternalForm();
	private static final String notInstalledImage = application.Mod.class.getResource("notinstalled.png").toExternalForm();
	private static final String conflictImage = application.Mod.class.getResource("conflict.png").toExternalForm();
	private static final String outdatedImage = application.Mod.class.getResource("outdated.png").toExternalForm();
	
	public ArrayList<String> filesModified = new ArrayList<String>();
	public boolean installed = false;
	public boolean selected = false;
	public boolean hasConflicts = false;
	public boolean patched = false;
	
	public String file;
	public String modInfoName;
	public String displayName;
	public String internalName;
	public String author;
	public String version;
	public String assetsPath;
	public String gameVersion;
	public String description;
	public String conflicts;
	public String subDirectory;
	
	public VBox container;
	public GridPane gridPane;
	public Rectangle bottomStroke;
	public Text modName;
	public Text modAuthor;
	public Text modVersion;
	public CenteredRegion modStatus;
	
	public int order = 0;
	
	private Mod() {
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setName(String name) {
		this.internalName = name;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void updateStyles() {
		
		gridPane.getStyleClass().removeAll("not-installed-mod-fill", "installed-mod-fill", "conflicted-mod-fill");
		bottomStroke.getStyleClass().removeAll("not-installed-mod-stroke", "installed-mod-stroke", "conflicted-mod-stroke");
		modName.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		modVersion.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		modAuthor.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		gridPane.getChildren().remove(modStatus);
		
		if (!Configuration.gameVersionString.equals(gameVersion)) {
			gridPane.getStyleClass().add("outdated-mod-fill");
			bottomStroke.getStyleClass().add("outdated-mod-stroke");
			modName.getStyleClass().add("outdated-font-color");
			modVersion.getStyleClass().add("outdated-font-color");
			modAuthor.getStyleClass().add("outdated-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(outdatedImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (hasConflicts && !installed && !patched) {
			gridPane.getStyleClass().add("conflicted-mod-fill");
			bottomStroke.getStyleClass().add("conflicted-mod-stroke");
			modName.getStyleClass().add("conflicted-font-color");
			modVersion.getStyleClass().add("conflicted-font-color");
			modAuthor.getStyleClass().add("conflicted-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(conflictImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (installed || patched) {
			gridPane.getStyleClass().add("installed-mod-fill");
			bottomStroke.getStyleClass().add("installed-mod-stroke");
			modName.getStyleClass().add("installed-font-color");
			modVersion.getStyleClass().add("installed-font-color");
			modAuthor.getStyleClass().add("installed-font-color");
			if (hasConflicts) {
				modStatus = new CenteredRegion(new ImageView(new Image(conflictImage)));
			} else {
				modStatus = new CenteredRegion(new ImageView(new Image(installedImage)));
			}
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else {
			gridPane.getStyleClass().add("not-installed-mod-fill");
			bottomStroke.getStyleClass().add("not-installed-mod-stroke");
			modName.getStyleClass().add("not-installed-font-color");
			modVersion.getStyleClass().add("not-installed-font-color");
			modAuthor.getStyleClass().add("not-installed-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(notInstalledImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		}
	}
	
	public void setConflicted(boolean conflicted) {
		
		hasConflicts = conflicted;
		
		updateStyles();
		
	}
	
	public ArrayList<String> getFilesModified() {
		return filesModified;
	}

	public void setFilesModified(ArrayList<String> filesModified) {
		this.filesModified = filesModified;
	}

	public void uninstall(final ArrayList<Mod> installedMods) {
		
		patched = false;
		
		if (!installed) {
			return;
		}

		Configuration.addProperty("mods", file, "false");
		installed = false;
		
		if (conflictsWithInstalledMods(installedMods)) {
		
			try {
				createModPatch(installedMods);
			} catch (IOException e) {
				Configuration.printException(e, "Creating mod patch.");
			}
		
		}
		
		if(new File(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + internalName).exists()) {
			
			try {
				FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + modInfoName.substring(0, modInfoName.indexOf(".modinfo")));
			} catch (IOException e) {
				Configuration.printException(e, "Deleting installed mod folder when uninstalling.");
			}
		
		}
		
		updateStyles();
		
	}

	public void install(final ArrayList<Mod> installedMods) {
		
		try {
			
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + file);
			
			modArchive.extractAll(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + modInfoName.substring(0, modInfoName.indexOf(".modinfo")));
			
		} catch (ZipException e) {
			Configuration.printException(e, "Extracting mod folder when installing.");
		}
		
		if (subDirectory != null) {
			
			String tempInstallPath = Configuration.modsInstallFolder.getAbsolutePath() + File.separator + modInfoName.substring(0, modInfoName.indexOf(".modinfo")) + File.separator + subDirectory;
			
			try {
				FileHelper.copyDirectory(tempInstallPath, Configuration.modsInstallFolder.getAbsolutePath() + File.separator + modInfoName.substring(0, modInfoName.indexOf(".modinfo")));
				tempInstallPath = tempInstallPath.substring(0, tempInstallPath.indexOf(subDirectory) + subDirectory.indexOf("/"));
				FileHelper.deleteFile(tempInstallPath);
			} catch (IOException e) {
				Configuration.printException(e, "Copying installed mod subdirectory to main directory.");
			}
			
		}
		
		installed = true;
		
		try {
			Database.updateMod(this);
		} catch (SqlJetException e1) {
			Configuration.printException(e1);
		}

		updateStyles();
		
		if (conflictsWithInstalledMods(installedMods)) {
			try {
				createModPatch(installedMods);
			} catch (IOException e) {
				Configuration.printException(e, "Creating mod patch.");
			}
		}
		
	}
	
	private boolean conflictsWithInstalledMods(final ArrayList<Mod> installedMods) {

		ArrayList<Mod> tempModList = new ArrayList<Mod>(installedMods);
		ArrayList<String> conflictingFiles = new ArrayList<String>();
		
		tempModList.remove(this);
		
		for (Mod mod : tempModList) {
			conflictingFiles.addAll(mod.filesModified);
		}
		
		conflictingFiles.retainAll(filesModified);
		
		return (conflictingFiles.size() != 0);
		
	}
	
	private void createModPatch(final ArrayList<Mod> installedMods) throws IOException {
		
		if (installedMods.size() <= 1) {
			
			//No patches needed, delete the patches folder.
			if (Configuration.modsPatchesFolder.exists()) {
				FileHelper.deleteFile(Configuration.modsPatchesFolder);
			}
			
			for (Mod mod : installedMods) {
				
				try {
					ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + mod.file);
					modArchive.extractAll(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
				} catch (ZipException e) {
					Configuration.printException(e, "Last installed mod in patch gets put in its own folder again.");
				}
				
				if (mod.subDirectory != null) {
					try {
						FileHelper.copyDirectory(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.subDirectory + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")), Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
						FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.subDirectory + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
					} catch (IOException e) {
						Configuration.printException(e, "Copying installed mod subdirectory to main directory during patch.");
					}
				}
				
			}
			
			return;
			
		}
		
		Collections.reverse(installedMods);
		
		for (Mod mod : installedMods) {

			try {
				ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + mod.file);
				modArchive.extractAll(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
			} catch (ZipException e) {
				Configuration.printException(e, "Copying temporary folders and files for mod merging.");
			}
			
			if (mod.subDirectory != null) {
				try {
					FileHelper.copyDirectory(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.subDirectory + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")), Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
					FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.subDirectory + File.separator + mod.modInfoName.substring(0, mod.modInfoName.indexOf(".modinfo")));
				} catch (IOException e) {
					Configuration.printException(e, "Copying installed mod subdirectory to main directory during patch.");
				}
			}
			
		}
		
		HashSet<Mod> conflictingMods = new HashSet<Mod>();
		
		//Find all conflicting files in currently installed mods.
		HashMap<String, Integer> fileConflictsTemp = new HashMap<String, Integer>();
		
		for (Mod mod : installedMods) {
			for (String file : mod.filesModified) {
				if (fileConflictsTemp.containsKey(file)) {
					fileConflictsTemp.put(file, fileConflictsTemp.get(file) + 1);
				} else {
					fileConflictsTemp.put(file, 1);
				}
			}
		}
		
		HashSet<String> fileConflicts = new HashSet<String>();
		
		for (String s : fileConflictsTemp.keySet()) {
			if (fileConflictsTemp.get(s) > 1) {
				fileConflicts.add(s);
			}
		}
		
		HashSet<String> toRemove = new HashSet<String>();
		
		//Purge ignored file extensions.
		for (String s : Configuration.fileTypesToIgnore) {
	        for (String file : fileConflicts) {
	        	
	        	if (!new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "assets" + File.separator + file).exists()) {
	        		toRemove.add(file);
	        	}
	        	
	        	if (file.endsWith(s)) {
                    toRemove.add(file);
                }
	        	
	        }
		}
		 
		fileConflicts.removeAll(toRemove);
		
		if (fileConflicts.isEmpty()) {
			FileHelper.deleteFile(Configuration.modsPatchesFolder.getAbsolutePath());
			return;
		}
		
		//Delete and re-create the patches folder.
		if (Configuration.modsPatchesFolder.exists()) {
			FileHelper.deleteFile(Configuration.modsPatchesFolder);
		}
		
		Configuration.modsPatchesFolder.mkdir();
		
		//For each file, get each mod that edits that file.
		//Then get the original file and all mods' files and merge them.
		//Finally, save the merged file in the patched directory.
		ArrayList<Mod> currentMods = new ArrayList<Mod>();
		
		for (String file : fileConflicts) {
			
			currentMods.clear();
			
			for (Mod mod : installedMods) {
				if (mod.filesModified.contains(file)) {
					currentMods.add(mod);
					conflictingMods.add(mod);
				}
			}
			
			String originalFile = FileHelper.fileToString(new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "assets" + File.separator + file));
			
			diff_match_patch dpm = new diff_match_patch();
			LinkedList<Patch> patchesToApply = new LinkedList<Patch>();
			
			for (int i = 0; i < currentMods.size(); i++) {
				
				Mod mod = currentMods.get(i);
				
				String filePath = "";
				
				if (mod.assetsPath.isEmpty()) {
					filePath = Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.internalName + File.separator + file.replace("/", "\\");
				} else {
					filePath = Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.internalName + File.separator + mod.assetsPath.substring(2) + File.separator + file.replace("/", "\\");
				}
				
				if (i != currentMods.size() - 1) {
					LinkedList<Diff> diff = dpm.diff_main(originalFile, FileHelper.fileToString(new File(filePath)));
					LinkedList<Patch> patches = dpm.patch_make(diff);
					patchesToApply.addAll(patches);
				} else {
					originalFile = (String) dpm.patch_apply(patchesToApply, FileHelper.fileToString(new File(filePath)))[0];
				}
				
			}
			
			new File(new File(Configuration.modsPatchesFolder.getAbsolutePath() + File.separator + "assets" + File.separator + file).getParent()).mkdirs();
			
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(Configuration.modsPatchesFolder.getAbsolutePath() + File.separator + "assets" + File.separator + file), "UTF-8");
			
			out.append(originalFile);
			out.flush();
			out.close();
			
		}
		
		for (Mod mod : conflictingMods) {
			
			try {
				
				FileHelper.deleteFile("tempFolder" + File.separator);
				
				ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + mod.file);
				modArchive.extractAll(new File("tempFolder" + File.separator).getAbsolutePath());

				ArrayList<File> files = new ArrayList<File>();
				
				if (mod.assetsPath.isEmpty()) {
					FileHelper.listFiles("tempFolder" + File.separator, files);
				} else {
					FileHelper.listFiles("tempFolder" + File.separator + mod.assetsPath.substring(2), files);
				}
				
				ArrayList<File> remove = new ArrayList<File>();
				
				String toFindPath = "";
				
				if (mod.assetsPath.isEmpty()) {
					toFindPath = new File("tempFolder" + File.separator).getAbsolutePath() + File.separator;
				} else {
					toFindPath = new File("tempFolder" + File.separator + mod.assetsPath.substring(2)).getAbsolutePath() + File.separator;
				}

				for (File file : files) {
					
					String fileName = file.getAbsolutePath().substring(toFindPath.length());
					fileName = fileName.replace("\\", "/");
					
					if (mod.subDirectory != null) {
						fileName = fileName.substring((mod.subDirectory + File.separator).length());
					}
					
					if (fileConflicts.contains(fileName)) {
						remove.add(file);
					}
					
					if (fileName.endsWith(".modinfo")) {
						remove.add(file);
					}
					
				}
				
				files.removeAll(remove);
				
				for (File file : files) {
					
					String fileName = file.getAbsolutePath().substring(toFindPath.length());
					
					if (mod.subDirectory != null) {
						fileName = fileName.substring((mod.subDirectory + File.separator).length());
					}
					
					File outputFile = new File(Configuration.modsPatchesFolder.getAbsolutePath() + File.separator + "assets" + File.separator + fileName);
					outputFile.getParentFile().mkdirs();
					
					FileHelper.copyFile(file, outputFile);
					
				}
				
			} catch (ZipException e) {
				Configuration.printException(e, "Extracting mod folder when patching.");
			}
			
			mod.updateStyles();
			
		}

		for (Mod mod : conflictingMods) {
			FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + mod.internalName);
		}
		
		FileHelper.deleteFile("tempFolder" + File.separator);
		
		FileWriter writer = new FileWriter(new File(Configuration.modsPatchesFolder.getAbsolutePath() + File.separator + Configuration.modsPatchesFolder.getName() + ".modinfo"));
		
		writer.append(
				"{\r\n" +
				"  \"name\" : \"" + Configuration.modsPatchesFolder.getName() + "\",\r\n" +
				"  \"version\" : \"" + Configuration.gameVersionString + "\",\r\n" +
				"  \"path\" : \"./assets\",\r\n" +
				"  \"metadata\" : {\r\n" +
				"    \"name\" : \"A Test Mod\",\r\n" +
				"    \"author\" : \"KrazyTheFox' Mod Manager\",\r\n" +
				"    \"description\" : \"This is a patch of all conflicting mods. Do not modify by hand unless you know what you're doing.\",\r\n" +
				"    \"support_url\" : \"http://community.playstarbound.com/index.php?threads/starbound-mod-manager.51639/\",\r\n" +
				"    \"version\" : \"1.0\"\r\n" +
				"  }\r\n" +
				"}");
		
		writer.close();
		
		new FXDialogueConfirm("Patch generated for conflicting mods.").show();
		
	}
	
	// TODO: Replace this boolean with a check for folder + modinfo in mods directory
	@SuppressWarnings("rawtypes")
	public static Mod loadMod(String fileName, boolean installed) {
		
		Mod mod = new Mod();
		
		mod.file = fileName;
		mod.installed = installed;
		
		mod.modInfoName = "";
		
		try {
			
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + mod.file);

			@SuppressWarnings("unchecked")
			List<FileHeader> fileHeaders = modArchive.getFileHeaders();
			
			for (FileHeader fileHeader : fileHeaders) {
				if (fileHeader.getFileName().endsWith(".modinfo")) {
					modArchive.extractFile(fileHeader, new File("").getAbsolutePath());
					mod.modInfoName = fileHeader.getFileName();
					if (mod.modInfoName.contains("/")) {
						mod.subDirectory = mod.modInfoName.substring(0, mod.modInfoName.lastIndexOf('/'));
						mod.modInfoName = mod.modInfoName.substring(mod.modInfoName.lastIndexOf('/') + 1);
					} else {
						mod.subDirectory = null;
					}
					break;
				}
			}
			
		} catch (ZipException e) {
			Configuration.printException(e, "Searching for mod info in archive: " + mod.file);
		}
		
		if (mod.modInfoName.isEmpty()) {
			new FXDialogueConfirm("Mod \"" + mod.file + "\" is missing a valid .modinfo file.\nPlease contact the creator of this mod for help.").show();
			return null;
		}
		
		try {
			
			Map<?, ?> map;
			
			if (mod.subDirectory == null) {
				map = JsonReader.jsonToMaps(FileHelper.fileToJSON(new File(mod.modInfoName)));
			} else {
				map = JsonReader.jsonToMaps(FileHelper.fileToJSON(new File(mod.subDirectory + File.separator + mod.modInfoName)));
			}
			
			for (Object e : map.keySet()) {
				
				String value = e.toString();
				
				if (value.equalsIgnoreCase("name")) {
					
					mod.internalName = map.get(e).toString();
					
				} else if (value.equalsIgnoreCase("path")) {
					
					if (map.get(value).equals(".")) {
						mod.assetsPath = "";
					} else {
						mod.assetsPath = map.get(value).toString();
					}
					
				} else if (value.equalsIgnoreCase("version")) {
					
					mod.gameVersion = map.get(value).toString();
					
				} else if (value.equalsIgnoreCase("metadata")) {
					
					for (Object e2 : ((JsonObject) map.get(value)).entrySet()) {
						
						String metaValue = e2.toString();
						
						if (metaValue.split("=").length < 2) {
							continue;
						}
						
						String key = metaValue.split("=")[0];
						String val = metaValue.split("=")[1];
						
						if (key.equalsIgnoreCase("author")) {
							mod.author = val;
						} else if (key.equalsIgnoreCase("version")) {
							mod.version = val;
						} else if (key.equalsIgnoreCase("displayname")) {
							mod.displayName = val;
						} else if (key.equalsIgnoreCase("description")) {
							mod.description = val;
						}
						
					}
					
				}
				
			}
			
		} catch (IOException e) {
			new FXDialogueConfirm("Mod \"" + mod.file + "\" is missing a valid .modinfo file.\nPlease contact the creator of this mod for help.").show();
			Configuration.printException(e, "Reading mod info file to JSON: " + mod.modInfoName);
			return null;
		}
		
		/*if (!mod.modInfoName.replace(".modinfo", "").toLowerCase().equals(mod.internalName.toLowerCase())) {
			//new FXDialogueConfirm("Mod \"" + mod.file + "\"'s name in its .modinfo file must be the same as the .modinfo file name.\nPlease contact the creator of this mod for help.").show();
			Configuration.printException(new Exception("\"" + mod.modInfoName + "\" does not match \"" + mod.internalName + "\""), "Internal name vs. Modinfo name.");
			//return null;
		}*/
		
		try {
			if (mod.subDirectory != null) {
				FileHelper.deleteFile(new File(mod.subDirectory));
			} else {
				FileHelper.deleteFile(new File(mod.modInfoName));
			}
		} catch (IOException e) {
			Configuration.printException(e, "Deleting temporary .modinfo file.");
		}
		
		if (mod.displayName == null) {
			mod.displayName = mod.file.substring(0, mod.file.indexOf(".zip"));
		}
		
		if (mod.version == null) {
			mod.version = "???";
		}
		
		if (mod.author == null) {
			mod.author = "???";
		}
		
		try {
			
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + mod.file);
			
			@SuppressWarnings("unchecked")
			List<FileHeader> fileHeaders = modArchive.getFileHeaders();
			
			String fileToLookFor = "";
			
			if (!mod.assetsPath.isEmpty()) {
				fileToLookFor = mod.assetsPath.substring(2) + File.separator;
			}

			for(FileHeader fileHeader : fileHeaders) {
				if (!fileHeader.isDirectory() && fileHeader.getFileName().contains(fileToLookFor)) {
					
					String toAdd = fileHeader.getFileName().substring(fileHeader.getFileName().indexOf(fileToLookFor) + fileToLookFor.length());
					
					if (mod.subDirectory != null) {
						toAdd = toAdd.substring((mod.subDirectory + File.separator).length());
					}
					
					boolean isJSONFile = true;
					boolean isModified = true;
					
					for (String extension : Configuration.fileTypesToIgnore) {
						if (toAdd.endsWith(extension)) {
							isJSONFile = false;
						}
					}
					
					if (isJSONFile) {

						modArchive.extractFile(fileHeader, new File("temp" + File.separator).getAbsolutePath());
						
						String fileContents = "";
						String fileLocation = "";
						
						if (mod.subDirectory != null) {
							fileLocation = "temp" + File.separator + fileToLookFor + mod.subDirectory + File.separator + toAdd;
						} else {
							fileLocation = "temp" + File.separator + fileToLookFor + toAdd;
						}
						
						fileContents = FileHelper.fileToJSON(new File(fileLocation));
						
						if (fileContents.contains("\"__merge\"")) {
							isModified = false;
						}
						
						FileHelper.deleteFile(new File("temp"));
						
					}
					
					if (isModified) {
						mod.filesModified.add(toAdd);
					}
					
				}
			}
			
		} catch (ZipException | IOException e) {
			Configuration.printException(e, "Locating assets folder in archive.");
		}
		
		try {
			Database.addMod(mod);
		} catch (SqlJetException e) {
			Configuration.printException(e, "Adding mod to database.");
		}
		
		/*if (mod.filesModified.size() == 0) {
			new FXDialogueConfirm("Cannot find assets folder for: \"" + mod.file + "\".\nPlease contact the creator of this mod for help.\n\nCommon causes are:\n - Incorrect folder structures\n - Incorrect \"assets\" path in the .modinfo file").show();
			return null;
		}*/

		mod.container = new VBox();
		
		mod.gridPane = new GridPane();
		mod.gridPane.setAlignment(Pos.CENTER);
		
		mod.gridPane.setMinHeight(46);
		mod.gridPane.setPadding(new Insets(0, 0, 0, 15));
		
		mod.bottomStroke = new Rectangle(350, 2);
		mod.bottomStroke.widthProperty().bind(mod.container.widthProperty());
		
		mod.container.getChildren().addAll(mod.gridPane, mod.bottomStroke);
		
		mod.modName = new Text(mod.displayName);
		mod.modName.getStyleClass().add("modname");
		mod.gridPane.add(mod.modName, 0, 0, 1, 2);
		
		mod.modVersion = new Text("v" + mod.version);
		mod.modVersion.getStyleClass().add("modversion");
		GridPane.setValignment(mod.modVersion, VPos.BOTTOM);
		mod.gridPane.add(mod.modVersion, 1, 0);
		
		mod.modAuthor = new Text(mod.author);
		mod.modAuthor.getStyleClass().add("modauthor");
		GridPane.setValignment(mod.modVersion, VPos.TOP);
		mod.gridPane.add(mod.modAuthor, 1, 1);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setFillWidth(true);
		col1.setHgrow(Priority.ALWAYS);
		mod.gridPane.getColumnConstraints().add(col1);
		
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.RIGHT);
		mod.gridPane.getColumnConstraints().add(col2);
		
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(42);
		col3.setMaxWidth(42);
		col3.setHalignment(HPos.CENTER);
		mod.gridPane.getColumnConstraints().add(col3);
		
		mod.updateStyles();
		
		return mod;
		
	}
	
}