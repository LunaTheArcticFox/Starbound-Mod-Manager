package net.krazyweb.starmodmanager.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import com.ibm.icu.text.MessageFormat;

public class Localizer implements LocalizerModelInterface, Observer {
	
	private static final Logger log = LogManager.getLogger(Localizer.class);
	
	private List<Language> languages;
	private Locale locale;
	private ResourceBundle bundle;
	
	private SettingsModelInterface settings;
	private SettingsModelFactory settingsFactory;
	
	private Set<Observer> observers;
	
	protected Localizer(final SettingsModelFactory settingsFactory) {
		observers = new HashSet<>();
		this.settingsFactory = settingsFactory;
	}
	
	@Override
	public Task<Void> getInitializerTask() {

		settings = settingsFactory.getInstance();
		settings.addObserver(this);
		
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Loading Localizer");
				this.updateProgress(0.0, 1.0);
				
				setLocale(settings.getPropertyString("locale"));
				
				languages = new ArrayList<>();
				Collections.addAll(languages,
					new Language("en-US", "English"),
					new Language("de-DE", "Deutsch"),
					new Language("fl-SB", "Floran")
				);
				Collections.sort(languages);
				
				this.updateProgress(1.0, 1.0);
				
				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				notifyObservers("localizerloaded");
			}
		});
		
		return task;
		
	}
	
	@Override
	public String getMessage(final String key, final boolean suppressLogging) {
		
		String output = "";
		
		try {
			output = bundle.getString(key.toLowerCase());
		} catch (final NullPointerException e) {
			if (!suppressLogging) {
				log.warn("Localization key is null.");
			}
			return "Localization key is null.";
		} catch (final MissingResourceException e) {
			if (!suppressLogging) {
				log.warn("Key '{}' not found.", key);
			}
			return "Key '" + key + "' not found.";
		} catch (final ClassCastException e) {
			if (!suppressLogging) {
				log.warn("Value found for key '{}' is not a String.", key);
			}
			return "Value found for key '" + key + "' is not a String.";
		}
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(output.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			if (!suppressLogging) {
				log.error(new ParameterizedMessage("Could not encode '{}' to UTF-8.", output), e);
			}
		}

		if (!suppressLogging) {
			log.debug("String '{}' converted to '{}'.", output, formatted);
		}
		
		return formatted;
		
	}

	@Override
	public String getMessage(final String key) {
		return getMessage(key, false);
	}

	@Override
	public String formatMessage(final boolean suppressLogging, final String key, final Object... messageArguments) {
		
		MessageFormat formatter = null;
		
		try {
			formatter = new MessageFormat(bundle.getString(key.toLowerCase()), locale);
		} catch (final IllegalArgumentException e) {
			if (!suppressLogging) {
				log.error(new ParameterizedMessage("Could not parse pattern for '{}'", key.toLowerCase()), e);
			}
		}
		
		if (formatter == null) {
			return "INVALID PROPERTY: " + key;
		}
		
		formatter.setLocale(locale);
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(formatter.format(messageArguments).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			if (!suppressLogging) {
				log.error(new ParameterizedMessage("Could not encode '{}' to UTF-8.", formatter.format(messageArguments)), e);
			}
		}
		
		if (!suppressLogging) {
			log.debug("String '{}' encoded to '{}'.", formatter.format(messageArguments), formatted);
		}
		
		return formatted;
		
	}

	@Override
	public String formatMessage(final String key, final Object... messageArguments) {
		return formatMessage(false, key, messageArguments);
	}

	@Override
	public List<Language> getLanguages() {
		return new ArrayList<>(languages);
	}

	@Override
	public Language getCurrentLanguage() {
		
		String loc = settings.getPropertyString("locale");
		
		for (Language l : languages) {
			if (l.getLocale().equals(loc)) {
				return l;
			}
		}
		
		return null;
		
	}

	private void setLocale(final String loc) {
		
		String[] splitLocale = loc.split("-");
		
		Locale oldLocale = locale;
		
		locale = new Locale(splitLocale[0], splitLocale[1]);
		
		//Don't reload the language if nothing changed
		if (oldLocale == null || !oldLocale.equals(locale)) {
			bundle = ResourceBundle.getBundle("strings", locale);
			log.debug("Locale set to: {}", locale);
			notifyObservers("localechanged");
		}
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Settings && message.equals("propertychanged:locale")) {
			log.debug("Locale changed message received.");
			setLocale(settings.getPropertyString("locale"));
		}
		
	}

	@Override
	public void addObserver(final Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(final Observer observer) {
		observers.remove(observer);
	}
	
	private final void notifyObservers(final String message) {
		for (final Observer o : observers) {
			o.update(this, (Object) message);
		}
	}
	
}