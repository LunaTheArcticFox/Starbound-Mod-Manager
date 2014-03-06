package net.krazyweb.starmodmanager.data;

import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NotLoadedLocalizer implements LocalizerModelInterface, Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(NotLoadedLocalizer.class);
	
	protected NotLoadedLocalizer() {
	}
	
	@Override
	public Task<Void> getInitializerTask() {
		return null;
	}
	
	@Override
	public String getMessage(final String key, final boolean suppressLogging) {
		
		String output = "";
		
		switch (key) {
			case "messagedialogue.okay":
				output = "OK";
				break;
		}
		
		return output;
		
	}

	@Override
	public String getMessage(final String key) {
		return getMessage(key, false);
	}

	@Override
	public String formatMessage(final boolean suppressLogging, final String key, final Object... messageArguments) {
		return "";
	}

	@Override
	public String formatMessage(final String key, final Object... messageArguments) {
		return formatMessage(false, key, messageArguments);
	}

	@Override
	public List<Language> getLanguages() {
		return new ArrayList<>();
	}

	@Override
	public Language getCurrentLanguage() {
		return null;
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
	}

	@Override
	public void addObserver(final Observer observer) {
	}

	@Override
	public void removeObserver(final Observer observer) {
	}
	
}