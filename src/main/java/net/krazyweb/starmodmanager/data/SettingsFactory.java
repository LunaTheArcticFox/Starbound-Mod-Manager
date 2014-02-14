package net.krazyweb.starmodmanager.data;


public class SettingsFactory implements SettingsModelFactory {
	
	private static Settings instance;
	
	@Override
	public SettingsModelInterface getInstance() {
		if (instance == null) {
			synchronized (SettingsFactory.class) {
				instance = new Settings(new DatabaseFactory());
			}
		}
		return instance;
	}
	
}
