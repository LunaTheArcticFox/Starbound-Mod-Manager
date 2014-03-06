package net.krazyweb.starmodmanager.data;

public class NotLoadedLocalizerFactory implements LocalizerModelFactory {
	
	private static LocalizerModelInterface instance;
	
	@Override
	public LocalizerModelInterface getInstance() {
		if (instance == null) {
			synchronized (NotLoadedLocalizerFactory.class) {
				instance = new NotLoadedLocalizer();
			}
		}
		return instance;
	}
	
}