package net.krazyweb.starmodmanager.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.concurrent.Task;

import org.apache.logging.log4j.Level;

import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

public class TestSettings implements SettingsModelInterface {

	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task<Void> getInitializeLoggerTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task<Void> getLoadSettingsTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OS getOperatingSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertyString(String key) {
		
		switch (key) {
			case "modsdir":
				return "mods-testing";
		}
		
		return null;
		
	}

	@Override
	public int getPropertyInt(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPropertyDouble(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getPropertyBoolean(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Path getPropertyPath(String key) {
		
		switch (key) {
			case "modsdir":
				return Paths.get("mods-testing");
		}
		
		return null;
		
	}

	@Override
	public Level getPropertyLevel(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String key, Object property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoggerLevel(Level level) {
		// TODO Auto-generated method stub
		
	}

}
