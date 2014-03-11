package net.krazyweb.starmodmanager;

import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.data.Localizer;
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
		
		if (args.length > 0) {
			
			for (String arg : args) {
				log.debug("Command Line Arg: {}", arg);
			}
			
			if (args[0].equals("-language")) {
				
				String langFile = args[1];
				String locale = args[2];
				
				if (Files.exists(Paths.get(langFile))) {
					Localizer.overrideLanguage(langFile, locale);
				} else {
					log.warn("Could not open language file: {}", langFile);
				}
				
			}
			
		}
		
		launch(args);
		
	}

}
