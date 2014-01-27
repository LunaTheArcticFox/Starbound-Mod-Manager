package main.java.net.krazyweb.starmodmanager.data;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Localizer {
	
	private static ResourceBundle bundle;
	
	public static void initialize() {
		
		bundle = ResourceBundle.getBundle("strings", Settings.getLocale());
		
		
		
	}
	
	public static String getMessage(final String key) {
		return bundle.getString(key);
	}
	
	public static String formatMessage(final String key, Object... messageArguments) {
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(Settings.getLocale());
		formatter.applyPattern(bundle.getString(key));
		return formatter.format(messageArguments);
	}
	
}