package net.krazyweb.starmodmanager.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class BackgroundTaskProgressDialogue {
	
	private Stage stage;
	
	private ProgressBar bar;
	private Text text;
	
	public void build() {
		bar = new ProgressBar();
		bar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue) {
				if ((int) newValue.doubleValue() >= 1) {
					close();
				}
			}
			
		});
		text = new Text("Loading");
		BorderPane p = new BorderPane();
		p.setTop(text);
		p.setCenter(bar);
		stage = new Stage();
		stage.setScene(new Scene(p, 300, 150));
		stage.setTitle("Loading");
		stage.centerOnScreen();
		stage.initStyle(StageStyle.UTILITY);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent event) {
				event.consume();
			}
		});
		stage.initModality(Modality.APPLICATION_MODAL);
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
	
	public void start() {
		stage.showAndWait();
	}
	
}
