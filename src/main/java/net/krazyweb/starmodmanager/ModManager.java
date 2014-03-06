package net.krazyweb.starmodmanager;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.view.ApplicationLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModManager extends Application {
	
	private static final Logger log = LogManager.getLogger(ModManager.class);
	
	private static Stage primary;
	
	@Override
	public void start(final Stage primaryStage) throws Exception {
		primary = primaryStage;
		primaryStage.initStyle(StageStyle.DECORATED);
		primaryStage.centerOnScreen();
		try {
			new ApplicationLoader();
		} catch (final Exception e) {
			log.fatal("Uncaught application error: {}", e);
		}
	}
	
	public static Stage getPrimaryStage() {
		return primary;
	}
	
	public static void main(final String[] args) {
		launch(args);
	}

}
