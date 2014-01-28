package main.java.net.krazyweb.starmodmanager.data;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.ibm.icu.text.MessageFormat;

public class Localizer {
	
	private static final Logger log = Logger.getLogger(Localizer.class);
	
	private static ResourceBundle bundle;
	
	public static void initialize() {
		bundle = ResourceBundle.getBundle("strings", Settings.getLocale());
	}
	
	public static String getMessage(final String key) {
		try {
			return bundle.getString(key.toLowerCase());
		} catch (final NullPointerException e) {
			log.warn("Localization key is null.");
			return "Localization key is null.";
		} catch (final MissingResourceException e) {
			log.warn("Key '" + key + "' not found.");
			return "Key '" + key + "' not found.";
		} catch (final ClassCastException e) {
			log.warn("Value found for key '" + key + "' is not a String.");
			return "Value found for key '" + key + "' is not a String.";
		}
	}
	
	public static String formatMessage(final String key, final Object... messageArguments) {
		
		MessageFormat formatter = new MessageFormat(bundle.getString(key.toLowerCase()), Settings.getLocale());
		formatter.setLocale(Settings.getLocale());
		
		return formatter.format(messageArguments);
		
	}
	
}