package application;

import java.io.File;
import java.util.ArrayList;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import application.Configuration.KeyValuePair;

public class Database {
	
	private static final String DB_NAME = "database";
	private static final String TABLE_NAME = "mods";
	
	private static SqlJetDb database;
	
	public static void addMod(final Mod mod) throws SqlJetException {
		
		connect();
		
		if (hasMod(mod)) {
			System.out.println("Mod already in database! (" + mod.internalName + ")");
			return;
		}

		try {
			
			database.beginTransaction(SqlJetTransactionMode.WRITE);
			
			String files = "";
			
			for (String s : mod.filesModified) {
				files += s + ";"; 
			}
			
			if (files.length() > 0) {
				files = files.substring(0, files.length() - 1);
			}
			
			database.getTable(TABLE_NAME).insert(mod.internalName, mod.file, mod.installed ? "1" : "0", mod.patched ? "1" : "0", "0", 0, files, "");
			
		} finally {
			database.commit();
		}
		
		printTable();
		
	}
	
	public static void updateMod(final Mod mod) throws SqlJetException {
		
		connect();
		
		if (!hasMod(mod)) {
			addMod(mod);
			return;
		}

		try {
			
			database.beginTransaction(SqlJetTransactionMode.WRITE);
			
			String files = "";
			
			for (String s : mod.filesModified) {
				files += s + ";"; 
			}
			
			if (files.length() > 0) {
				files = files.substring(0, files.length() - 1);
			}

			ISqlJetCursor cursor = database.getTable(TABLE_NAME).lookup("nameIndex", mod.internalName);//.insert(mod.internalName, mod.file, mod.installed ? "1" : "0", mod.patched ? "1" : "0", "0", 0, files, "");

			if (!cursor.eof()) {
				cursor.update(mod.internalName, mod.file, mod.installed ? "1" : "0", mod.patched ? "1" : "0", "0", 0, files, "");
			}
			
			//printTable();
			
		} finally {
			database.commit();
		}
		
	}
	
	public static void removeMod(final String mod) throws SqlJetException {
		
		connect();

		try {
			
			database.beginTransaction(SqlJetTransactionMode.WRITE);

			ISqlJetCursor cursor = database.getTable(TABLE_NAME).lookup("fileIndex", mod);

			if (!cursor.eof()) {
				cursor.delete();
			}
			
		} finally {
			database.commit();
		}
		
		printTable();
		
	}
	
	public static void connect() throws SqlJetException {
		
		if (database != null) {
			return;
		}
		
		File dbFile = new File(DB_NAME);
		
		database = SqlJetDb.open(dbFile, true);
		if (!database.getOptions().isAutovacuum()) {
			database.getOptions().setAutovacuum(true);
		}
		
		if (!tableExists(TABLE_NAME)) {
			createTables();
		}

		database.beginTransaction(SqlJetTransactionMode.WRITE);
		database.commit();
		
	}
	
	private static void createTables() throws SqlJetException {

		database.beginTransaction(SqlJetTransactionMode.WRITE);
		
		try {
			database.createTable("CREATE TABLE " + TABLE_NAME + " ("
					+ "internal_name TEXT UNIQUE,"
					+ "file TEXT,"
					+ "installed VARCHAR(4),"
					+ "patched VARCHAR(4),"
					+ "hidden VARCHAR(4),"
					+ "order INT(12),"
					+ "files_modified MEDIUMTEXT,"
					+ "files_game_merged MEDIUMTEXT,"
					+ "dependencies MEDIUMTEXT)");
			database.createIndex("CREATE INDEX nameIndex ON " + TABLE_NAME + "(internal_name)");
			database.createIndex("CREATE INDEX fileIndex ON " + TABLE_NAME + "(file)");
		} finally {
			database.commit();
		}
		
	}
	
	private static boolean tableExists(final String tableName) throws SqlJetException {
		if (database.getSchema().getTable(tableName) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean hasMod(final Mod mod) throws SqlJetException {
		
		int rows = 0;
		
		database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		try {
			rows = (int) database.getTable(TABLE_NAME).lookup("nameIndex", mod.internalName).getRowCount();
		} finally {
			database.commit();
		}
		
		return (rows > 0);
		
	}
	
	public static boolean hasMod(final String mod) throws SqlJetException {
		
		int rows = 0;
		
		database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		try {
			rows = (int) database.getTable(TABLE_NAME).lookup("fileIndex", mod).getRowCount();
		} finally {
			database.commit();
		}
		
		return (rows > 0);
		
	}
	
	public static ArrayList<KeyValuePair> getModList() throws SqlJetException {
		
		ArrayList<KeyValuePair> data = new ArrayList<KeyValuePair>();
		
		database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetCursor cursor = database.getTable(TABLE_NAME).open();
		
		try {
			
			if (!cursor.eof()) {
				
				do {
					Object[] o = cursor.getRowValues();
					data.add(new KeyValuePair((String) o[1], (String) o[2]));
				} while (cursor.next());
				
			}
			
		} finally {
			cursor.close();
		}
		
		database.commit();
		
		return data;
		
	}
	
	private static void printTable() throws SqlJetException {
		
		database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		ISqlJetCursor cursor = database.getTable(TABLE_NAME).open();
		
		try {
			
			if (!cursor.eof()) {
				
				do {
					for (Object o : cursor.getRowValues()) {
						System.out.print(o + "  --  ");
					}
					System.out.println("");
				} while (cursor.next());
				
			}
			
		} finally {
			cursor.close();
		}
		
		database.commit();
		
		System.out.println("");
		
	}
	
	public static void closeConnection() {
		try {
			database.close();
		} catch (SqlJetException e) {
			Configuration.printException(e, "Closing database.");
		}
	}
	
}