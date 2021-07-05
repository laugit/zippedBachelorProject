import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * 
 * @author Laura Bastin
 * ImageAnalyser allows to analyze pixels of a BufferedImage. An ImageAnalyser is caracterised
 * by the File that the BufferedImage will use.
 *
 */
public class ImageAnalyser {

	private BufferedImage bimage;
	private File img_file;
	private ImageModifier img_mod;
	private float brightness_low;
	private float brightness_high;
	private String txt_readed;
	Logger logger = Logger.getLogger(ImageAnalyser.class);
	
	/**
	 * @param image_file : the image that will be analyzed
	 * @throws IOException if image_file = null
	 * @precondition /
	 * @postcondition creates a new instance of this (initialize the BufferedImage)
	 */
	public ImageAnalyser(File image_file) throws IOException{
		
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
		
		try {
			bimage = ImageIO.read(image_file);
			img_file = image_file;
			
		} catch (IOException e) {logger.fatal("IOException! This should never occur!");}
		
		txt_readed = new BufferedReader(new FileReader(Main.colors_file)).readLine();
		
	}
	
	/**
	 * @param x : the x coordinate of the image to analyze
	 * @param y : the y coordinate of the image to analyze
	 * @return true if the color of the pixel at (x,y) is less purple or darker than the other
	 * @throws IOException if bimage = null
	 */
	public boolean isLessPurpleORDarker(int x, int y) throws IOException{
		
		  String red_low = "";
		  String green_low = "";
		  String blue_low = "";
		  String red_high = "";
		  String green_high = "";
		  String blue_high = "";
		  Color color= new Color(bimage.getRGB(x,y)); 
		  float brightness = (color.getRed() * 0.2126f + color.getGreen() * 0.7152f + color.getBlue() * 0.07222f) /255;
		  if(Main.low_color == null && Main.high_color == null && txt_readed.isEmpty()) { // default configuration
			  if(brightness < 0.28f || brightness > 0.30f){
			  	return true;
			  }
		  } 
		  else if(Main.low_color != null && Main.high_color != null) { // if configuration via options
			  brightness_high = (Main.high_color.getRed() * 0.2126f + Main.high_color.getGreen() * 0.7152f + Main.high_color.getBlue() * 0.07222f) / 255;
			  brightness_low = (Main.low_color.getRed() * 0.2126f + Main.low_color.getGreen() * 0.7152f + Main.low_color.getBlue() * 0.07222f) / 255;
			  if(brightness < brightness_low || brightness > brightness_high) {
				  return true;
			  }
		  }
		  else if(!txt_readed.isEmpty()) { //if permanent configuration via options and teintes.txt file
			  for(int ind = 0; ind < txt_readed.length(); ind++) {
				 if(ind >= 0 && ind <3) {
					if(ind == 0 && txt_readed.charAt(ind) != '0' || ind == 1 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 1 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 1 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' ||  ind == 2) {
						if(red_low.length() == 0) {
							red_low = String.valueOf(txt_readed.charAt(ind));
						}
						else {
							red_low = red_low.concat(String.valueOf(txt_readed.charAt(ind))); 
						}
					}
				 }
				 else if(ind > 3 && ind < 7) {
					 if(ind == 4 && txt_readed.charAt(ind) != '0' || ind == 5 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 5 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 5 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' || ind == 6) {
						if(green_low.length() == 0) {
							green_low = String.valueOf(txt_readed.charAt(ind));
						}
						else {
							green_low = green_low.concat(String.valueOf(txt_readed.charAt(ind)));
						}
					}
				 }
				 else if(ind > 7 && ind < 11) {
					 if(ind == 8 && txt_readed.charAt(ind) != '0' || ind == 9 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 9 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 9 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' || ind == 10) {
						 if(blue_low.length() == 0) {
							blue_low = String.valueOf(txt_readed.charAt(ind));
						 }
						 else {
							blue_low = blue_low.concat(String.valueOf(txt_readed.charAt(ind)));
						 }
					 }
				 }
				 else if(ind > 11 && ind < 15) {
					 if(ind == 12 && txt_readed.charAt(ind) != '0' || ind == 13 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 13 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 13 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' || ind == 14) {
						 if(red_high.length() == 0) {
							red_high = String.valueOf(txt_readed.charAt(ind));
						 }
						 else {
							red_high = red_high.concat(String.valueOf(txt_readed.charAt(ind)));
						 }
					 }
				 }
				 else if(ind > 15 && ind < 19) {
					 if(ind == 16 && txt_readed.charAt(ind) != '0' || ind == 17 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 17 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 17 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' || ind == 18) {
						 if(green_high.length() == 0) {
							green_high = String.valueOf(txt_readed.charAt(ind));
						 }
						 else {
							green_high = green_high.concat(String.valueOf(txt_readed.charAt(ind)));
						 }
					 }
				 }
				 else if(ind >19 && ind <=22) {
					 if(ind == 20 && txt_readed.charAt(ind) != '0' || ind == 21 && txt_readed.charAt(ind) == '0' && txt_readed.charAt(ind-1) != '0' || ind == 21 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) != '0' || ind == 21 && txt_readed.charAt(ind) != '0' && txt_readed.charAt(ind-1) == '0' || ind == 22) {
						 if(blue_high.length() == 0) {
							blue_high = String.valueOf(txt_readed.charAt(ind));
						 }
						 else {
							 blue_high = blue_high.concat(String.valueOf(txt_readed.charAt(ind)));
						 }
					 }
				 }
			  }
			  brightness_high = (Integer.valueOf(red_high) * 0.2126f + Integer.valueOf(green_high) * 0.7152f + Integer.valueOf(blue_high) * 0.07222f) / 255;
			  brightness_low = (Integer.valueOf(red_low) * 0.2126f + Integer.valueOf(green_low) * 0.7152f + Integer.valueOf(blue_low) * 0.07222f) / 255;
			  if(brightness < brightness_low || brightness > brightness_high) {
				  return true;
			  }
		  }
		  return false;
		 
	}
	
	
	/**
	 * 
	 * @return bimage : the analyzed BufferedImage
	 */
	public BufferedImage getBufferedImage(){
		return bimage;
	}
	
}
