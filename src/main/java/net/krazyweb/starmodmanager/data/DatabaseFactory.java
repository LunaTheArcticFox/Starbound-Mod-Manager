package net.krazyweb.starmodmanager.data;

public class DatabaseFactory implements DatabaseModelFactory {
	
	private DatabaseModelInterface instance;
	
	@Override
	public DatabaseModelInterface getInstance() {
		if (instance == null) {
			synchronized (DatabaseFactory.class) {
				instance = new HyperSQLDatabase(new SettingsFactory().getInstance());
			}
		}
		return instance;
	}
	
}
