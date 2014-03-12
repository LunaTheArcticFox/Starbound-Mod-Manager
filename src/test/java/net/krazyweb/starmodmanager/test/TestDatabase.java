package net.krazyweb.starmodmanager.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;
import net.krazyweb.starmodmanager.data.DatabaseModelInterface;
import net.krazyweb.starmodmanager.data.Mod;
import net.krazyweb.starmodmanager.data.Observer;

public class TestDatabase implements DatabaseModelInterface {

	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task<Void> getInitializerTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task<Void> getCloseTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateMod(Mod mod) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMod(Mod mod) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getModNames() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mod getModByName(String modName) throws SQLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getProperties() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertyString(String property, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPropertyInt(String property, int defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setProperty(String property, Object value) {
		// TODO Auto-generated method stub
		
	}

}