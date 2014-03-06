

package net.krazyweb.starmodmanager.dialogue;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.krazyweb.jfx.controls.ProgressIndicatorBar;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;
import net.krazyweb.starmodmanager.view.LoaderView;

public class ProgressDialogue {

	private Stage stage;
	
	private ProgressIndicatorBar bar;
	private Text text;
	
	public ProgressDialogue(final String windowTitle) {
		build(windowTitle);
	}
	
	private void build(final String windowTitle) {
		
		SettingsModelInterface settings = new SettingsFactory().getInstance();
		
		bar = new ProgressIndicatorBar();
		bar.setSize(250.0, 56.0);
		
		text = new Text();
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
		scene.getStylesheets().add(LoaderView.class.getClassLoader().getResource(settings.getPropertyString("theme")).toString());
		
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(windowTitle);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.centerOnScreen();
		
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
	
	public void start() {
		stage.show();
	}
	
}