package net.krazyweb.starmodmanager.data;

import java.util.List;

import javafx.concurrent.Task;


public interface LocalizerModelInterface extends Observable {
	
	public static class Language implements Comparable<Language> {
		
		private String locale;
		private String name;
		
		protected Language(final String locale, final String name) {
			this.locale = locale;
			this.name = name;
		}
		
		public String getLocale() {
			return locale;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public int compareTo(final Language language) {
			return name.compareTo(language.name);
		}
		
		@Override
		public String toString() {
			return name + "\t (" + locale + ")";
		}
		
	}

	public Task<Void> getInitializerTask();
	
	public String getMessage(final String key, final boolean suppressLogging);
	public String getMessage(final String key);
	public String formatMessage(final boolean suppressLogging, final String key, final Object... messageArguments);
	public String formatMessage(final String key, final Object... messageArguments);
	
	public List<Language> getLanguages();
	public Language getCurrentLanguage();
	
}
