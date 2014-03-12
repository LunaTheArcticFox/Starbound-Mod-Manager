package net.krazyweb.starmodmanager.test;

import net.krazyweb.starmodmanager.data.DatabaseModelFactory;
import net.krazyweb.starmodmanager.data.DatabaseModelInterface;

public class TestDatabaseFactory implements DatabaseModelFactory {

	@Override
	public DatabaseModelInterface getInstance() {
		return new TestDatabase();
	}

}
