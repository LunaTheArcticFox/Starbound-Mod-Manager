package net.krazyweb.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.scene.paint.Color;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CSSHelper {
	
	public static final Logger log = LogManager.getLogger(CSSHelper.class);
	
	public static Color getColor(final String key, final String file) {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(CSSHelper.class.getClassLoader().getResourceAsStream(file)));
		
		String line;
		Color c = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("/* " + key)) {
					
					line = line.replace("/*", "").replace("*/", "").trim().split(":")[1].trim();
					
					int rgb = Integer.decode(line); 
					
				    int r = (rgb & 0xFF0000) >> 16;
				    int g = (rgb & 0xFF00) >> 8;
				    int b = (rgb & 0xFF);
				    
				    c = new Color(r / 255.0, g / 255.0, b / 255.0, 1.0);
				    
				    log.debug("Color code '{}' converted to rgb({}, {}, {}) for key '{}'", line, r, g, b, key);
				    
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
		if (c == null) {
			throw new RuntimeException("Could not find color for key '" + key + "' in CSS file '" + file + "'");
		}
		
		return c;
		
	}
	
}