package net.krazyweb.starmodmanager.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;


public class BackupListView implements Observer {

	private VBox root;
	
	@SuppressWarnings("unused")
	private BackupViewController controller;

	@SuppressWarnings("unused")
	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	public BackupListView() {
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new BackupViewController(this);
	}
	
	protected void build() {
		
		root = new VBox();
		root.setAlignment(Pos.CENTER);
		
		Text comingSoon = new Text("Coming in Version 2.1.0");
		comingSoon.setId("about-view-title");
		
		root.getChildren().add(comingSoon);
		
	}
	
	protected Node getContent() {
		return root;
	}

	@Override
	public void update(Observable o, Object message) {
		// TODO Auto-generated method stub
		
	}
	
}