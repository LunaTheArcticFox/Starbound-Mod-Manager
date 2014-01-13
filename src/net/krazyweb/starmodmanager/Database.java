package net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


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
			query.append("internalName = '")	.append(mod.getInternalName()).append("',");
			query.append("archiveName = '")		.append(mod.getArchiveName()).append("',");
			query.append("displayName = '")		.append(mod.getDisplayName()).append("',");
			query.append("modVersion = '")		.append(mod.getModVersion()).append("',");
			query.append("gameVersion = '")		.append(mod.getGameVersion()).append("',");
			query.append("author = '")			.append(mod.getAuthor()).append("',");
			query.append("description = '")		.append(mod.getDescription()).append("',");
			query.append("url = '")				.append(mod.getURL()).append("',");
			query.append("checksum = '")		.append(mod.getChecksum()).append("',");
			query.append("loadOrder = '")			.append(mod.getOrder()).append("',");
			query.append("hidden = '")			.append(mod.isHidden() ? "1" : "0").append("',");
			query.append("installed = '")		.append(mod.isInstalled() ? "1" : "0").append("',");
			query.append("dependencies = '")	.append(dependencyList.toString()).append("',");
			query.append("files = '")			.append(fileList.toString()).append(" ");
			query.append("WHERE internalName = '").append(mod.getInternalName()).append("';");
			
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
			query.append(") VALUES(");
			
			query.append("'").append(mod.getInternalName()).append("',");
			query.append("'").append(mod.getArchiveName()).append("',");
			query.append("'").append(mod.getDisplayName()).append("',");
			query.append("'").append(mod.getModVersion()).append("',");
			query.append("'").append(mod.getGameVersion()).append("',");
			query.append("'").append(mod.getAuthor()).append("',");
			query.append("'").append(mod.getDescription()).append("',");
			query.append("'").append(mod.getURL()).append("',");
			query.append("'").append(mod.getChecksum()).append("',");
			query.append("'").append(mod.getOrder()).append("',");
			query.append("'").append(mod.isHidden() ? "1" : "0").append("',");
			query.append("'").append(mod.isInstalled() ? "1" : "0").append("',");
			
			if (dependencyList.toString().isEmpty()) {
				query.append("'NULL',");
			} else {
				query.append("'").append(dependencyList.toString()).append("',");
			}
			
			query.append("'").append(fileList.toString()).append("')");
			
		}
		
		Statement insert = connection.createStatement();
		insert.execute(query.toString());
		insert.closeOnCompletion();
		
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
		
		Statement modQuery = connection.createStatement();
		
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT internalName FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(" WHERE internalName = '");
		query.append(mod.getInternalName());
		query.append("' LIMIT 1;");
		
		ResultSet results = modQuery.executeQuery(query.toString());
		
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
		
		Statement modQuery = connection.createStatement();

		StringBuilder query = new StringBuilder();
		
		query.append("SELECT * FROM ");
		query.append(MOD_TABLE_NAME);
		query.append(";");
		
		ResultSet results = modQuery.executeQuery(query.toString());
		
		if (!hasRows(results)) {
			return modList;
		}
		
		while (results.next()) {
			
			Mod mod = new Mod();
			
			mod.setInternalName(results.getString("internalName"));
			mod.setArchiveName(results.getString("archiveName"));
			mod.setArchiveName(results.getString("displayName"));
			mod.setArchiveName(results.getString("modVersion"));
			mod.setArchiveName(results.getString("gameVersion"));
			mod.setArchiveName(results.getString("author"));
			mod.setArchiveName(results.getString("description"));
			mod.setArchiveName(results.getString("url"));
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
		
		results.close();
		modQuery.closeOnCompletion();
		
		Collections.sort(modList, new Mod.ModOrderComparator());
		
		for (Mod mod : modList) {
			System.out.println("[" + mod.getOrder() + "] \t" + mod.getInternalName());
		}
		
		return modList;
		
	}
	
	private static boolean hasRows(final ResultSet resultSet) throws SQLException {
		return resultSet.isBeforeFirst();
	}
	
}