package net.krazyweb.starmodmanager.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;

public interface DatabaseModelInterface extends Observable {

	public Task<Void> getInitializerTask();
	public Task<Void> getCloseTask();
	
	public void updateMod(final Mod mod) throws SQLException;
	public void deleteMod(final Mod mod) throws SQLException;
	
	public List<String> getModNames() throws SQLException;
	public Mod getModByName(final String modName) throws SQLException, IOException;
	
	public Map<String, String> getProperties() throws SQLException;
	public String getPropertyString(final String property, final String defaultValue);
	public int getPropertyInt(final String property, final int defaultValue);
	public void setProperty(final String property, final Object value);
	
}
