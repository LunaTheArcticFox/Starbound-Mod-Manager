package main.java.net.krazyweb.starmodmanager.view;

import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialogue {
	
	//TODO Major changes to this, see MessageDialogue

	private Stage stage;
	
	public ProgressBar bar;
	public Text text;
	
	private Scene createPreloaderScene() {
		bar = new ProgressBar();
		text = new Text();
		BorderPane p = new BorderPane();
		p.setTop(text);
		p.setCenter(bar);
		return new Scene(p, 300, 150);		
	}
	
	public void start(final String windowTitle) {
		stage = new Stage();
		stage.setScene(createPreloaderScene());
		stage.setTitle(windowTitle);
		stage.initStyle(StageStyle.UTILITY);
		stage.show();
	}
	
	public void close() {
		stage.close();
	}
	
}
