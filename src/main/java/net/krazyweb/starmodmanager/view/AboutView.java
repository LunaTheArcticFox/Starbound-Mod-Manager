package net.krazyweb.starmodmanager.view;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.krazyweb.starmodmanager.data.Localizer;


public class AboutView implements Observer {
	
	private VBox root;
	
	private AboutViewController controller;
	
	protected AboutView() {
		controller = new AboutViewController(this);
		Localizer.getInstance().addObserver(this);
	}
	
	protected void build() {
		
		root = new VBox();
		
		Text testText = new Text("ABOUT");
		
		root.getChildren().add(testText);
		
	}
	
	private void updateStrings() {
		
	}
	
	protected Node getContent() {
		return root;
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
