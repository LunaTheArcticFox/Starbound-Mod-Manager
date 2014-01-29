package main.java.net.krazyweb.starmodmanager.data;

import java.io.UnsupportedEncodingException;
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
		
		String output = "";
		
		try {
			output = bundle.getString(key.toLowerCase());
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
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(output.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			log.error("Could not encode '" + output + "' to UTF-8.", e);
		}
		
		log.debug("String '" + output + "' converted to '" + formatted + "'.");
		
		return formatted;
		
	}
	
	public static String formatMessage(final String key, final Object... messageArguments) {
		
		MessageFormat formatter = null;
		
		try {
			formatter = new MessageFormat(bundle.getString(key.toLowerCase()), Settings.getLocale());
		} catch (final IllegalArgumentException e) {
			log.warn("Could not parse pattern for '" + key.toLowerCase() + "':", e);
		}
		
		if (formatter == null) {
			return "INVALID PROPERTY: " + key;
		}
		
		formatter.setLocale(Settings.getLocale());
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(formatter.format(messageArguments).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			log.error("Could not encode '" + formatter.format(messageArguments) + "' to UTF-8.", e);
		}
		
		log.debug("String '" + formatter.format(messageArguments) + "' encoded to '" + formatted + "'.");
		
		return formatted;
		
	}
	
}