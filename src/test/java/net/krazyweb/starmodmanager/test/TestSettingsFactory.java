package net.krazyweb.starmodmanager.test;

import net.krazyweb.starmodmanager.data.SettingsModelFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

public class TestSettingsFactory implements SettingsModelFactory {

	@Override
	public SettingsModelInterface getInstance() {
		return new TestSettings();
	}

}
