package net.krazyweb.starmodmanager;

import java.io.File;
import java.util.HashSet;

import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class Database {
	
	private static final String DATABASE_NAME = "database";
	
	private static SqlJetDb database;
	
	public static void initialize() {
		
		File dbFile = new File(DATABASE_NAME);
		
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
	
	public static HashSet<Mod> getModList() {
		//TODO Get list from database and parse into Mod objects.
		return null;
	}
	
	public static void addMod(final Mod mod) {
		
	}
	
}