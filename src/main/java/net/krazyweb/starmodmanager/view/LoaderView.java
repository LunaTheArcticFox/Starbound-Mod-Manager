package net.krazyweb.starmodmanager.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.krazyweb.jfx.controls.ProgressIndicatorBar;

public class LoaderView {

	private Stage stage;
	
	private ProgressIndicatorBar bar;
	private Text text;
	
	public void build() {
		
		bar = new ProgressIndicatorBar();
		bar.setSize(250.0, 56.0);
		
		text = new Text("Loading");
		text.setId("progress-text");
		
		VBox box = new VBox();
		box.getChildren().addAll(
			text,
			bar
		);
		box.setAlignment(Pos.CENTER);
		box.setSpacing(34);
		
		Scene scene = new Scene(box, 450, 300);
		scene.getStylesheets().add(LoaderView.class.getClassLoader().getResource("theme_base.css").toString());
		scene.getStylesheets().add(LoaderView.class.getClassLoader().getResource("theme_green.css").toString());
		
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Loading");
		stage.centerOnScreen();
		stage.show();
		
	}
	
	public void close() {
		stage.close();
	}
	
	public ProgressIndicatorBar getProgressBar() {
		return bar;
	}
	
	public Text getText() {
		return text;
	}
	
}