import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

/**
 * 
 * @author Laura BASTIN
 * Shows the progress of long operations in the software.
 *
 */
public class Progress {

	private String title;
	private JFrame frame;
	private BufferedImage perc_image = new BufferedImage(500,50,BufferedImage.TYPE_INT_RGB);
	
	/**
	 * @param title : the title of the JFrame that will show the progress.
	 * @param percentage : the value (between 0 and 100) that represent the progress in percent.
	 * @precondition /
	 * @postcondition Initializes this to title,percentage
	 */
	public Progress(String title) {
		this.title = title;
		frame = new JFrame();
		frame.setLocationRelativeTo(null);
		frame.setSize(500,50);
		frame.setTitle(this.title);
		frame.setVisible(true);
	}
	
	/**
	 * @precondition /
	 * @postcondition this.title = newTitle
	 * @param newTitle : the new title of the frame
 	 */
	public void setTitle(String newTitle) {
		title = newTitle;
		frame.setTitle(title);
	}
	
	/**
	 * @param newPercent : the new percentage that will be shown in the frame
	 * @precondition /
	 * @postcondition this.percentage = newPercent
	 */
	public void setPercentage(int newPercent) {
		
		for(int x=0; x<newPercent; x++) {
			for(int y=0; y<50; y++) {
				perc_image.setRGB(x, y, Color.green.getRGB());
			}
		}
		frame.getGraphics().drawImage(perc_image, 0, 0, frame);
	}
	
	/**
	 * @precondtion /
	 * @postcondtion Erases the frame containing the percentage
	 */
	public void dispose() {
		frame.dispose();
	}
	
}
