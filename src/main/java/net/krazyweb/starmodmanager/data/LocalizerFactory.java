package net.krazyweb.starmodmanager.data;

public class LocalizerFactory implements LocalizerModelFactory {
	
	private LocalizerModelInterface instance;
	
	@Override
	public LocalizerModelInterface getInstance() {
		if (instance == null) {
			synchronized (LocalizerFactory.class) {
				instance = new Localizer(new SettingsFactory().getInstance());
			}
		}
		return instance;
	}
	
}