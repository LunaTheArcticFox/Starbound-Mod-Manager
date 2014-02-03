package net.krazyweb.starmodmanager.view;

import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoaderView {

	private Stage stage;
	
	private ProgressBar bar;
	private Text text;
	
	public void build() {
		bar = new ProgressBar();
		text = new Text("Loading");
		BorderPane p = new BorderPane();
		p.setTop(text);
		p.setCenter(bar);
		stage = new Stage();
		stage.setScene(new Scene(p, 300, 150));
		stage.setTitle("Loading");
		stage.centerOnScreen();
		stage.show();	
	}
	
	public void close() {
		stage.close();
	}
	
	public ProgressBar getProgressBar() {
		return bar;
	}
	
	public Text getText() {
		return text;
	}
	
}