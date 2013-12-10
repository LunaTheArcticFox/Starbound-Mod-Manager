package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class Mod {
	
	private static final String installedImage = application.Mod.class.getResource("installed.png").toExternalForm();
	private static final String notInstalledImage = application.Mod.class.getResource("notinstalled.png").toExternalForm();
	private static final String conflictImage = application.Mod.class.getResource("conflict.png").toExternalForm();
	//private static final String outdatedImage = Mod.class.getClassLoader().getResource("outdated.png").toExternalForm();
	
	public ArrayList<String> filesModified = new ArrayList<String>();
	public boolean installed = false;
	public boolean selected = false;
	public boolean hasConflicts = false;
	public boolean hasAssetsFolder = false;
	
	public String file;
	public String name;
	public String author;
	public String version;
	
	public VBox container;
	public GridPane gridPane;
	public Rectangle bottomStroke;
	public Text modName;
	public Text modAuthor;
	public Text modVersion;
	public CenteredRegion modStatus;
	
	public int order = 0;
	
	public Mod(String file, boolean installed) {
		
		this.file = file;
		this.installed = installed;
		
		load();
		
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setName(String name) {
		this.name = name;
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
		
		if (hasConflicts && !installed) {
			gridPane.getStyleClass().add("conflicted-mod-fill");
			bottomStroke.getStyleClass().add("conflicted-mod-stroke");
			modName.getStyleClass().add("conflicted-font-color");
			modVersion.getStyleClass().add("conflicted-font-color");
			modAuthor.getStyleClass().add("conflicted-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(conflictImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (installed) {
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
	
	public void uninstall() {
		
		if (!installed) {
			return;
		}
		
		if(new File(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name).exists()) {
			
			try {
				FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ArrayList<String> bootstrapData = new ArrayList<String>();
	
			try {
				
				BufferedReader config = new BufferedReader(new FileReader(Configuration.bootstrapFile));
				String line;
				
				while ((line = config.readLine()) != null) {
					bootstrapData.add(line);
				}
				
				config.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				
				FileWriter bootstrapOutput = new FileWriter(Configuration.bootstrapFile);
				
				for (String line : bootstrapData) {
					
					if (!line.contains(name)) {
						bootstrapOutput.append(line + "\r\n");
					}
					
				}
				
				bootstrapOutput.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}

		Configuration.addProperty("mods", file, "false");
		installed = false;

		updateStyles();
		
	}

	public void install(final ArrayList<Mod> installedMods) {
		
		try {
			
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + file);
			
			modArchive.extractAll(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name);
			
			if (hasAssetsFolder) {
				FileHelper.copyFolder(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name + File.separator + "assets", Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name + File.separator);
				FileHelper.deleteFile(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name + File.separator + "assets");
			}
			
			File f = new File(Configuration.modsInstallFolder.getAbsolutePath() + File.separator + name + File.separator + "mod.dat");
			
			if (f.exists()) {
				FileHelper.deleteFile(f);
			}
			
		} catch (ZipException | IOException e) {
			e.printStackTrace();
		}
		
		Configuration.updateBootstrap(installedMods);
		
		Configuration.addProperty("mods", file, "true");
		installed = true;

		updateStyles();
		
	}
	
	private void load() {
		
		try {
			
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + file);
			modArchive.extractFile("mod.dat", new File("").getAbsolutePath());
			
		} catch (ZipException e) {
			System.err.println("Could not locate mod.dat in '" + file + "'. Using archive name instead.");
		}

		try {
				
			ZipFile modArchive = new ZipFile(Configuration.modsFolder.getAbsolutePath() + File.separator + file);

			for (Object o : modArchive.getFileHeaders()) {
				if (((FileHeader) o).getFileName().equals("assets/")) {
					hasAssetsFolder = true;
				}
			}
			
			for (Object o : modArchive.getFileHeaders()) {
				if (!((FileHeader) o).isDirectory() && !((FileHeader) o).getFileName().equals("mod.dat")) {
					filesModified.add(((FileHeader) o).getFileName().replace("assets/", ""));
				}
			}
			
		} catch (ZipException e) {
			System.err.println("Could not read '" + file + "' contents.");
		}
		
		try {
			
			BufferedReader dat = new BufferedReader(new FileReader("mod.dat"));
			String line;
			
			while ((line = dat.readLine()) != null) {
				
				if (line.startsWith("name:")) {
					name = line.replace("name:", "");
				} else if (line.startsWith("author:")) {
					author = line.replace("author:", "");
				} else if (line.startsWith("version:")) {
					version = line.replace("version:", "");
				}
				
			}
			
			dat.close();
			
		} catch (IOException e) {
		}
		
		try {
			FileHelper.deleteFile(new File("mod.dat"));
		} catch (IOException e) {
		}
		
		if (name == null) {
			name = file;
		}
		
		if (version == null) {
			version = "???";
		}
		
		if (author == null) {
			author = "???";
		}
		
		container = new VBox();
		
		gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		
		gridPane.setMinHeight(46);
		gridPane.setPadding(new Insets(0, 0, 0, 15));
		
		bottomStroke = new Rectangle(350, 2);
		bottomStroke.widthProperty().bind(container.widthProperty());
		
		container.getChildren().addAll(gridPane, bottomStroke);
		
		modName = new Text(name);
		modName.getStyleClass().add("modname");
		gridPane.add(modName, 0, 0, 1, 2);
		
		modVersion = new Text("v" + version);
		modVersion.getStyleClass().add("modversion");
		GridPane.setValignment(modVersion, VPos.BOTTOM);
		gridPane.add(modVersion, 1, 0);
		
		modAuthor = new Text(author);
		modAuthor.getStyleClass().add("modauthor");
		GridPane.setValignment(modVersion, VPos.TOP);
		gridPane.add(modAuthor, 1, 1);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setFillWidth(true);
		col1.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().add(col1);
		
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.RIGHT);
		gridPane.getColumnConstraints().add(col2);
		
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(42);
		col3.setMaxWidth(42);
		col3.setHalignment(HPos.CENTER);
		gridPane.getColumnConstraints().add(col3);
		
		updateStyles();
		
	}
	
}