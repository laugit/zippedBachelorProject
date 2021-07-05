import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestImageModifier {

	
	private static File buffer_file;
	private static ImageModifier imodif;
	private static JFrame fr;
	private static JFrame fr2;
	private static JLabel lab;
	private static JLabel lab2;
	
	@BeforeClass
	public static void setUpBeforeClass(){
		
		buffer_file = null;
		imodif = null;
		fr = null;
	}
	
	@AfterClass
	public static void EraseAfterClass() {
		
		buffer_file = null;
		imodif = null;
		fr.dispose();
		fr2.dispose();
		
	}
	
	@Before
	public void setUp() throws IOException {
		
		buffer_file = new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//image_test_2pixels.png");
		Main.jfc = new JFileChooser();
		Main.jfc.setSelectedFile(buffer_file);
		imodif = new ImageModifier(buffer_file);
		fr = new JFrame();
		fr.setSize(16,16);
		lab = new JLabel();
		lab.setSize(16,16);
		lab.setIcon(new ImageIcon(ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//image_test_16pixels.png"))));
		fr.add(lab);
		fr2 = new JFrame();
		fr2.setSize(ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png")).getWidth(),ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png")).getHeight());
		lab2 = new JLabel();
		lab2.setSize(ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png")).getWidth(),ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png")).getWidth());
		lab2.setIcon(new ImageIcon(ImageIO.read(new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png"))));
		fr2.add(lab2);
		
		
	}
	
	@After
	public void EraseBufferAfter() {
		
		buffer_file = null;
		imodif = null;
		fr.dispose();
		fr2.dispose();
		
	}
	
	
	@Test
	/**
	 * 
	 * @throws NonExistingFrameException if fr == null
	 * @throws IOException if buffer_file == null
	 * Test method for {@link ImageModifier#cutAndModifyImage(JFrame)}
	 */
	public void testCutAndModifyImage() throws NonExistingLabelException, IOException{
	
		buffer_file = new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//image_test_16pixels.png");
		imodif = new ImageModifier(buffer_file);
		Main.fileChoosed =  new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//image_test_16pixels.png");
		Main.label = lab;
		Main.enregistrer = new JMenuItem("enreg");
		Main.mainframe = fr;
		Main.options_retouche = new JMenuItem("retouche");
		Main.options_retouche.setEnabled(true);
		Main.help_button = new JMenuItem("help");
		Main.help_listener02 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		Main.runetape2 = new JMenuItem("etape2");
		Main.contour1 = new JMenuItem("contour1");
		Main.contour2 = new JMenuItem("contour2");
		Main.contour1.setEnabled(true);
		Main.contour2.setEnabled(true);
		Main.help_button.addActionListener(Main.help_listener02);
		Main.compare_frame = new JFrame();
		imodif.cutAndModifyImage();
		for(int i=1; i<=16; i++) {
			if(i==1||i==3||i==6||i==8||i==9||i==11||i==14||i==16) {
				assertTrue(ImageIO.read(new File("C://Users//"+System.getProperty("user.name")+"//Pictures//small_image"+i+"_file//image_filtered"+i+".png")).getRGB(0, 0) != Color.white.getRGB());
			}
			else {
				assertTrue(ImageIO.read(new File("C://Users//"+System.getProperty("user.name")+"//Pictures//small_image"+i+"_file//image_filtered"+i+".png")).getRGB(0, 0) == Color.white.getRGB());
			}
		}
		
	}
	
	@Test
	/**
	 * 
	 * @throws NullPointerException if buffer_file == null
	 * @throws IOException if bimage in cutAndModifyImage == null
	 * Test method for {@link ImageModifier#DrawRedSquaresOnImages()}
	 */
	public void testDrawRedSquaresOnImage() throws NullPointerException, IOException, NonExistingLabelException{
		buffer_file = new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png");
		imodif = new ImageModifier(buffer_file);
		Main.label = lab2;
		Main.mainframe = fr2;
		Main.enregistrer = new JMenuItem("enreg");
		Main.fileChoosed =  new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//cellule.png");
		Main.runetape3 = new JMenuItem("etape3");
		Main.options_retouche = new JMenuItem("retouche");
		Main.options_retouche.setEnabled(true);
		Main.help_button = new JMenuItem("help");
		Main.runetape2 = new JMenuItem("etape2");
		Main.help_listener02 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		Main.help_button.addActionListener(Main.help_listener02);
		Main.contour1 = new JMenuItem("contour1");
		Main.contour2 = new JMenuItem("contour2");
		Main.contour1.setEnabled(true);
		Main.contour2.setEnabled(true);
		Main.compare_frame = new JFrame();
		imodif.cutAndModifyImage();
		imodif.DrawRedSquaresOnImage();
		if(Main.method == 1) {
			assertTrue(Main.nb_square == 1);
		}
		else if(Main.method == 2) {
			assertTrue(Main.nb_square/14058 == 11);
		}
		else {
			assertTrue(Main.nb_square == 1);
		}
	}
	
}
