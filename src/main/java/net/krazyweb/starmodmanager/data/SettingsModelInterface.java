package net.krazyweb.starmodmanager.data;

import java.nio.file.Path;

import javafx.concurrent.Task;

import org.apache.logging.log4j.Level;


public interface SettingsModelInterface extends Observable {

	public static enum OS {
		WINDOWS, MACOS, LINUX32, LINUX64;
	}
	
	public Task<Void> getInitializeLoggerTask();
	public Task<Void> getLoadSettingsTask();
	
	public OS getOperatingSystem();
	public String getVersion();
	public String getApple();

	public String getPropertyString(final String key);
	public int getPropertyInt(final String key);
	public double getPropertyDouble(final String key);
	public boolean getPropertyBoolean(final String key);
	public Path getPropertyPath(final String key);
	public Level getPropertyLevel(final String key);
	
	public void setProperty(final String key, final Object property);
	public void setLoggerLevel(final Level level);
	
}
