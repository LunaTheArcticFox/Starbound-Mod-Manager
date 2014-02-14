package net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import net.krazyweb.helpers.FileHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HyperSQLDatabase implements DatabaseModelInterface {
	
	private static final Logger log = LogManager.getLogger(HyperSQLDatabase.class);
	
	private static final String MOD_TABLE_NAME = "mods";
	private static final String SETTINGS_TABLE_NAME = "settings";
	
	private Connection connection;

	private SettingsModelInterface settings;
	private SettingsModelFactory settingsFactory;
	
	private Set<Observer> observers;
	
	protected HyperSQLDatabase(final SettingsModelFactory settingsFactory) {
		observers = new HashSet<>();
		this.settingsFactory = settingsFactory;
	}
	
	@Override
	public Task<Void> getInitializerTask() {
		
		final Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				settings = settingsFactory.getInstance();

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
				notifyObservers("databaseinitialized");
			}
		});
		
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", task.getException());
			}
		});
		
		return task;
		
	}
	
	@Override
	public Task<Void> getCloseTask() {

		final Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				connection.commit();
				connection.close();
				
				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				notifyObservers("databaseclosed");
			}
		});
		
		return task;
		
	}
	
	private void createTables() throws SQLException {
		
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

		log.debug("'{}' executed.", sb);
		
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

		log.debug("'{}' executed.", sb);
		
	}
	
	/* 
	 * updateMod() is used for both updating and adding mods.
	 * It picks the right one for the mod in question.
	 */
	@Override
	public void updateMod(final Mod mod) throws SQLException {
		
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
			log.trace("Statement Executed: {}", statement);
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
			log.trace("Statement Executed: {}", statement);
			statement.closeOnCompletion();
			
		}
		
	}
	
	@Override
	public void deleteMod(final Mod mod) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = '").append(mod.getInternalName()).append("';");
		
		Statement delete = connection.createStatement();
		delete.execute(query.toString());
		log.trace("Statement Executed: {}", query);
		delete.closeOnCompletion();
		
	}
	
	private boolean containsMod(final Mod mod) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT internalName FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, mod.getInternalName());
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: {}", query);
		
		if (!hasRows(results)) {
			results.close();
			modQuery.close();
			return false;
		}
		
		results.close();
		modQuery.close();
		
		return true;
		
	}
	
	private boolean containsProperty(final String property) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT property FROM ");
		query.append(SETTINGS_TABLE_NAME);
		query.append(" WHERE property = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, property);
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: {}", query);
		
		if (!hasRows(results)) {
			results.close();
			modQuery.close();
			return false;
		}
		
		results.close();
		modQuery.close();
		
		return true;
		
	}
	
	@Override
	public List<String> getModNames() throws SQLException {

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
	
	@Override
	public Mod getModByName(final String modName) throws SQLException {
		
		Mod output = null;
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		modQuery.setString(1, modName);
		
		ResultSet results = modQuery.executeQuery();

		log.trace("Statement Executed: {}", query);
		
		if (hasRows(results)) {
			
			log.debug("Mod found in database: {}", modName);
			
			results.next();
				
			Mod mod = new Mod(new LocalizerFactory());
			
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
			
			if (Files.notExists(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()))) {
				deleteMod(mod);
				return null;
			}
			
			Set<Mod> mods = null;
			
			try {
				
				long checksum = FileHelper.getChecksum(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()));
				
				if (mod.getChecksum() != checksum) {
					log.debug("Mod file checksum mismatch: {} ({})", mod.getArchiveName(), mod.getChecksum());
					mods = Mod.load(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), mod.getOrder(), new SettingsFactory(), new DatabaseFactory(), new LocalizerFactory());
				} else {
					mods = new HashSet<>();
					mods.add(mod);
				}
				
			} catch (final IOException e) {
				log.error("", e); //TODO Better error message
			}
			
			output = mod;

		}
		
		results.close();
		modQuery.closeOnCompletion();
		
		return output;
		
	}
	
	private String getSettingsValue(final String property) throws SQLException {

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT value FROM ");
		query.append(SETTINGS_TABLE_NAME);
		query.append(" WHERE property = ?");
		query.append(" LIMIT 1");
		
		PreparedStatement propertyQuery = connection.prepareStatement(query.toString());
		propertyQuery.setString(1, property);
		
		log.trace("Statement Executed: {}", propertyQuery);
		
		ResultSet results = propertyQuery.executeQuery();
		
		String output = null;
		
		if (hasRows(results)) {
			while (results.next()) {
				output = results.getString(1);
				log.debug("'{}' retrieved from database for property '{}'.", output, property);
			}
		}
		
		results.close();
		propertyQuery.closeOnCompletion();
		
		return output;
		
	}
	
	@Override
	public Map<String, String> getProperties() throws SQLException {
		
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
				log.debug("'{}' retrieved from database for property '{}'.", results.getString(2), results.getString(1));
			}
		}
		
		results.close();
		propertyQuery.closeOnCompletion();
		
		return properties;
		
	}
	
	@Override
	public String getPropertyString(final String property, final String defaultValue) {
		
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
	
	@Override
	public int getPropertyInt(final String property, final int defaultValue) {
		return Integer.parseInt(getPropertyString(property, "" + defaultValue));
	}
	
	@Override
	public void setProperty(final String property, Object value) {

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
				log.trace("Statement Executed: {}", statement);
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
				log.trace("Statement Executed: {}", statement);
				statement.closeOnCompletion();
			
			}
		
		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		log.debug("'{}' -> '{}' added to database.", property, value);
		
	}
	
	private boolean hasRows(final ResultSet resultSet) throws SQLException {
		return resultSet.isBeforeFirst();
	}

	@Override
	public void addObserver(final Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(final Observer observer) {
		observers.remove(observer);
	}
	
	private final void notifyObservers(final String message) {
		for (final Observer o : observers) {
			o.update(this, (Object) message);
		}
	}
	
}