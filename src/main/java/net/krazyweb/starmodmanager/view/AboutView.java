package net.krazyweb.starmodmanager.view;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;


public class AboutView implements Observer {
	
	private VBox root;
	
	@SuppressWarnings("unused")
	private AboutViewController controller;

	private LocalizerModelInterface localizer;
	
	protected AboutView() {
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new AboutViewController(this);
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
		
		if (observable instanceof LocalizerModelInterface && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
