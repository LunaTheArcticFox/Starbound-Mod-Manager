package net.krazyweb.helpers;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class FXHelper {
	
	public static void setColor(final Node image, final Color color) {
		FXHelper.setColor(image, color.getRed(), color.getGreen(), color.getBlue());
	}
	
	public static void setColor(final Node image, final double r, final double g, final double b) {
		
		Image src = ((ImageView) image).getImage();
	    PixelReader reader = src.getPixelReader();

	    int width = (int) src.getWidth();
	    int height = (int) src.getHeight();

	    WritableImage dest = new WritableImage(width, height);
	    PixelWriter writer = dest.getPixelWriter();

	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	        	Color c = new Color(r, g, b, reader.getColor(x, y).getOpacity());
	        	writer.setColor(x, y, c);
	        }
	    }
	    
	    ((ImageView) image).setImage(dest);
		
	}
	
}
