package main.java.net.krazyweb.starmodmanager.data;

import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

public class Localizer {
	
	private static ResourceBundle bundle;
	
	public static void initialize() {
		bundle = ResourceBundle.getBundle("strings", Settings.getLocale());
	}
	
	public static String getMessage(final String key) {
		return bundle.getString(key);
	}
	
	public static String formatMessage(final String key, final Object... messageArguments) {
		
		MessageFormat formatter = new MessageFormat(bundle.getString(key), Settings.getLocale());
		formatter.setLocale(Settings.getLocale());
		
		return formatter.format(messageArguments);
		
	}
	
}