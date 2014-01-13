package net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.krazyweb.starmodmanager.helpers.FileHelper;


public class Database {
	
	private static final String MOD_TABLE_NAME = "mods";
	private static final String SETTINGS_TABLE_NAME = "settings";
	
	private static Connection connection;
	
	public static void initialize() throws SQLException {
		
		connection = DriverManager.getConnection("jdbc:hsqldb:file:" + new File("").getAbsolutePath().replaceAll("\\\\", "/") + "/data/db", "SA", "");
		
		createTables();
		
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
			statement.closeOnCompletion();
			
		}
		
	}
	
	public static void deleteMod(final Mod mod) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = '").append(mod.getInternalName()).append("';");
		
		Statement delete = connection.createStatement();
		delete.execute(query.toString());
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
		
		if (!hasRows(results)) {
			results.close();
			modQuery.close();
			return false;
		}
		
		results.close();
		modQuery.close();
		
		return true;
		
	}
	
	public static ArrayList<Mod> getModList() throws SQLException {
		
		ArrayList<Mod> modList = new ArrayList<Mod>();

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);

		PreparedStatement modQuery = connection.prepareStatement(query.toString());
		
		ResultSet results = modQuery.executeQuery();
		
		if (hasRows(results)) {
			
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
				
				HashSet<String> dependencies = new HashSet<String>();
				
				for (String data : results.getString("dependencies").split("\n")) {
					dependencies.add(data);
				}
				
				mod.setDependencies(dependencies);
				
				HashSet<ModFile> files = new HashSet<ModFile>();
				
				for (String data : results.getString("files").split("\n")) {
					
					String[] fields = data.split(":::");
					
					ModFile file = new ModFile();
					
					file.setPath(fields[0]);
					file.setJson(Boolean.parseBoolean(fields[1]));
					file.setIgnored(Boolean.parseBoolean(fields[2]));
					file.setAutoMerged(Boolean.parseBoolean(fields[3]));
					
					files.add(file);
					
				}
				
				mod.setFiles(files);
				
				modList.add(mod);
				
			}

		}
		
		results.close();
		modQuery.closeOnCompletion();
		
		getNewMods(modList);
		
		Collections.sort(modList, new Mod.ModOrderComparator());
		
		for (Mod mod : modList) {
			System.out.println("[" + mod.getOrder() + "] \t" + mod.getInternalName());
		}
		
		return modList;
		
	}
	
	private static void getNewMods(final ArrayList<Mod> modList) {
		
		//Collect all filenames of already recognized mods
		HashSet<String> currentArchives = new HashSet<String>();
		
		for (Mod mod : modList) {
			currentArchives.add(mod.getArchiveName());
		}
		
		//List all the archives in the mods directory, then remove already recognized mods
		HashSet<File> archives = new HashSet<File>();
		HashSet<File> toRemove = new HashSet<File>();
		FileHelper.listFiles(Settings.getModsDirectory(), archives);
		
		for (File file : archives) {
			if (currentArchives.contains(file.getName())) {
				toRemove.add(file);
			}
		}
		
		archives.removeAll(toRemove);
		
		for (File file : archives) {
			
			Mod mod = Mod.load(file);
			
			if (mod == null) {
				continue;
			}

			mod.setOrder(modList.size());
			
			modList.add(mod);
			
		}
		
	}
	
	private static boolean hasRows(final ResultSet resultSet) throws SQLException {
		return resultSet.isBeforeFirst();
	}
	
}