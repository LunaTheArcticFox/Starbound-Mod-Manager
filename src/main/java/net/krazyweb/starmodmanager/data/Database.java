package main.java.net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import main.java.net.krazyweb.helpers.FileHelper;

import org.apache.log4j.Logger;


public class Database extends Observable implements Progressable {
	
	private static final Logger log = Logger.getLogger(Database.class);
	
	private static final String MOD_TABLE_NAME = "mods";
	private static final String SETTINGS_TABLE_NAME = "settings";
	
	private static Connection connection;

	private static Database instance;

	private Task<?> task;
	
	private ReadOnlyDoubleProperty progress;
	private ReadOnlyStringProperty message;
	
	private Database() {
		
	}
	
	public static Database getInstance() {
		if (instance == null) {
			synchronized (Database.class) {
				instance = new Database();
			}
		}
		return instance;
	}
	
	public void initialize() {
		
		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Connecting to Database");
				this.updateProgress(0.0, 2.0);
				
				connection = DriverManager.getConnection("jdbc:hsqldb:file:" + new File("").getAbsolutePath().replaceAll("\\\\", "/") + "/data/db", "SA", "");

				this.updateMessage("Creating Default Tables");
				this.updateProgress(1.0, 2.0);

				createTables();
				
				this.updateProgress(2.0, 2.0);
				
				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				setChanged();
				notifyObservers("databaseinitialized");
			}
		});
		
		this.setProgress(task.progressProperty());
		this.setMessage(task.messageProperty());
		
	}
	
	private static void createTables() throws SQLException {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(MOD_TABLE_NAME);
		sb.append(" (");
		sb.append("internalName VARCHAR(255) NOT NULL, ");
		sb.append("archiveName VARCHAR(255) NOT NULL, ");
		sb.append("displayName VARCHAR(255) NOT NULL, ");
		sb.append("modVersion VARCHAR(255) DEFAULT NULL, ");
		sb.append("gameVersion VARCHAR(255) NOT NULL, ");
		sb.append("author VARCHAR(255) DEFAULT NULL, ");
		sb.append("description VARCHAR(65535) DEFAULT NULL, ");
		sb.append("url VARCHAR(255) DEFAULT NULL, ");
		sb.append("checksum BIGINT NOT NULL, ");
		sb.append("loadOrder INT NOT NULL, ");
		sb.append("hidden BIT NOT NULL, ");
		sb.append("installed BIT NOT NULL, ");
		sb.append("dependencies VARCHAR(16777215) DEFAULT NULL, "); //"internalName\ninternalName\ninternalName"
		sb.append("files VARCHAR(16777215) NOT NULL, "); //"filepath::json::ignored::automerge\nfilepath::json::ignored::automerge"
		sb.append("PRIMARY KEY (internalName)");
		sb.append(");");

		Statement tableCreator = connection.createStatement();
		tableCreator.execute(sb.toString());

		log.debug("'" + sb.toString() + "' executed.");
		
		sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(SETTINGS_TABLE_NAME);
		sb.append(" (");
		sb.append("property VARCHAR(65535) NOT NULL, ");
		sb.append("value VARCHAR(65535) NOT NULL, ");
		sb.append("PRIMARY KEY (property)");
		sb.append(");");

		tableCreator.execute(sb.toString());
		
		tableCreator.closeOnCompletion();
		
		log.debug("'" + sb.toString() + "' executed.");
		
	}
	
	/* 
	 * updateMod() is used for both updating and adding mods.
	 * It picks the right one for the mod in question.
	 */
	public static void updateMod(final Mod mod) throws SQLException {
		
		StringBuilder dependencyList = new StringBuilder();
		
		for (String dependency : mod.getDependencies()) {
			dependencyList.append(dependency).append("\n");
		}
		
		StringBuilder fileList = new StringBuilder();
		
		for (ModFile file : mod.getFiles()) {
			fileList
			.append(file.getPath()).append(":::")
			.append(file.isJson()).append(":::")
			.append(file.isIgnored()).append(":::")
			.append(file.isAutoMerged()).append("\n");
		}
		
		StringBuilder query = new StringBuilder();

		if (containsMod(mod)) {

			query.append("UPDATE ");
			query.append(MOD_TABLE_NAME);
			query.append(" SET ");
			
			query.append("internalName = ?,");
			query.append("archiveName = ?,");
			query.append("displayName = ?,");
			query.append("modVersion = ?,");
			query.append("gameVersion = ?,");
			query.append("author = ?,");
			query.append("description = ?,");
			query.append("url = ?,");
			query.append("checksum = ?,");
			query.append("loadOrder = ?,");
			query.append("hidden = ?,");
			query.append("installed = ?,");
			query.append("dependencies = ?,");
			query.append("files = ?");
			query.append("WHERE internalName = ?");
			
			PreparedStatement statement = connection.prepareStatement(query.toString());
			
			statement.setString(1, mod.getInternalName());
			statement.setString(2, mod.getArchiveName());
			statement.setString(3, mod.getDisplayName());
			statement.setString(4, mod.getModVersion());
			statement.setString(5, mod.getGameVersion());
			statement.setString(6, mod.getAuthor());
			statement.setString(7, mod.getDescription());
			statement.setString(8, mod.getURL());
			statement.setLong(9, mod.getChecksum());
			statement.setInt(10, mod.getOrder());
			statement.setInt(11, mod.isHidden() ? 1 : 0);
			statement.setInt(12, mod.isInstalled() ? 1 : 0);
			statement.setString(13, dependencyList.toString());
			statement.setString(14, fileList.toString());
			statement.setString(15, mod.getInternalName());
			
			statement.executeUpdate();
			log.trace("Statement Executed: " + statement.toString());
			statement.closeOnCompletion();
			
		} else {
			
			query.append("INSERT INTO ");
			query.append(MOD_TABLE_NAME).append("(");
			query.append("internalName,");
			query.append("archiveName,");
			query.append("displayName,");
			query.append("modVersion,");
			query.append("gameVersion,");
			query.append("author,");
			query.append("description,");
			query.append("url,");
			query.append("checksum,");
			query.append("loadOrder,");
			query.append("hidden,");
			query.append("installed,");
			query.append("dependencies,");
			query.append("files");
			query.append(") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			
			PreparedStatement statement = connection.prepareStatement(query.toString());
			
			statement.setString(1, mod.getInternalName());
			statement.setString(2, mod.getArchiveName());
			statement.setString(3, mod.getDisplayName());
			statement.setString(4, mod.getModVersion());
			statement.setString(5, mod.getGameVersion());
			statement.setString(6, mod.getAuthor());
			statement.setString(7, mod.getDescription());
			statement.setString(8, mod.getURL());
			statement.setLong(9, mod.getChecksum());
			statement.setInt(10, mod.getOrder());
			statement.setInt(11, mod.isHidden() ? 1 : 0);
			statement.setInt(12, mod.isInstalled() ? 1 : 0);
			
			if (dependencyList.toString().isEmpty()) {
				statement.setString(13, "NULL");
			} else {
				statement.setString(13, dependencyList.toString());
			}

			statement.setString(14, fileList.toString());
			
			statement.execute();
			log.trace("Statement Executed: " + statement.toString());
			statement.closeOnCompletion();
			
		}
		
	}
	
	public static void deleteMod(final Mod mod) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = '").append(mod.getInternalName()).append("';");
		
		Statement delete = connection.createStatement();
		delete.execute(query.toString());
		log.trace("Statement Executed: " + query.toString());
		delete.closeOnCompletion();
		
	}
	
	private static boolean containsMod(final Mod mod) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT internalName FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, mod.getInternalName());
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: " + query.toString());
		
		if (!hasRows(results)) {
			results.close();
			modQuery.close();
			return false;
		}
		
		results.close();
		modQuery.close();
		
		return true;
		
	}
	
	private static boolean containsProperty(final String property) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT property FROM ");
		query.append(SETTINGS_TABLE_NAME);
		query.append(" WHERE property = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, property);
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: " + query.toString());
		
		if (!hasRows(results)) {
			results.close();
			modQuery.close();
			return false;
		}
		
		results.close();
		modQuery.close();
		
		return true;
		
	}
	
	protected List<String> getModNames() throws SQLException {

		List<String> output = new ArrayList<>();
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		
		ResultSet results = modQuery.executeQuery();

		if (hasRows(results)) {
			while (results.next()) {
				output.add(results.getString("internalName") + "\n" + results.getString("archiveName"));
			}
		}
		
		return output;
		
	}
	
	protected List<Mod> getModByName(final String modName) throws SQLException {
		
		List<Mod> output = new ArrayList<>();
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, modName);
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: " + query.toString());
		
		if (hasRows(results)) {
			
			log.debug("Mod found in database: " + modName);
			
			results.next();
				
			Mod mod = new Mod();
			
			mod.setInternalName(results.getString("internalName"));
			mod.setArchiveName(results.getString("archiveName"));
			mod.setDisplayName(results.getString("displayName"));
			mod.setModVersion(results.getString("modVersion"));
			mod.setGameVersion(results.getString("gameVersion"));
			mod.setAuthor(results.getString("author"));
			mod.setDescription(results.getString("description"));
			mod.setURL(results.getString("url"));
			mod.setChecksum(results.getLong("checksum"));
			mod.setOrder(results.getInt("loadOrder"));
			mod.setHidden(results.getInt("hidden") == 1);
			mod.setInstalled(results.getInt("installed") == 1);
			
			Set<String> dependencies = new HashSet<>();
			
			for (String data : results.getString("dependencies").split("\n")) {
				dependencies.add(data);
			}
			
			mod.setDependencies(dependencies);
			
			Set<ModFile> files = new HashSet<>();
			
			for (String data : results.getString("files").split("\n")) {
				
				String[] fields = data.split(":::");
				
				ModFile file = new ModFile();
				
				file.setPath(Paths.get(fields[0]));
				file.setJson(Boolean.parseBoolean(fields[1]));
				file.setIgnored(Boolean.parseBoolean(fields[2]));
				file.setAutoMerged(Boolean.parseBoolean(fields[3]));
				
				files.add(file);
				
			}
			
			mod.setFiles(files);
			
			//TODO Paths, Files
			if (!new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).exists()) {
				deleteMod(mod);
				return null;
			}
			
			Set<Mod> mods = null;
			
			try {
				
				long checksum = FileHelper.getChecksum(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).toPath());
				
				if (mod.getChecksum() != checksum) {
					log.debug("Mod file checksum mismatch: " + mod.getArchiveName() + " (" + mod.getChecksum() + ")");
					//TODO Get path instead of using File
					mods = Mod.load(Paths.get(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).getPath()), mod.getOrder());
				} else {
					mods = new HashSet<>();
					mods.add(mod);
				}
				
			} catch (IOException e) {
				log.error("", e); //TODO Better error message
			}
			
			if (mods != null && !mods.isEmpty()) {
				for (Mod m : mods) {
					output.add(m);
				}
			}

		}
		
		results.close();
		modQuery.closeOnCompletion();
		
		return output;
		
	}
	
	protected List<Mod> getModList() throws SQLException {
		
		List<Mod> modList = new ArrayList<Mod>();

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: " + query.toString());
		
		if (hasRows(results)) {
			
			log.debug("Mods found in database: ");
			
			while (results.next()) {
				
				Mod mod = new Mod();
				
				mod.setInternalName(results.getString("internalName"));
				mod.setArchiveName(results.getString("archiveName"));
				mod.setDisplayName(results.getString("displayName"));
				mod.setModVersion(results.getString("modVersion"));
				mod.setGameVersion(results.getString("gameVersion"));
				mod.setAuthor(results.getString("author"));
				mod.setDescription(results.getString("description"));
				mod.setURL(results.getString("url"));
				mod.setChecksum(results.getLong("checksum"));
				mod.setOrder(results.getInt("loadOrder"));
				mod.setHidden(results.getInt("hidden") == 1);
				mod.setInstalled(results.getInt("installed") == 1);
				
				log.debug(" -> " + mod.getInternalName());
				
				Set<String> dependencies = new HashSet<>();
				
				for (String data : results.getString("dependencies").split("\n")) {
					dependencies.add(data);
				}
				
				mod.setDependencies(dependencies);
				
				Set<ModFile> files = new HashSet<>();
				
				for (String data : results.getString("files").split("\n")) {
					
					String[] fields = data.split(":::");
					
					ModFile file = new ModFile();
					
					file.setPath(Paths.get(fields[0]));
					file.setJson(Boolean.parseBoolean(fields[1]));
					file.setIgnored(Boolean.parseBoolean(fields[2]));
					file.setAutoMerged(Boolean.parseBoolean(fields[3]));
					
					files.add(file);
					
				}
				
				mod.setFiles(files);
				
				if (!new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).exists()) {
					deleteMod(mod);
					continue;
				}
				
				Set<Mod> mods = null;
				
				try {
					
					long checksum = FileHelper.getChecksum(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).toPath());
					
					if (mod.getChecksum() != checksum) {
						log.debug("Mod file checksum mismatch: " + mod.getArchiveName() + " (" + mod.getChecksum() + ")");
						//TODO Get path instead of using File
						mods = Mod.load(Paths.get(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.getArchiveName()).getPath()), mod.getOrder());
					} else {
						mods = new HashSet<>();
						mods.add(mod);
					}
					
				} catch (IOException e) {
					log.error("", e); //TODO Better error message
				}
				
				if (mods != null && !mods.isEmpty()) {
					for (Mod m : mods) {
						modList.add(m);
					}
				}
				
			}

		}
		
		results.close();
		modQuery.closeOnCompletion();
		
		getNewMods(modList);
		
		Collections.sort(modList, new Mod.ModOrderComparator());
		
		for (Mod mod : modList) {
			mod.setOrder(modList.indexOf(mod));
			log.debug("[" + mod.getOrder() + "] " + mod.getInternalName());
		}
		
		return modList;
		
	}
	
	private static String getSettingsValue(final String property) throws SQLException {

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT value FROM ");
		query.append(SETTINGS_TABLE_NAME);
		query.append(" WHERE property = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement propertyQuery = connection.prepareStatement(query.toString());
		propertyQuery.setString(1, property);
		
		log.trace("Statement Executed: " + propertyQuery.toString());
		
		ResultSet results = propertyQuery.executeQuery();
		
		String output = null;
		
		if (hasRows(results)) {
			while (results.next()) {
				output = results.getString(1);
				log.debug("'" + output + "' retrieved from database for property '" + property + "'.");
			}
		}
		
		results.close();
		propertyQuery.closeOnCompletion();
		
		return output;
		
	}
	
	protected Map<String, String> getProperties() throws SQLException {
		
		Map<String, String> properties = new HashMap<>();

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(SETTINGS_TABLE_NAME);
		
		PreparedStatement propertyQuery = connection.prepareStatement(query.toString());
		
		log.trace("Statement Executed: " + propertyQuery.toString());
		
		ResultSet results = propertyQuery.executeQuery();
		
		if (hasRows(results)) {
			while (results.next()) {
				properties.put(results.getString(1), results.getString(2));
				log.debug("'" + results.getString(2) + "' retrieved from database for property '" + results.getString(1) + "'.");
			}
		}
		
		results.close();
		propertyQuery.closeOnCompletion();
		
		return properties;
		
	}
	
	public static String getPropertyString(final String property, final String defaultValue) {
		
		String result = null;
		
		try {
			result = getSettingsValue(property);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		if (result == null) {
			return defaultValue;
		}
		
		return result;
		
	}
	
	public static int getPropertyInt(final String property, final int defaultValue) {
		return Integer.parseInt(getPropertyString(property, "" + defaultValue));
	}
	
	public static void setProperty(final String property, Object value) {

		StringBuilder query = new StringBuilder();
		
		try {
		
			if (containsProperty(property)) {
	
				query.append("UPDATE ");
				query.append(SETTINGS_TABLE_NAME);
				query.append(" SET ");
				query.append("value = ?");
				query.append("WHERE property = ?");
				
				PreparedStatement statement = connection.prepareStatement(query.toString());
				
				statement.setString(1, value.toString());
				statement.setString(2, property);
				
				statement.executeUpdate();
				log.trace("Statement Executed: " + statement.toString());
				statement.closeOnCompletion();
				
			} else {
			
				query.append("INSERT INTO ");
				query.append(SETTINGS_TABLE_NAME).append("(");
				query.append("property,");
				query.append("value");
				query.append(") VALUES(?, ?);");
				
				PreparedStatement statement = connection.prepareStatement(query.toString());
	
				statement.setString(1, property);
				statement.setString(2, value.toString());
				
				statement.executeUpdate();
				log.trace("Statement Executed: " + statement.toString());
				statement.closeOnCompletion();
			
			}
		
		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		log.debug("'" + property + "' -> '" + value + "' added to database.");
		
	}
	
	protected void getNewMods(final List<Mod> modList) {
		
		//Collect all filenames of already recognized mods
		Set<String> currentArchives = new HashSet<>();
		
		for (Mod mod : modList) {
			currentArchives.add(Paths.get(Settings.getInstance().getPropertyString("modsdir")).toAbsolutePath() + File.separator + mod.getArchiveName());
		}
		
		//List all the archives in the mods directory, then remove already recognized mods
		Set<Path> archives = new HashSet<>();
		Set<Path> toRemove = new HashSet<>();
		
		FileHelper.listFiles(Settings.getInstance().getPropertyString("modsdir"), archives);
		
		for (Path path : archives) {
			if (currentArchives.contains(path.toString())) {
				toRemove.add(path);
			}
		}
		
		archives.removeAll(toRemove);
		
		for (Path path : archives) {
			
			Set<Mod> mods = Mod.load(path, modList.size());
			
			if (mods == null || mods.isEmpty()) {
				continue;
			}
			
			for (Mod mod : mods) {
				modList.add(mod);
			}
			
		}
		
	}
	
	private static boolean hasRows(final ResultSet resultSet) throws SQLException {
		return resultSet.isBeforeFirst();
	}
	
	private void setProgress(final ReadOnlyDoubleProperty progress) {
		this.progress = progress;
	}
	
	private void setMessage(final ReadOnlyStringProperty message) {
		this.message = message; 
	}

	@Override
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progress;
	}

	@Override
	public ReadOnlyStringProperty getMessageProperty() {
		return message;
	}

	@Override
	public void processTask() {
		Thread thread = new Thread(task);
		thread.setName("Database Task Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
}