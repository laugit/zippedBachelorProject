import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.event.KeyEvent;

/**
 * 
 * @author Laura Bastin
 * ImageAnalyser allows to modify pixels of a BufferedImage. An ImageModifier is caracterised
 * by the File that the BufferedImage will use.
 *
 */
public class ImageModifier {
	
	Logger logger = Logger.getLogger(ImageModifier.class.getName());
	private BufferedImage bimage;
	private File img_file;
	private int width;
	private int height;
	private boolean used;
	private BufferedImage deep_image_rd;
	private ActionListener filter_listener;
	private ActionListener squares_listener;
	private boolean hold_activated;
	private int xDrag;
	private int yDrag;
	private int square_size;
	private int ratio;
	private static BufferedImage resizedImage;
	private static BufferedImage resizedUpImage;
	private static BufferedImage full_image;
	private static BufferedImage higher_image_full;
	private boolean painter_activated;
	private int operations_nb = 0;
	private BufferedImage image_to_paint;
	private static boolean correct_erosion;
	private static int occ = 0;
	private static int i_min, i_max, j_min, j_max;
	private static JButton grouper;
	private static JFrame end_fr, zoomOptionsF,squareOptionsF,squareCountF,eraseAllF,groupSquaresF;
	private static MouseAdapter mAdapter;
	
	/**
	 * @param image_file : the image that will be modified
	 * @throws IOException if image_file = null
	 * @postcondition Creates a new instance of this (initialize the BufferedImage, the file containing the image, initialize 
	 * the number of squares on the picture ,the zoom on the image, the original image and the height and the width of the image)
	 */
	public ImageModifier(File image_file) throws NullPointerException{
		try {
			if(image_file == null) {
				throw new NullPointerException("File null at ImageModifier.ImageModifier(File)");
			}
			logger.setLevel(Level.DEBUG);
			BasicConfigurator.configure();
			bimage = ImageIO.read(image_file);
			img_file = image_file;
			width = bimage.getWidth();
			height = bimage.getHeight();
		}
		catch(IOException ioe){logger.fatal("IOException! This should never occur!");}
		filter_listener = null;
		squares_listener = null;
		square_size = 10;
		used = false;
		ratio = 1;
		painter_activated = false;
		
		
	}

	
	/**
	 * @param fileTocut : the file containing the image to cut
	 * @param fileName : the String pattern of the name of the files that will contain the "smaller images"
	 * @throws IOException if fileTocut is null
	 * @precondition bimage exists
	 * @postcondition Cut bimage into 16 "smaller images"
	 */
	public void cutImage(File fileTocut, String fileName) throws IOException {
		
		painter_activated = false;
		Progress progress = new Progress("Initialisation...");
		progress.setPercentage(0);
		
		BufferedImage new_bim[] = new BufferedImage[16];
		
		int count = 0;
		
		BufferedImage image1 = null;
		FileInputStream fis = null;
		if(fileTocut == null) {throw new IOException("The file must exist in ImageModifier.cutImage(File,String)");}
		
		try {
			fis = new FileInputStream(fileTocut);
			image1 = ImageIO.read(fis);
		} catch (IOException e1) {logger.fatal("Image file not found in ImageModifier.cutImage(String)");}
		
		for(int i = 0; i<4; i++) {
			for(int j = 0; j<4; j++) {
				new_bim[count] = new BufferedImage(image1.getWidth()/4,image1.getHeight()/4,image1.getType());
				Graphics2D g2 = new_bim[count++].createGraphics();
				g2.drawImage(image1, 0, 0, image1.getWidth()/4, image1.getHeight()/4, image1.getWidth()/4*j, image1.getHeight()/4*i, image1.getWidth()/4*j + image1.getWidth()/4, image1.getHeight()/4*i + image1.getHeight()/4,null);
				g2.dispose();
			}
        }
		image1.flush();
		for(int x=0; x<new_bim.length; x++) {
			int index = x+1;
			progress.setTitle("Création de la sous-image "+index+"/16...");
			progress.setPercentage(index*31);
			if(index == 16) {progress.dispose();}
			try {
				ImageIO.write(new_bim[x], "png", new File(Main.directory+"small_image"+index+"_file"+System.getProperty("file.separator")+fileName+index+".png"));
			} catch (IOException e) {logger.fatal("IOException : image not found in ImageModifier.cutAndModifyImage()");}
			
		}
		System.gc();
	}
	
	/**
	 * @throws NonExistingLabelException if Main.label == null
	 * @precondition bimage exists
	 * @postcondition Cuts bimage into 16 smaller images ,transforms the less purple and darker pixels into white pixels on the smaller images and erode them. 
	 * This function also merge all the smaller image and display the merge result in the main frame. 
	 */
	public void cutAndModifyImage() throws NonExistingLabelException, IOException{
		Main.label.removeAll();
		painter_activated = false;
		
		BufferedImage deep_image;
		
		File filter_file = null;
		
		try {
			cutImage(Main.fileChoosed,"small_image");
		} catch (IOException e1) {logger.fatal("IOException in ImageModifier.cutAndModifyImage()");}
		if (Main.label == null) {throw new NonExistingLabelException("Frame not existing in ImageModifier.cutAndModifyImage()");}
		
		bimage.flush();
		
		deep_image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		
		
		Graphics g = deep_image.getGraphics();
		
		BufferedImage small_img = null;
		String txt_readed = new BufferedReader(new FileReader(Main.e_d_file)).readLine();
		String input = null;
		if(Main.e_d == null && txt_readed.isEmpty()) {
			 input = javax.swing.JOptionPane.showInputDialog(null, "Entrez les érosions et dilations à faire");
		}
	
		Progress progress_white_erode = new Progress("Initialisation du filtrage et de l'érosion...");
		progress_white_erode.setPercentage(0);
		for(int j=1; j<=16; j++) {
			try {
				progress_white_erode.setTitle("Filtrage et érosion de la sous-image "+j+"/16...");
				progress_white_erode.setPercentage(j*31);
				if(j==16) {progress_white_erode.dispose();}
				if (Main.label == null) {throw new NonExistingLabelException("Frame not existing in ImageModifier.cutAndModifyImage()");}
				img_file = new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"small_image"+j+".png");
				BufferedImage img = ImageIO.read(img_file);
				ImageAnalyser image_an = new ImageAnalyser(img_file);
				for(int x=0; x<img.getWidth(); x++) {
					for(int y=0; y<img.getHeight(); y++) {
						if (image_an.isLessPurpleORDarker(x, y)) {
							img.setRGB(x, y, Color.white.getRGB());
						}
					}
				}
				ImageIO.write(img, "png", new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png"));
				
				
				//erosion
				
				
				try{	
					 System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			         Mat source = Imgcodecs.imread(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png",  Imgcodecs.CV_LOAD_IMAGE_COLOR);
			         Mat destination = new Mat(source.rows(),source.cols(),source.type());
			         
			         destination = source;

			         int erosion_size = 1;
			         
			         Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(erosion_size*8, erosion_size*8));
			         Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size((erosion_size*4), erosion_size*4));
			         
			         if(!txt_readed.isEmpty()) {
			        	 for(int index = 0; index < txt_readed.length(); index++) {
			        		 if(txt_readed.charAt(index) == 'd') {
			        			 Imgproc.dilate(destination, destination, element1);
			        		 }
			        		 else if(txt_readed.charAt(index)== 'e'){
			        			 Imgproc.erode(destination, destination, element);
			        		 }
			        		 else {
			        			 javax.swing.JOptionPane.showMessageDialog(null,"Attention il semble que vous avez entré d'autres caractères que le e ou le d dans les options.\nVeuillez vérifier le fichier erosions_dilations.txt dans votre répertoire Images et changer ce qui n'est pas e ou d en e, d ou rien.");
			        		 }
			        	 }
			         }
			         else if(Main.e_d!=null) {
			        	 for(int index = 0; index < Main.e_d.length(); index++) {
			        		 if(Main.e_d.charAt(index) == 'd') {
			        			 Imgproc.dilate(destination, destination, element1);
			        		 }
			        		 else if(Main.e_d.charAt(index) == 'e'){
			        			 Imgproc.erode(destination, destination, element);
			        		 }
			        		 else {
			        			 javax.swing.JOptionPane.showMessageDialog(null,"Attention il semble que vous avez entré d'autres caractères que le e ou le d dans les options");
			        		 }
			        	 }
			         }
			         else { //the first time the program is used
			        	 if (input.equals("")) {
				        	 Imgproc.erode(destination, destination, element);
					         Imgproc.dilate(destination, destination, element1);
					         Imgproc.dilate(destination, destination, element1);
				         }
				         else {
				        	 if(!input.contains("e") && !input.contains("d")) {
				        		 while(!input.contains("e") && !input.contains("d")) {
				        			 input = javax.swing.JOptionPane.showInputDialog(null, "Entrez les érosions et dilations à faire e pour érosion et d pour dilation)");
				        		 }
				        	 }
				        	 String txt = input;
				        	 for(int index = 0; index < txt.length(); index++) {
				        		 if(txt.charAt(index) == 'd') {
				        			 Imgproc.dilate(destination, destination, element1);
				        		 }
				        		 else {
				        			 Imgproc.erode(destination, destination, element);
				        		 }
				        	 }
				         }
			         }
			         
			         Imgcodecs.imwrite(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png", destination);
				}catch(Exception e) {logger.fatal("Unable to do erosion in ImageModifier.cutAndModifyImage()");}
				//
				
				
				small_img = ImageIO.read(new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png"));
				
				if (j>=1 && j<=4) {
					g.drawImage(small_img, (j-1)*width/4, 0, null);
				}
				else if (j>=5 && j<=8) {
					g.drawImage(small_img, (j-5)*width/4, (height/4), null);
				}
				else if(j>=9 && j<=12) {
					g.drawImage(small_img, (j-9)*width/4, 2*(height/4), null);
				}
				else {
					g.drawImage(small_img, (j-13)*width/4, 3*(height/4), null);
				}
				small_img.flush();
			} catch (IOException e) {logger.fatal("IOException in ImageModifier.cutAndModifyImage()");}
			System.gc();
		}
		try {
			ImageIO.write(deep_image, "png", new File(Main.directory+"picture_after_filter.png"));
		} catch (IOException e) {}
		filter_file =  new File(Main.directory+"picture_after_filter.png");
		
		g.dispose();
		
		try {
			Main.label.setIcon(new ImageIcon(ImageIO.read(new File(Main.directory+"picture_after_filter.png"))));
		} catch (IOException e) {logger.fatal("cannot read picture_after_filter in ImageModifier.cutAndModifiyImage()");}
		
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				if(deep_image.getRGB(i, j)==Color.white.getRGB()) {
					deep_image.setRGB(i, j, new Color(255,255,255,0).getRGB());
				}
				else {
					deep_image.setRGB(i, j, Color.red.getRGB());
				}
			}
		}
		
		for(int i=0; i<width;i++) {
			for(int j=height-5; j<height;j++) {
				deep_image.setRGB(i,j, bimage.getRGB(i,j));
			}
		}
		
		
		Main.compare_frame = new JFrame();
		Main.compare_frame.setSize(width, height);
		Main.compare_frame.setTitle("Comparaison avec image de base");
		Main.compare_frame.setLocation(800, 100);
		
		JPanel panel = new JPanel();
		
		JLabel down_label = new JLabel();
		down_label.setLayout(new GridBagLayout());
		down_label.setIcon(new ImageIcon(bimage));
		
		JLabel up_label = new JLabel();
		up_label.setIcon(new ImageIcon(deep_image));
		
		down_label.add(up_label);
		
		panel.add(down_label);
		Main.compare_frame.add(panel);
		Main.compare_frame.getContentPane().add(new JScrollPane(panel));
		Main.compare_frame.setVisible(true);
		
		deep_image.flush();
		
		System.gc();
		
		Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Main.compare_frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		Main.help_button.removeActionListener(Main.help_listener02);
		
		Main.help_listener03 = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				javax.swing.JOptionPane.showMessageDialog(null, "La fenêtre comparaison avec image de base affiche en rouge ce qui n'est pas en blanc sur l'image de gauche (traitée).\nCliquez sur Etape2 puis Exécuter l'entourage des cellules pour passer à la suite.");
			}
		};
		
		Main.help_button.addActionListener(Main.help_listener03);

		int rognage = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous rogner (enlever des parties indésirables) la photo?", "rognage",JOptionPane.YES_NO_OPTION);
		if(rognage == JOptionPane.YES_OPTION) {
			painter_first_step(filter_file);
		}
		else {
			Main.runetape2.setEnabled(true);
		}

		/*
		//for junit test
			
				textFrame.dispose();
				Main.options_retouche.setEnabled(false);
				Main.options_retouche.removeActionListener(options_listener);
				BufferedImage deep_image;
				
				try {
					cutImage(Main.fileChoosed,"small_image");
				} catch (IOException e1) {logger.fatal("IOException in ImageModifier.cutAndModifyImage()");}
				if (Main.label == null) {throw new NonExistingLabelException("Frame not existing in ImageModifier.cutAndModifyImage()");}
				
				bimage.flush();
				
				Progress progress_white_erode = new Progress("Initialisation du filtrage et de l'érosion...");
				progress_white_erode.setPercentage(0);
				deep_image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				
				
				Graphics g = deep_image.getGraphics();
				
				BufferedImage small_img = null;
				
				
				for(int j=1; j<=16; j++) {
					try {
						progress_white_erode.setTitle("Filtrage et érosion de la sous-image "+j+"/16...");
						progress_white_erode.setPercentage(j*31);
						if(j==16) {progress_white_erode.dispose();}
						img_file = new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"small_image"+j+".png");
						BufferedImage img = ImageIO.read(img_file);
						ImageAnalyser image_an = new ImageAnalyser(img_file);
						for(int x=0; x<img.getWidth(); x++) {
							for(int y=0; y<img.getHeight(); y++) {
								if (image_an.isLessPurpleORDarker(x, y)) {
									img.setRGB(x, y, Color.white.getRGB());
								}
							}
						}
						ImageIO.write(img, "png", new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png"));
						
						
						//erosion
						
						
						try{	
					         System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
					         Mat source = Imgcodecs.imread(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png",  Imgcodecs.CV_LOAD_IMAGE_COLOR);
					         Mat destination = new Mat(source.rows(),source.cols(),source.type());
					         
					         destination = source;

					         int erosion_size = 1;
					         
					         Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(erosion_size*8, erosion_size*8));
					         Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size((erosion_size*4), erosion_size*4));
					         if (textFieldErDil.getText().equals("")) {
					        	 Imgproc.erode(destination, destination, element);
						         Imgproc.dilate(destination, destination, element1);
						         Imgproc.dilate(destination, destination, element1);
					         }
					         else {
					        	 String txt = textFieldErDil.getText();
					        	 for(int index = 0; index < txt.length(); index++) {
					        		 if(txt.charAt(index) == 'd') {
					        			 Imgproc.dilate(destination, destination, element1);
					        		 }
					        		 else {
					        			 Imgproc.erode(destination, destination, element);
					        		 }
					        	 }
					         }
					         
					         Imgcodecs.imwrite(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png", destination);
						}catch(Exception e) {logger.fatal("Unable to do erosion in ImageModifier.cutAndModifyImage()");}
						//
						
						
						small_img = ImageIO.read(new File(Main.directory+"small_image"+j+"_file"+System.getProperty("file.separator")+"image_filtered"+j+".png"));
						
						if (j>=1 && j<=4) {
							g.drawImage(small_img, (j-1)*width/4, 0, null);
						}
						else if (j>=5 && j<=8) {
							g.drawImage(small_img, (j-5)*width/4, (height/4), null);
						}
						else if(j>=9 && j<=12) {
							g.drawImage(small_img, (j-9)*width/4, 2*(height/4), null);
						}
						else {
							g.drawImage(small_img, (j-13)*width/4, 3*(height/4), null);
						}
						small_img.flush();
					} catch (IOException e) {logger.fatal("IOException in ImageModifier.cutAndModifyImage()");}
					System.gc();
				}
				try {
					ImageIO.write(deep_image, "png", new File(Main.directory+"picture_after_filter.png"));
				} catch (IOException e) {}
				File filter_file =  new File(Main.directory+"picture_after_filter.png");
				
				g.dispose();
				
				try {
					Main.label.setIcon(new ImageIcon(ImageIO.read(filter_file)));
				} catch (IOException e) {logger.fatal("cannot read picture_after_filter in ImageModifier.cutAndModifiyImage()");}
				
				for(int i=0; i<width; i++) {
					for(int j=0; j<height; j++) {
						if(deep_image.getRGB(i, j)==Color.white.getRGB()) {
							deep_image.setRGB(i, j, new Color(255,255,255,0).getRGB());
						}
						else {
							deep_image.setRGB(i, j, Color.red.getRGB());
						}
					}
				}
				
				for(int i=0; i<width;i++) {
					for(int j=height-5; j<height;j++) {
						if(j < height && j>0 && i<width && i > 0) {
							deep_image.setRGB(i,j, bimage.getRGB(i,j));
						}
					}
				}
				
				
				Main.compare_frame = new JFrame();
				Main.compare_frame.setSize(width, height);
				Main.compare_frame.setTitle("Comparaison avec image de base");
				Main.compare_frame.setLocation(800, 100);
				
				JPanel panel = new JPanel();
				
				JLabel down_label = new JLabel();
				down_label.setLayout(new GridBagLayout());
				down_label.setIcon(new ImageIcon(bimage));
				
				JLabel up_label = new JLabel();
				up_label.setIcon(new ImageIcon(deep_image));
				
				down_label.add(up_label);
				
				panel.add(down_label);
				Main.compare_frame.add(panel);
				Main.compare_frame.getContentPane().add(new JScrollPane(panel));
				Main.compare_frame.setVisible(true);
				
				deep_image.flush();
				
				System.gc();
				
				Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				Main.compare_frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				
				Main.help_button.removeActionListener(Main.help_listener02);
				
				Main.help_listener03 = new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						javax.swing.JOptionPane.showMessageDialog(null, "La fenêtre comparaison avec image de base affiche en rouge ce qui n'est pas en blanc sur l'image de gauche (traitée).\nCliquez sur Etape2 puis Exécuter l'entourage des cellules pour passer à la suite.");
					}
				};
				
				Main.help_button.addActionListener(Main.help_listener03);
				
				int rognage = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous rogner (enlever des parties indésirables) la photo?", "rognage",JOptionPane.YES_NO_OPTION);
				if(rognage == JOptionPane.YES_OPTION) {
					painter_first_step(filter_file);
				}
				else {
					Main.runetape2.setEnabled(true);
				}
	
		*/
	}
	
	/**
	 * @param file_to_paint: the file containing the image to be partially painted in white
	 * @precondition file_to_paint exists and is not corrupted
	 * @postcondition Allows to paint parts of the image contained by file_to_paint in white to erase undesirable parts
	 */
	private void painter_first_step(File file_to_paint) {
		
		//determine if the image received treatment or not
		Main.is_treated = true;
		
		try {
			image_to_paint = ImageIO.read(file_to_paint);
			resizedImage = image_to_paint;
		} catch (IOException e1) {logger.fatal("Image to paint not existing in ImageModifier.painter_first_step(File)");}
		
		Main.help_button.removeActionListener(Main.help_listener03);
		
		Main.help_listener04 = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				javax.swing.JOptionPane.showMessageDialog(null, "Maintenez la souris enfoncée et bougez la pour effacer une zone de l'image. \nDézoomer 2x et dézoomer 4x réduisent la taille de l'image, respectivement, 2 et 4 fois. Zoom normal remet l'image à sa taille initiale.\nCliquez sur terminer le rognage puis Etape2 puis Exécuter l'entourage des cellules pour passer à l'étape suivante.\nLa fenêtre comparaison avec image de base affiche en rouge ce qui n'est pas en blanc sur l'image de gauche (traitée).");
			}
		};
		
		Main.help_button.addActionListener(Main.help_listener04);
		MouseAdapter mAdapter1 =  new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               //on left click
               if (e.getButton() == MouseEvent.BUTTON1) {
            	   if(e.getX()>=0 && e.getX()<ratio*width && e.getY()>=0 && e.getY()<ratio*height) {
	            	   xDrag = e.getX();
	            	   yDrag = e.getY();
            	   }
               }
            }
		};
		
		MouseMotionListener mlistener = new MouseMotionListener() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(xDrag <= e.getX() && yDrag <= e.getY()) {
					for(int x=xDrag; x<=e.getX();x++) {
						for(int y=yDrag;y<=e.getY();y++) {
							resizedImage.setRGB(x, y, Color.white.getRGB());
						}
					}
				}
				else if(xDrag > e.getX() && yDrag <= e.getY()) {
					for(int x=e.getX(); x<xDrag;x++) {
						for(int y=yDrag;y<=e.getY();y++) {
							resizedImage.setRGB(x, y, Color.white.getRGB());
						}
					}
				}
				else if(xDrag <= e.getX() && yDrag > e.getY()) {
					for(int x=xDrag; x<=e.getX();x++) {
						for(int y=e.getY();y<yDrag;y++) {
							resizedImage.setRGB(x, y, Color.white.getRGB());
						}
					}
				}
				
				else if(xDrag > e.getX() && yDrag > e.getY()) {
					for(int x=e.getX(); x<xDrag;x++) {
						for(int y=e.getY();y<yDrag;y++) {
							resizedImage.setRGB(x, y, Color.white.getRGB());
						}
					}
				}
				Main.label.setIcon(new ImageIcon(resizedImage));
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub	
			}

		};
		Main.label.addMouseListener(mAdapter1);
		Main.label.addMouseMotionListener(mlistener);
		
		JFrame dezoomF = new JFrame();
		dezoomF.setSize(225,225);
		dezoomF.setLocation(775, 10);
		dezoomF.setVisible(true);
		
		JLabel dezoomL = new JLabel();
		
		JButton dezoom2 = new JButton("Dézoomer 2x");
		dezoom2.setSize(100, 100);
		dezoom2.setLocation(0, 0);
		
		JButton dezoom4 = new JButton("Dézoomer 4x");
		dezoom4.setSize(100, 100);
		dezoom4.setLocation(100, 0);
		
		JButton zoom_base = new JButton("Zoom normal");
		zoom_base.setSize(100, 100);
		zoom_base.setLocation(50,100);
		
		dezoomL.add(dezoom2);
		dezoomL.add(dezoom4);
		dezoomL.add(zoom_base);
		dezoomF.add(dezoomL);
		
		dezoom2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dezoom4.setEnabled(true);
				zoom_base.setEnabled(true);
	    		BufferedImage resizedImage = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width/2, height/2, null);
	    		g2d.dispose();
	    		ImageModifier.resizedImage = resizedImage;
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		ratio = 1/2;
	    		dezoom2.setEnabled(false);
			}
		});
		
		dezoom4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dezoom2.setEnabled(true);
				zoom_base.setEnabled(true);
	    		BufferedImage resizedImage = new BufferedImage(width/4,height/4,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width/4, height/4, null);
	    		g2d.dispose();
	    		ImageModifier.resizedImage = resizedImage;
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		ratio = 1/4;
	    		dezoom4.setEnabled(false);
			}
		});
		
		zoom_base.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dezoom2.setEnabled(true);
				dezoom4.setEnabled(true);
	    		BufferedImage resizedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width, height, null);
	    		g2d.dispose();
	    		ImageModifier.resizedImage = resizedImage;
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		ratio = 1;
	    		zoom_base.setEnabled(false);
			}
		});
		
		JFrame end_frame = new JFrame();
		end_frame.setSize(200,100);
		end_frame.setLocation(1000, 10);
		end_frame.setVisible(true);
		
		JLabel end_label = new JLabel();
		
		JButton end1 = new JButton("Terminer le rognage");
		end1.setSize(200,100);
		
		end_label.add(end1);
		end_frame.add(end_label);
		
		end1.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event){
				if(ratio == 1) {
					Main.help_button.removeActionListener(Main.help_listener04);
					try {
						ImageIO.write(resizedImage, "png", new File(Main.directory+"picture_after_filter.png"));
					} catch (IOException e) {logger.fatal("Failed to write image in filtered_image in ImageModifier.painter_first_step(File)");}
					javax.swing.JOptionPane.showMessageDialog(null,"Image filtrée et rognée enregistrée sous "+Main.directory+"picture_after_filter.png");
					end_frame.dispose();
					dezoomF.dispose();
					Main.label.removeMouseListener(mAdapter1);
					Main.label.removeMouseMotionListener(mlistener);
					Main.runetape2.setEnabled(true);
				}
				else {
					javax.swing.JOptionPane.showMessageDialog(null, "Veuillez mettre le zoom normal (x1) pour passer à la suite.");
				}
			}
			
		});
		
	}
	
	/**
	 * @throws NonExistingLabelException if Main.label == null
	 * @throws IOException if a filtered smaller image does not exist
	 * @precondition the filtered (with the cells only) smaller images must exist
	 * @postcondition Draws red squares around the kernels on each filtered smaller image (images with the cells only) and merge all the images. 
	 * The merged result is displayed in the main frame. This can be done in 3 different modes (small images, large images with 
	 * cells surrounding precision and large images without cells surrounding precision
	 */
	public void DrawRedSquaresOnImage() throws NonExistingLabelException, IOException {
		
		Main.help_button.removeActionListener(Main.help_listener03);
		
		Main.compare_frame.dispose();
		painter_activated = false;
		
		if(Main.label == null) {throw new NonExistingLabelException("The drawable label does not exist in ImageModifier.DrawRedSquaresOnImage()");}
		
		if(Main.is_treated) {
			cutImage(new File(Main.directory+"picture_after_filter.png"),"image_filtered");
		}
		
		Main.help_listener05 = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				javax.swing.JOptionPane.showMessageDialog(null, "Vous êtes dans l'étape de l'entourage des cellules.\nEntourage petites images permet d'avoir un entourage précis sur les petites images\ntandis que entourage images larges permet d'avoir un entourage plus précis sur les grandes images.\nAprès l'entourage, vous pouvez choisir ou non de déjà retoucher l'image,\nc'est-à-dire de rajouter ou d'enlever des carrés entourant les cellules.");
			}
		};
		
		Main.help_button.addActionListener(Main.help_listener05);
		
		String txt_readed = new BufferedReader(new FileReader(Main.surr_file)).readLine();
		String nb_surround = "";
		boolean jump2 = false;
		boolean jump3 = false;
		if(!Main.perm_surround_runtime1 && !Main.perm_surround_runtime2 && !Main.perm_surround_runtime3 && txt_readed.isEmpty()) {
			nb_surround = javax.swing.JOptionPane.showInputDialog(null,"Veuillez choisir une méthode d'entourage. \nEntrez 1 pour un entourage précis des petites images\net 2 pour un entourage précis des grandes images");
			if(!nb_surround.equals("1") && !nb_surround.equals("2")) {
				while(!nb_surround.equals("1") && !nb_surround.equals("2")) {
					nb_surround = javax.swing.JOptionPane.showInputDialog(null,"Entrez 1 pour un entourage précis des petites images\net 2 pour un entourage précis des grandes images");
				}
			}
		}
		else if(Main.perm_surround_runtime1) {
			nb_surround = "1";
		}
		else if(Main.perm_surround_runtime2||Main.perm_surround_runtime3) {
			nb_surround = "2";
			if(Main.perm_surround_runtime2) {
				jump2 = true;
			}
			else if(Main.perm_surround_runtime3) {
				jump3 = true;
			}
		}
		else if(!txt_readed.isEmpty()){
			 if(txt_readed.equals("1")) {
				 nb_surround = "1";
			 }
			 else if(txt_readed.equals("2")||txt_readed.equals("3")) {
				 nb_surround = "2";
				 if(txt_readed.equals("2")) {
					 jump2 = true;
				 }
				 else if(txt_readed.equals("3")) {
					 jump3 = true;
				 }
			 }
		}
		
		if(nb_surround.equals("1")) {
			Main.method = 1;
			Progress progress_squares = new Progress("Initialisation de l'entourage des cellules...");
			progress_squares.setPercentage(0);
			
			BufferedImage bim_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics g = bim_full_size.getGraphics();
			BufferedImage calque_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics c = calque_full_size.getGraphics();
			
			for(int x = 1; x<=16; x++) {
				
				progress_squares.setTitle("Entourage des cellules de l'image "+x+"/16...");
				progress_squares.setPercentage(x*31); 
				if(x==16) {progress_squares.dispose();}
				logger.debug("before_rows");
	
				BufferedImage filtered_img = null;
				try {
					filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
				} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
				
				int filtered_img_w = filtered_img.getWidth();
				int filtered_img_h = filtered_img.getHeight();
				
				BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
				Graphics trans_grph = trans_calque.getGraphics();
			    trans_grph.setColor(new Color(0,0,0,0));
				
				ArrayList<Coordinate[]> coord_list_rows = new ArrayList<Coordinate[]>();
				ArrayList<Coordinate[]> coord_list_columns = new ArrayList<Coordinate[]>();
				ArrayList<Coordinate[][]> merged_rows_and_columns = new ArrayList<Coordinate[][]>();
				int distance_max_rows = 0;
				int distance_max_col = 0;
				int index_i = -1;
				int index_j = -1;
				int max_i = 0;
				int max_j = 0;
				
				
				logger.debug("just before rows");
				//iteration on rows
				outloopRows:
				for(int j=1;j<filtered_img_h;j++) {
					for(int i=1; i<filtered_img_w;i++) {
						if(filtered_img.getRGB(i, j)!=Color.white.getRGB() && filtered_img.getRGB(i-1, j)==Color.white.getRGB()) {
							Coordinate coord_left = new Coordinate(i,j);
							if(i+1<filtered_img_w) {
								i++;
							}
							if(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
								while(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
									if(i+1<filtered_img_w) {
										i++;
									}
									else {break outloopRows;}
								}
							}
							Coordinate coord_right = new Coordinate(i-1,j);
							if (coord_right.getX()-coord_left.getX() > distance_max_rows){
								distance_max_rows = coord_right.getX()-coord_left.getX();
								Coordinate[] coord_tab = {coord_left,coord_right};
								if (!coord_list_rows.isEmpty()) {
									coord_list_rows.remove(index_i);
									index_i--;
								}
								coord_list_rows.add(coord_tab);
								index_i++;
								if(coord_list_rows.size()>index_i) {
									trans_calque.setRGB(coord_list_rows.get(index_i)[0].getX(),coord_list_rows.get(index_i)[0].getY(),Color.cyan.getRGB());
									trans_calque.setRGB(coord_list_rows.get(index_i)[1].getX(),coord_list_rows.get(index_i)[1].getY(),Color.cyan.getRGB());
								}
							}
						}
					}
				}
				
				logger.debug("rows done");
				
				logger.debug("just before columns");
				//iteration on columns
				outloopCol:
				for(int i=1;i<filtered_img_w;i++) {
					for(int j=1; j<filtered_img_h;j++) {
						if(filtered_img.getRGB(i, j)!=Color.white.getRGB() && filtered_img.getRGB(i, j-1)==Color.white.getRGB()) {
							Coordinate coord_up = new Coordinate(i,j);
							if(j+1<filtered_img_h) {
								j++;
							}
							if(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
								while(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
									if(j+1<filtered_img_h) {
										j++;
									}
									else {break outloopCol;}
								}
							}
							Coordinate coord_down = new Coordinate(i,j-1);
							if (coord_down.getY()-coord_up.getY() > distance_max_col){
								distance_max_col = coord_down.getY()-coord_up.getY();
								Coordinate[] coord_tab = {coord_up,coord_down};
								if (!coord_list_columns.isEmpty()) {
									coord_list_columns.remove(index_j);
									index_j--;
								}
								coord_list_columns.add(coord_tab);
								index_j++;
								if(coord_list_columns.size()>index_j) {
									trans_calque.setRGB(coord_list_columns.get(index_j)[0].getX(),coord_list_columns.get(index_j)[0].getY(),Color.cyan.getRGB());
									trans_calque.setRGB(coord_list_columns.get(index_j)[1].getX(),coord_list_columns.get(index_j)[1].getY(),Color.cyan.getRGB());
								}
							}
						}
					}
				}
				
				//logger.info(coord_list_columns);
				logger.debug("columns done");
				
				//merge the two arrays
				for(int indexi = 0; indexi < coord_list_rows.size(); indexi ++) {
					for(int indexj= 0; indexj <coord_list_columns.size(); indexj ++) {
						if(coord_list_columns.get(indexj)[0].getX()>=coord_list_rows.get(indexi)[0].getX()||coord_list_columns.get(indexj)[0].getX()<=coord_list_rows.get(indexi)[0].getX()) {
							Coordinate[][] coord_square = {coord_list_rows.get(indexi),coord_list_columns.get(indexj)};
							merged_rows_and_columns.add(coord_square);
						}
					}
				}
				//
				logger.debug("merging done");
	
				//draw red squares
			   
				for(int l = 0; l<merged_rows_and_columns.size(); l++) {
					
					int X_left_part = merged_rows_and_columns.get(l)[0][0].getX();
					int X_right_part = merged_rows_and_columns.get(l)[0][1].getX();
					int Y_up_part = merged_rows_and_columns.get(l)[1][0].getY();
					int Y_down_part = merged_rows_and_columns.get(l)[1][1].getY();
					
					drawSquare(trans_calque, new Coordinate(X_left_part,Y_up_part), new Coordinate(X_left_part,Y_down_part), new Coordinate(X_right_part, Y_up_part), new Coordinate(X_right_part,Y_down_part));
						
				}
			    
				//
				
				//Remove cyan color on transparent image
				for(int i=0;i<filtered_img_w;i++) {
					for(int j=0;j<filtered_img_h;j++) {
						if (trans_calque.getRGB(i, j)==Color.cyan.getRGB()) {
							trans_calque.setRGB(i, j, filtered_img.getRGB(i, j));
						}
					}
				}
				//
				
				//recreate filtered image (second layer) and the first layer image (with squares)
				g.setColor(new Color(0,0,0,255));
				if (x>=1 && x<=4) {
					g.drawImage(filtered_img, (x-1)*width/4, 0, null);
					c.drawImage(trans_calque, (x-1)*width/4, 0, null);
				}
				else if (x>=5 && x<=8) {
					g.drawImage(filtered_img, (x-5)*width/4, height/4, null);
					c.drawImage(trans_calque, (x-5)*width/4, height/4, null);
				}
				else if(x>=9 && x<=12) {
					g.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
					c.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
				}
				else {
					g.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
					c.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
				}
				//
				
				
				filtered_img.flush();
				trans_calque.flush();
				
				logger.debug("sticking done");
				
				trans_grph.dispose();
				System.gc();
				
			}
			
			g.dispose();
			c.dispose();
			
			logger.debug("all done");
			Main.label.setIcon(new ImageIcon(bim_full_size));
			
			JLabel label = new JLabel();
			label.setSize(width,height);
			label.setIcon(new ImageIcon(calque_full_size));
			label.setVisible(true);
			//fuse the two layers
			Main.label.add(label);
			
			if(Main.upper_born==16) {
				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules sur la sous-image "+Main.i+" : "+Main.square_array[Main.i-1]);
			}
			else {
				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire: "+Main.nb_square);
			}
			
		   	int retouche = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
		   	if(retouche == JOptionPane.YES_OPTION) {
		   		Main.help_button.removeActionListener(Main.help_listener05);
		   		painter(calque_full_size,bim_full_size);
		   	}
		   	else {
		   		try {
		   			if(Main.upper_born!=16) {
						ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre.png"));
						javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
		   			}
		   			else {
			   				ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre"+Main.i+".png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre"+Main.i+".png");
		   			}
				} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
		   		Main.runetape3.setEnabled(true);
		   	}
		   	
		   	bim_full_size.flush();
		   	
			Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
		}
		
		else if(nb_surround.equals("2")){
			int precision = 0;
			if(!jump2 && !jump3) {
				precision = javax.swing.JOptionPane.showConfirmDialog(null, "Activer le mode d'entourage moins précis mais avec un comptage plus précis?", "Précision",JOptionPane.YES_NO_OPTION);
			}
			else if(jump2) {
				precision = JOptionPane.NO_OPTION;
			}
			else if(jump3) {
				precision = JOptionPane.YES_OPTION;
			}
			if(precision == JOptionPane.NO_OPTION) {
				Main.method = 2;
				Progress progress_squares = new Progress("Initialisation de l'entourage des cellules...");
				progress_squares.setPercentage(0);
				
				BufferedImage bim_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				Graphics g = bim_full_size.getGraphics();
				BufferedImage calque_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				Graphics c = calque_full_size.getGraphics();
				
				for(int x = 1; x<=16; x++) {
					
					progress_squares.setTitle("Entourage des cellules de l'image "+x+"/16...");
					progress_squares.setPercentage(x*31); 
					if(x==16) {progress_squares.dispose();}
					logger.debug("before_rows");

					BufferedImage filtered_img = null;
					try {
						filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
					} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
					
					int filtered_img_w = filtered_img.getWidth();
					int filtered_img_h = filtered_img.getHeight();
					
					BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
					Graphics trans_grph = trans_calque.getGraphics();
				    trans_grph.setColor(new Color(0,0,0,0));
					
					
				    
				   //iteration on rows and columns
				    
				    int i_depart = 0;
				    int j_depart = 0;
				    int i_arrivee = 0;
				    int j_arrivee = 0;
				    for(int i =0; i < filtered_img_w; i++) {
				    	for(int j=0; j<filtered_img_h;j++) {
			    			//horizontal treatment
				    		if(i-1>=0 && filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i-1,j) == Color.white.getRGB()) {
				    			i_arrivee = 0;
				    			i_depart = i;
				    			j_depart = j;
				    			while(filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i-1,j) == Color.white.getRGB()) {
				    				if(i+1<filtered_img_w) {
				    					i++;
				    					if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
				    				}
				    				else {break;}
				    			}
				    			i_arrivee = i;
				    			while(filtered_img.getRGB(i_depart,j_depart)!=Color.white.getRGB() && trans_calque.getRGB(i_depart,j_depart)!=Color.red.getRGB() && filtered_img.getRGB(i_depart-1,j_depart)==Color.white.getRGB()) {
				    				i=i_depart;
				    				if(j+1<filtered_img_h) {
					    				j++;
					    				j_arrivee = j;
					    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
					    			}
				    				else {j_arrivee = j_depart; break;}
					    		}
				    		}
				    		//vertical treatment
				    		else if(j-1>=0 && filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i,j-1) == Color.white.getRGB()) {
				    			j_arrivee = 0;
				    			i_depart = i;
				    			j_depart = j;
				    			while(filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i,j-1) == Color.white.getRGB()) {
				    				if(j+1<filtered_img_h) {
				    					j++;
					    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
				    				}
				    				else {break;}
				    			}
				    			j_arrivee = j;
				    			while(filtered_img.getRGB(i_depart,j_depart)!=Color.white.getRGB() && trans_calque.getRGB(i_depart,j_depart)!=Color.red.getRGB() && filtered_img.getRGB(i_depart,j_depart-1)==Color.white.getRGB()) {
				    				j=j_depart;
				    				if(i+1<filtered_img_w) {
					    				i++;
					    				i_arrivee = i;
					    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
					    			}
				    				else {i_arrivee = i_depart; break;}
					    		}
				    		}
				    		drawSquare(trans_calque, new Coordinate(i_depart,j_depart), new Coordinate(i_depart,j_arrivee), new Coordinate(i_arrivee,j_depart), new Coordinate(i_arrivee,j_arrivee));
				    	}
				    }
				    //
				    
					//recreate filtered image (second layer) and the first layer image (with squares)
					g.setColor(new Color(0,0,0,255));
					if (x>=1 && x<=4) {
						g.drawImage(filtered_img, (x-1)*width/4, 0, null);
						c.drawImage(trans_calque, (x-1)*width/4, 0, null);
					}
					else if (x>=5 && x<=8) {
						g.drawImage(filtered_img, (x-5)*width/4, height/4, null);
						c.drawImage(trans_calque, (x-5)*width/4, height/4, null);
					}
					else if(x>=9 && x<=12) {
						g.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
						c.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
					}
					else {
						g.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
						c.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
					}
					//
					
					
					filtered_img.flush();
					trans_calque.flush();
					
					logger.debug("sticking done");
					
					trans_grph.dispose();
					System.gc();
					
				}
				
				g.dispose();
				c.dispose();
				
				logger.debug("all done");
				Main.label.setIcon(new ImageIcon(bim_full_size));
				
				JLabel label = new JLabel();
				label.setSize(width,height);
				label.setIcon(new ImageIcon(calque_full_size));
				label.setVisible(true);
				
				//fuse the two layers
				Main.label.add(label);
				
				if(Main.upper_born==16) {
					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules sur la sous-image "+Main.i+" : "+Main.square_array[Main.i-1]/14058);
				}
				else {
					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire: "+Main.nb_square/14058);
				}
				
			   	int retouche = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
			   	if(retouche == JOptionPane.YES_OPTION) {
			   		Main.help_button.removeActionListener(Main.help_listener05);
			   		painter(calque_full_size,bim_full_size);
			   	}
			   	else {
			   		try {
			   			if(Main.upper_born!=16) {
							ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre.png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
			   			}
			   			else {
			   				ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre"+Main.i+".png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre"+Main.i+".png");
			   			}
					} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
			   		Main.runetape3.setEnabled(true);
			   	}
			   	
			   	bim_full_size.flush();
			   	
			}
			else if(precision == JOptionPane.YES_OPTION) {
				Main.method = 3;
				Progress progress_squares = new Progress("Initialisation de l'entourage des cellules...");
				progress_squares.setPercentage(0);
				
				BufferedImage bim_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				Graphics g = bim_full_size.getGraphics();
				BufferedImage calque_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				Graphics c = calque_full_size.getGraphics();
				
				for(int x = 1; x<=16; x++) {
					progress_squares.setTitle("Entourage des cellules de l'image "+x+"/16...");
					progress_squares.setPercentage(x*31); 
					if(x==16) {progress_squares.dispose();}
					
					BufferedImage filtered_img = null;
					try {
						filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
					} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
					
					int filtered_img_w = filtered_img.getWidth();
					int filtered_img_h = filtered_img.getHeight();
					
					BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
					Graphics trans_grph = trans_calque.getGraphics();
				    trans_grph.setColor(new Color(0,0,0,255));
				    
					boolean square_near = false;
					ImageAnalyser ia_filtered = null;
					if(!new BufferedReader(new FileReader(Main.size_file)).readLine().isEmpty()) {
						Main.sq_size3 = Integer.valueOf(new BufferedReader(new FileReader(Main.size_file)).readLine());
					}
					try {
						ia_filtered = new ImageAnalyser(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
					} catch (IOException e) {logger.fatal("image_filtered"+x+"does not exist. This error should never occur.");}
					for(int i=0; i<filtered_img_w; i++) {
						for(int j=0; j<filtered_img_h; j++) {
							try {
								if(!ia_filtered.isLessPurpleORDarker(i, j)) {
									for(int k=-Main.sq_size3; k<=Main.sq_size3; k++) {
										if(!(i+k > 0 && i+k < filtered_img_w && j+k>0 && j+k < filtered_img_h) || (trans_calque.getRGB(i+k, j) == Color.red.getRGB() || trans_calque.getRGB(i, j+k) == Color.red.getRGB())) {
											square_near = true;
										}
									}
									if(!square_near) {
										drawSquare(trans_calque, new Coordinate(i-Main.sq_size3,j-Main.sq_size3), new Coordinate(i-Main.sq_size3,j+Main.sq_size3), new Coordinate(i+Main.sq_size3,j-Main.sq_size3), new Coordinate(i+Main.sq_size3,j+Main.sq_size3));
									}
									square_near = false;
								}
							} catch (IOException e) {logger.fatal("filtered_img does not exists. This error should never occur.");}
						}
					}
					//recreate filtered image (second layer) and the first layer image (with squares)
					g.setColor(new Color(0,0,0,255));
					if (x>=1 && x<=4) {
						g.drawImage(filtered_img, (x-1)*width/4, 0, null);
						c.drawImage(trans_calque, (x-1)*width/4, 0, null);
					}
					else if (x>=5 && x<=8) {
						g.drawImage(filtered_img, (x-5)*width/4, height/4, null);
						c.drawImage(trans_calque, (x-5)*width/4, height/4, null);
					}
					else if(x>=9 && x<=12) {
						g.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
						c.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
					}
					else {
						g.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
						c.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
					}
					//
					
					filtered_img.flush();
					trans_calque.flush();
					
					trans_grph.dispose();
					System.gc();
				}
				g.dispose();
				c.dispose();
				
				Main.label.setIcon(new ImageIcon(bim_full_size));
				
				JLabel label = new JLabel();
				label.setSize(width,height);
				label.setIcon(new ImageIcon(calque_full_size));
				label.setVisible(true);
				
				//fuse the two layers
				Main.label.add(label);
				
				if(Main.upper_born==16) {
					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules sur la sous-image "+Main.i+" : "+Main.square_array[Main.i-1]);
				}
				else {
					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules total actuel: "+Main.nb_square);
				}
				
			   	int retouche = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
			   	if(retouche == JOptionPane.YES_OPTION) {
			   		Main.help_button.removeActionListener(Main.help_listener05);
			   		painter(calque_full_size,bim_full_size);
			   	}
			   	else {
			   		try {
			   			if(Main.upper_born!=16) {
							ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre.png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
			   			}
			   			else {
			   				ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre"+Main.i+".png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre"+Main.i+".png");
			   			}
					} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
			   		Main.runetape3.setEnabled(true);
			   	}
			   	
			   	bim_full_size.flush();
				
			}
			Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}
		
		/*
		//for junit test method 1
		Main.method = 1;
		Progress progress_squares = new Progress("Initialisation de l'entourage des cellules...");
		progress_squares.setPercentage(0);
		
		BufferedImage bim_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bim_full_size.getGraphics();
		BufferedImage calque_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics c = calque_full_size.getGraphics();
		
		for(int x = 1; x<=16; x++) {
			
			progress_squares.setTitle("Entourage des cellules de l'image "+x+"/16...");
			progress_squares.setPercentage(x*31); 
			if(x==16) {progress_squares.dispose();}
			logger.debug("before_rows");

			BufferedImage filtered_img = null;
			try {
				filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
			} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
			
			int filtered_img_w = filtered_img.getWidth();
			int filtered_img_h = filtered_img.getHeight();
			
			BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
			Graphics trans_grph = trans_calque.getGraphics();
		    trans_grph.setColor(new Color(0,0,0,0));
			
			ArrayList<Coordinate[]> coord_list_rows = new ArrayList<Coordinate[]>();
			ArrayList<Coordinate[]> coord_list_columns = new ArrayList<Coordinate[]>();
			ArrayList<Coordinate[][]> merged_rows_and_columns = new ArrayList<Coordinate[][]>();
			int distance_max_rows = 0;
			int distance_max_col = 0;
			int index_i = -1;
			int index_j = -1;
			int max_i = 0;
			int max_j = 0;
			
			
			logger.debug("just before rows");
			//iteration on rows
			outloopRows:
			for(int j=1;j<filtered_img_h;j++) {
				for(int i=1; i<filtered_img_w;i++) {
					if(filtered_img.getRGB(i, j)!=Color.white.getRGB() && filtered_img.getRGB(i-1, j)==Color.white.getRGB()) {
						Coordinate coord_left = new Coordinate(i,j);
						if(i+1<filtered_img_w) {
							i++;
						}
						if(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
							while(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
								if(i+1<filtered_img_w) {
									i++;
								}
								else {break outloopRows;}
							}
						}
						Coordinate coord_right = new Coordinate(i-1,j);
						if (coord_right.getX()-coord_left.getX() > distance_max_rows){
							distance_max_rows = coord_right.getX()-coord_left.getX();
							Coordinate[] coord_tab = {coord_left,coord_right};
							if (!coord_list_rows.isEmpty()) {
								coord_list_rows.remove(index_i);
								index_i--;
							}
							coord_list_rows.add(coord_tab);
							index_i++;
							if(coord_list_rows.size()>index_i) {
								trans_calque.setRGB(coord_list_rows.get(index_i)[0].getX(),coord_list_rows.get(index_i)[0].getY(),Color.cyan.getRGB());
								trans_calque.setRGB(coord_list_rows.get(index_i)[1].getX(),coord_list_rows.get(index_i)[1].getY(),Color.cyan.getRGB());
							}
						}
					}
				}
			}
			
			logger.debug("rows done");
			
			logger.debug("just before columns");
			//iteration on columns
			outloopCol:
			for(int i=1;i<filtered_img_w;i++) {
				for(int j=1; j<filtered_img_h;j++) {
					if(filtered_img.getRGB(i, j)!=Color.white.getRGB() && filtered_img.getRGB(i, j-1)==Color.white.getRGB()) {
						Coordinate coord_up = new Coordinate(i,j);
						if(j+1<filtered_img_h) {
							j++;
						}
						if(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
							while(filtered_img.getRGB(i, j)!=Color.white.getRGB()) {
								if(j+1<filtered_img_h) {
									j++;
								}
								else {break outloopCol;}
							}
						}
						Coordinate coord_down = new Coordinate(i,j-1);
						if (coord_down.getY()-coord_up.getY() > distance_max_col){
							distance_max_col = coord_down.getY()-coord_up.getY();
							Coordinate[] coord_tab = {coord_up,coord_down};
							if (!coord_list_columns.isEmpty()) {
								coord_list_columns.remove(index_j);
								index_j--;
							}
							coord_list_columns.add(coord_tab);
							index_j++;
							if(coord_list_columns.size()>index_j) {
								trans_calque.setRGB(coord_list_columns.get(index_j)[0].getX(),coord_list_columns.get(index_j)[0].getY(),Color.cyan.getRGB());
								trans_calque.setRGB(coord_list_columns.get(index_j)[1].getX(),coord_list_columns.get(index_j)[1].getY(),Color.cyan.getRGB());
							}
						}
					}
				}
			}
			
			//logger.info(coord_list_columns);
			logger.debug("columns done");
			
			//merge the two arrays
			for(int indexi = 0; indexi < coord_list_rows.size(); indexi ++) {
				for(int indexj= 0; indexj <coord_list_columns.size(); indexj ++) {
					if(coord_list_columns.get(indexj)[0].getX()>=coord_list_rows.get(indexi)[0].getX()||coord_list_columns.get(indexj)[0].getX()<=coord_list_rows.get(indexi)[0].getX()) {
						Coordinate[][] coord_square = {coord_list_rows.get(indexi),coord_list_columns.get(indexj)};
						merged_rows_and_columns.add(coord_square);
					}
				}
			}
			//
			logger.debug("merging done");

			//draw red squares
		   
			for(int l = 0; l<merged_rows_and_columns.size(); l++) {
				
				int X_left_part = merged_rows_and_columns.get(l)[0][0].getX();
				int X_right_part = merged_rows_and_columns.get(l)[0][1].getX();
				int Y_up_part = merged_rows_and_columns.get(l)[1][0].getY();
				int Y_down_part = merged_rows_and_columns.get(l)[1][1].getY();
				
				drawSquare(trans_calque, new Coordinate(X_left_part,Y_up_part), new Coordinate(X_left_part,Y_down_part), new Coordinate(X_right_part, Y_up_part), new Coordinate(X_right_part,Y_down_part));
					
			}
		    
			//
			
			//Remove cyan color on transparent image
			for(int i=0;i<filtered_img_w;i++) {
				for(int j=0;j<filtered_img_h;j++) {
					if (trans_calque.getRGB(i, j)==Color.cyan.getRGB()) {
						trans_calque.setRGB(i, j, filtered_img.getRGB(i, j));
					}
				}
			}
			//
			
			//recreate filtered image (second layer) and the first layer image (with squares)
			g.setColor(new Color(0,0,0,255));
			if (x>=1 && x<=4) {
				g.drawImage(filtered_img, (x-1)*width/4, 0, null);
				c.drawImage(trans_calque, (x-1)*width/4, 0, null);
			}
			else if (x>=5 && x<=8) {
				g.drawImage(filtered_img, (x-5)*width/4, height/4, null);
				c.drawImage(trans_calque, (x-5)*width/4, height/4, null);
			}
			else if(x>=9 && x<=12) {
				g.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
				c.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
			}
			else {
				g.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
				c.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
			}
			//
			
			
			filtered_img.flush();
			trans_calque.flush();
			
			logger.debug("sticking done");
			
			trans_grph.dispose();
			System.gc();
			
		}
		
		g.dispose();
		c.dispose();
		
		logger.debug("all done");
		Main.label.setIcon(new ImageIcon(bim_full_size));
		
		JLabel label = new JLabel();
		label.setSize(width,height);
		label.setIcon(new ImageIcon(calque_full_size));
		label.setVisible(true);
		//fuse the two layers
		Main.label.add(label);
		
		if(Main.upper_born==16) {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire total: "+Main.nb_square);
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire: "+Main.nb_square);
		}
		
	   	int retouche = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
	   	if(retouche == JOptionPane.YES_OPTION) {
	   		Main.help_button.removeActionListener(Main.help_listener05);
	   		painter(calque_full_size,bim_full_size);
	   	}
	   	else {
	   		try {
				ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre.png"));
				javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
			} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
	   		Main.runetape3.setEnabled(true);
	   	}
	   	
	   	bim_full_size.flush();
	   	
		Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		*/
		
		/*
		//for junit test method 2
		
		Main.method = 2;
		Progress progress_squares1 = new Progress("Initialisation de l'entourage des cellules...");
		progress_squares1.setPercentage(0);
		
		BufferedImage bim_full_size1 = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g1 = bim_full_size1.getGraphics();
		BufferedImage calque_full_size1 = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics c1 = calque_full_size1.getGraphics();
		
		for(int x = 1; x<=16; x++) {
			
			progress_squares1.setTitle("Entourage des cellules de l'image "+x+"/16...");
			progress_squares1.setPercentage(x*31); 
			if(x==16) {progress_squares1.dispose();}
			logger.debug("before_rows");

			BufferedImage filtered_img = null;
			try {
				filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
			} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
			
			int filtered_img_w = filtered_img.getWidth();
			int filtered_img_h = filtered_img.getHeight();
			
			BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
			Graphics trans_grph = trans_calque.getGraphics();
		    trans_grph.setColor(new Color(0,0,0,0));
			
			
		    
		   //iteration on rows and columns
		    
		    int i_depart = 0;
		    int j_depart = 0;
		    int i_arrivee = 0;
		    int j_arrivee = 0;
		    for(int i =0; i < filtered_img_w; i++) {
		    	for(int j=0; j<filtered_img_h;j++) {
	    			//horizontal treatment
		    		if(i-1>=0 && filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i-1,j) == Color.white.getRGB()) {
		    			i_arrivee = 0;
		    			i_depart = i;
		    			j_depart = j;
		    			while(filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i-1,j) == Color.white.getRGB()) {
		    				if(i+1<filtered_img_w) {
		    					i++;
		    					if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
		    				}
		    				else {break;}
		    			}
		    			i_arrivee = i;
		    			while(filtered_img.getRGB(i_depart,j_depart)!=Color.white.getRGB() && trans_calque.getRGB(i_depart,j_depart)!=Color.red.getRGB() && filtered_img.getRGB(i_depart-1,j_depart)==Color.white.getRGB()) {
		    				i=i_depart;
		    				if(j+1<filtered_img_h) {
			    				j++;
			    				j_arrivee = j;
			    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
			    			}
		    				else {j_arrivee = j_depart; break;}
			    		}
		    		}
		    		//vertical treatment
		    		else if(j-1>=0 && filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i,j-1) == Color.white.getRGB()) {
		    			j_arrivee = 0;
		    			i_depart = i;
		    			j_depart = j;
		    			while(filtered_img.getRGB(i, j)!=Color.white.getRGB() && trans_calque.getRGB(i,j)!=Color.red.getRGB() && filtered_img.getRGB(i,j-1) == Color.white.getRGB()) {
		    				if(j+1<filtered_img_h) {
		    					j++;
			    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
		    				}
		    				else {break;}
		    			}
		    			j_arrivee = j;
		    			while(filtered_img.getRGB(i_depart,j_depart)!=Color.white.getRGB() && trans_calque.getRGB(i_depart,j_depart)!=Color.red.getRGB() && filtered_img.getRGB(i_depart,j_depart-1)==Color.white.getRGB()) {
		    				j=j_depart;
		    				if(i+1<filtered_img_w) {
			    				i++;
			    				i_arrivee = i;
			    				if(filtered_img.getRGB(i,j)==Color.white.getRGB()) {break;}
			    			}
		    				else {i_arrivee = i_depart; break;}
			    		}
		    		}
		    		drawSquare(trans_calque, new Coordinate(i_depart,j_depart), new Coordinate(i_depart,j_arrivee), new Coordinate(i_arrivee,j_depart), new Coordinate(i_arrivee,j_arrivee));
		    	}
		    }
		    //
		    
			//recreate filtered image (second layer) and the first layer image (with squares)
			g1.setColor(new Color(0,0,0,255));
			if (x>=1 && x<=4) {
				g1.drawImage(filtered_img, (x-1)*width/4, 0, null);
				c1.drawImage(trans_calque, (x-1)*width/4, 0, null);
			}
			else if (x>=5 && x<=8) {
				g1.drawImage(filtered_img, (x-5)*width/4, height/4, null);
				c1.drawImage(trans_calque, (x-5)*width/4, height/4, null);
			}
			else if(x>=9 && x<=12) {
				g1.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
				c1.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
			}
			else {
				g1.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
				c1.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
			}
			//
			
			
			filtered_img.flush();
			trans_calque.flush();
			
			logger.debug("sticking done");
			
			trans_grph.dispose();
			System.gc();
			
		}
		
		g1.dispose();
		c1.dispose();
		
		logger.debug("all done");
		Main.label.setIcon(new ImageIcon(bim_full_size1));
		
		JLabel label1 = new JLabel();
		label1.setSize(width,height);
		label1.setIcon(new ImageIcon(calque_full_size1));
		label1.setVisible(true);
		
		//fuse the two layers
		Main.label.add(label1);
		
		if(Main.upper_born==16) {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire total: "+Main.nb_square/14058);
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire: "+Main.nb_square/14058);
		}
		
	   	int retouche1 = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
	   	if(retouche1 == JOptionPane.YES_OPTION) {
	   		Main.help_button.removeActionListener(Main.help_listener05);
	   		painter(calque_full_size1,bim_full_size1);
	   	}
	   	else {
	   		try {
				ImageIO.write(calque_full_size1,"png",new File(Main.directory+"image_carre.png"));
				javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
			} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
	   		Main.runetape3.setEnabled(true);
	   	}
	   	
	   	bim_full_size1.flush();
		*/
		
		/*
		//for junit test method 3
		
		Main.method = 3;
		Progress progress_squares = new Progress("Initialisation de l'entourage des cellules...");
		progress_squares.setPercentage(0);
		
		BufferedImage bim_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bim_full_size.getGraphics();
		BufferedImage calque_full_size = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics c = calque_full_size.getGraphics();
		
		for(int x = 1; x<=16; x++) {
			progress_squares.setTitle("Entourage des cellules de l'image "+x+"/16...");
			progress_squares.setPercentage(x*31); 
			if(x==16) {progress_squares.dispose();}
			
			BufferedImage filtered_img = null;
			try {
				filtered_img = ImageIO.read(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
			} catch (IOException e) {logger.fatal("Failed to read image_filtered"+x+".png in ImageModifier.DrawRedSquaresOnImage(). This should never happen.");}
			
			int filtered_img_w = filtered_img.getWidth();
			int filtered_img_h = filtered_img.getHeight();
			
			BufferedImage trans_calque = new BufferedImage(filtered_img_w,filtered_img_h,BufferedImage.TYPE_INT_ARGB);
			Graphics trans_grph = trans_calque.getGraphics();
		    trans_grph.setColor(new Color(0,0,0,0));
		    
			boolean square_near = false;
			ImageAnalyser ia_filtered = null;
			try {
				ia_filtered = new ImageAnalyser(new File(Main.directory+"small_image"+x+"_file"+System.getProperty("file.separator")+"image_filtered"+x+".png"));
			} catch (IOException e) {logger.fatal("image_filtered"+x+"does not exist. This error should never occur.");}
			for(int i=0; i<filtered_img_w; i++) {
				for(int j=0; j<filtered_img_h; j++) {
					try {
						if(!ia_filtered.isLessPurpleORDarker(i, j)) {
							for(int k=-15; k<=15; k++) {
								if(!(i+k > 0 && i+k < filtered_img_w && j+k>0 && j+k < filtered_img_h) || (filtered_img.getRGB(i+k, j) == Color.red.getRGB() || filtered_img.getRGB(i, j+k) == Color.red.getRGB())) {
									square_near = true;
								}
							}
							if(!square_near) {
								drawSquare(filtered_img, new Coordinate(i-15,j-15), new Coordinate(i-15,j+15), new Coordinate(i+15,j-15), new Coordinate(i+15,j+15));
							}
							square_near = false;
						}
					} catch (IOException e) {logger.fatal("filtered_img does not exists. This error should never occur.");}
				}
			}
			//recreate filtered image (second layer) and the first layer image (with squares)
			g.setColor(new Color(0,0,0,255));
			if (x>=1 && x<=4) {
				g.drawImage(filtered_img, (x-1)*width/4, 0, null);
				c.drawImage(trans_calque, (x-1)*width/4, 0, null);
			}
			else if (x>=5 && x<=8) {
				g.drawImage(filtered_img, (x-5)*width/4, height/4, null);
				c.drawImage(trans_calque, (x-5)*width/4, height/4, null);
			}
			else if(x>=9 && x<=12) {
				g.drawImage(filtered_img, (x-9)*width/4, (2*height/4)-1, null);
				c.drawImage(trans_calque, (x-9)*width/4, (2*height/4)-1, null); 
			}
			else {
				g.drawImage(filtered_img, (x-13)*width/4, (3*height/4)-2, null);
				c.drawImage(trans_calque, (x-13)*width/4, (3*height/4)-2, null);
			}
			//
			
			filtered_img.flush();
			trans_calque.flush();
			
			trans_grph.dispose();
			System.gc();
		}
		g.dispose();
		c.dispose();
		
		Main.label.setIcon(new ImageIcon(bim_full_size));
		
		JLabel label2 = new JLabel();
		label2.setSize(width,height);
		label2.setIcon(new ImageIcon(calque_full_size));
		label2.setVisible(true);
		
		//fuse the two layers
		Main.label.add(label2);
		
		if(Main.upper_born==16) {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules provisoire total: "+Main.nb_square);
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules total actuel: "+Main.nb_square);
		}
		
	   	int retouche2 = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous retoucher l'entourage des cellules à ce stade?","Retouchage?",JOptionPane.YES_NO_OPTION);
	   	if(retouche2 == JOptionPane.YES_OPTION) {
	   		Main.help_button.removeActionListener(Main.help_listener05);
	   		painter(calque_full_size,bim_full_size);
	   	}
	   	else {
	   		try {
				ImageIO.write(calque_full_size,"png",new File(Main.directory+"image_carre.png"));
				javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
			} catch (IOException e) {logger.fatal("failed to write image_carre.png in ImageModifier.DrawRedSquaresOnImage()");}
	   		Main.runetape3.setEnabled(true);
	   	}
	   	
	   	bim_full_size.flush();
		
	   	Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	   	*/
	}
	
	/**
	 * @param im : the BufferedImage that will be painted on.
	 * @param under_im: the BufferedImage that contains im (in its Layout).
	 * @precondition im and under_im must exists
	 * @postcondition Dotes the main label of mouse listeners and allows to draw on the image in this label (draws
	 * and erases red squares on im without changing under_im).
	 */
	private void painter(BufferedImage im, BufferedImage under_im) {
		
		Main.enregistrer.setEnabled(false);
		painter_activated = true;
		assert(im!=null):"the calque image can't be null";
		assert(Main.label!=null):"the main label must exist";
		if(!Main.last_step_done) {
			if(Main.upper_born != 16) {
				if(Main.method == 1 || Main.method == 3) {
					if(Main.nb_square>=0) {
						Main.nb_square-=4;
					}
				}
				else if(Main.method == 2) {
					if(Main.nb_square>=0) {
						Main.nb_square-=4*14058;
					}
				}
			}
			else {
				if(Main.method == 1 || Main.method == 3) {
					for(int k = 1; k<=16; k++) {
						if(Main.i == k) {
							if(Main.square_array[k-1] >= 0) {
								Main.square_array[k-1]-=4;
							}
						}
					}
				}
				else if(Main.method == 2) {
					for(int k = 1; k<=16; k++) {
						if(Main.i == k) {
							if(Main.square_array[k-1] >= 0) {
								Main.square_array[k-1]-=4*14058;
							}
						}
					}
				}
			}
		}
		
		resizedImage = under_im;
		resizedUpImage = im;
		Main.label.setIcon(new ImageIcon(resizedImage));
 	    Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
		
		Main.help_listener06 = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				javax.swing.JOptionPane.showMessageDialog(null, "Faites un clic droit sur un carré rouge pour l'effacer et un clic gauche \nà l'endroit ou voulez sur l'image pour en faire apparaître un.\nCliquez sur x1 pour voir l'image à sa taille normale, x2 pour la grossir 2 fois et x4 pour la grossir 4 fois.\nx1/2 et x1/4 permettent, respectivement de dézoomer 2 et 4 fois.\nCliquez sur les boutons avec des carrés de tailles différentes pour changer la taille des carrés que vous créez ou effacez.\nCliquez sur compter carrés pour savoir combien de cellules vous avez sur l'image.\nCliquez sur effacer tous les carrés pour remettre le nombre de cellules à 0 (et effacer les carrés).\n Séléctionnez un groupe de carrés puis cliquez sur grouper carrés pour n'en faire qu'un seul carré.\n Cliquez sur terminer la correction pour passer à l'étape suivante.");
			}
		};
		
		Main.help_button.addActionListener(Main.help_listener06);
		
		
		mAdapter =  new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               //erase square on right click
               if (e.getButton() == MouseEvent.BUTTON3) {
            	   int w_r = width*ratio;
            	   int h_r = height*ratio;
            	   int s2_r = square_size*2*ratio;
            	   if(ratio == 1/2) {w_r = width/2; h_r = height/2;s2_r = square_size;}
            	   if(ratio == 1/4) {w_r = width/4; h_r = height/4;s2_r = square_size/2;}
            	   int Xpos = e.getX();
    	           int Ypos = e.getY();
    	           if((Xpos-square_size*2*ratio > 0 || Xpos+s2_r<w_r || Ypos-s2_r > 0 || Ypos+s2_r < h_r) && resizedUpImage.getRGB(Xpos, Ypos)==Color.red.getRGB()) {
    	        	   for(int w = Xpos-s2_r;w<=Xpos+s2_r;w++) {
    	        		   for(int h= Ypos-s2_r;h<=Ypos+s2_r;h++) {
    	        			   if(w>=0 && h>=0 && w<w_r && h<h_r) {
    	        				   resizedUpImage.setRGB(w, h, new Color(new Color(resizedImage.getRGB(w, h)).getRed(),new Color(resizedImage.getRGB(w, h)).getGreen(),new Color(resizedImage.getRGB(w, h)).getBlue(),255).getRGB());
    	        			   }//set color translucent
    	        		   }
    	        	   }
    	        	   
    	        	   if(Main.upper_born!=16) {
	    	        	   if(Main.method == 1 || Main.method == 3) {
	    	        		   if(Main.nb_square>0) {
									Main.nb_square--;
								}
								else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");} 
	    	        	   }
	    	        	   
	    	        	   else if(Main.method == 2) {
	    	        		   if(Main.nb_square>14058) {
	    	        			   Main.nb_square -= 14058;
	    	        		   }
	    	        		   else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");} 
	    	        	   }
    	        	   }
    	        	   else {
    	        		   if(Main.method == 1 || Main.method == 3) {
    	        			   for(int k=1; k<=16; k++) {
    	        				   if(Main.i == k) {
    	        					   if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
	    	        					   if(Main.square_array[k-1]>0) {
	    										Main.square_array[k-1]--;
	    	        					   }
	    	        					   else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");}  
    	        					   }
    	        					   else if(Main.last_step_done && Main.i!=16){
    	        						   if(Main.square_array[k-2]>0) {
	    										Main.square_array[k-2]--;
	    	        					   } 
    	        						   else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");}  
    	        					   }
    	        				   }
    	        			   }
	    	        	   }
	    	        	   
	    	        	   else if(Main.method == 2) {
	    	        		   for(int k=1; k<=16; k++) {
    	        				   if(Main.i == k) {
    	        					   if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
	    	        					   if(Main.square_array[k-1]>14058) {
	    										Main.square_array[k-1]-=14058;
	    									}
	    									else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");}  
    	        				   
    	        					  }
    	        					   else if(Main.last_step_done && Main.i!=16){ 
    	        						   if(Main.square_array[k-2]>14058) {
    	        							   Main.square_array[k-2]-=14058;
    	        						   }
    	        						   else {logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");}  
   									  }
    	        				 } 
	    	        	   }
    	        	   }
    	        	   Main.label.setIcon(new ImageIcon(resizedImage));
    	        	   Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
    	           }

    	           }
               }
               //draw square on left click
               else if(e.getButton()==MouseEvent.BUTTON1){
            	   int s_r = square_size*ratio;
            	   int w_r = width*ratio;
            	    int h_r = height*ratio;
            	   if(ratio == 1/2) {s_r = square_size/2;w_r = width/2; h_r = height/2;}
            	   if(ratio == 1/4) {s_r = square_size/4;w_r = width/4; h_r = height/4;}
            	   if(e.getX()-s_r>0 && e.getY()-s_r>0 && e.getX()+s_r < w_r && e.getY() + s_r < h_r) {
            		   drawSquare(resizedUpImage,new Coordinate(e.getX()-s_r,e.getY()-s_r),new Coordinate(e.getX()-s_r,e.getY()+s_r),new Coordinate(e.getX()+s_r,e.getY()-s_r),new Coordinate(e.getX()+s_r,e.getY()+s_r));
            	   }
            	   Main.label.setIcon(new ImageIcon(resizedImage));
	        	   Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
               }
            }
        };
        
        int w_r = width*ratio;
 	    int h_r = height*ratio;
	 	if(ratio == 1/2) {w_r = width/2; h_r = height/2;}
	 	if(ratio == 1/4) {w_r = width/4; h_r = height/4;}
        i_min = w_r;
		j_min = h_r;
		i_max = 0;
		j_max = 0;
		
        MouseMotionListener mlistener = new MouseMotionListener() {
			

			@Override
			public void mouseDragged(MouseEvent e) {
				int w_r = width*ratio;
			 	int h_r = height*ratio;
				if(ratio == 1/2) {w_r = width/2; h_r = height/2;}
				 if(ratio == 1/4) {w_r = width/4; h_r = height/4;}
				if(e.getX()>0 && e.getX()<w_r && e.getY()>0 && e.getX()<h_r) {
					if(e.getX()<=i_min) {i_min = e.getX();}
					if(e.getX()>i_max) {i_max = e.getX();}
					if(e.getY()<=j_min) {j_min = e.getY();}
					if(e.getY()>j_max) {j_max = e.getY();}
				}
				resizedUpImage.setRGB(e.getX(), e.getY(), Color.black.getRGB());
				Main.label.setIcon(new ImageIcon(resizedImage));
	        	Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
				grouper.setEnabled(true);
 				
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub	
			}

		};
		Main.label.addMouseMotionListener(mlistener);
		
		Main.label.addMouseListener(mAdapter);
  	   
		eraseAllF = new JFrame();
		eraseAllF.setSize(200,100);
		eraseAllF.setLocation(1000, 500);
		eraseAllF.setVisible(true);
		
		JLabel eraseAllL = new JLabel();
		
		JButton eraseAll = new JButton("effacer tous les carrés");
		eraseAll.setSize(200,100);
		eraseAllL.add(eraseAll);
		eraseAllF.add(eraseAllL);
		
		eraseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Progress erase_progress = new Progress("Initialisation...");
				erase_progress.setPercentage(0);
				if(!Main.last_step_done) {
					int w_r = width*ratio;
					int h_r = height*ratio;
					if(ratio == 1/2) {w_r = width/2; h_r = height/2;}
					if(ratio == 1/4) {w_r = width/4; h_r = height/4;}
					for(int i = 0; i<w_r; i++) {
						for(int j = 0; j<h_r; j++) {
							if(i == w_r/5) {erase_progress.setTitle("Effaçage: 20%");erase_progress.setPercentage(100);}
							else if(i == w_r/4) {erase_progress.setTitle("Effaçage: 25%");erase_progress.setPercentage(125);}
							else if(i == w_r/3) {erase_progress.setTitle("Effaçage : 33%");erase_progress.setPercentage(167);}
							else if(i == w_r/2) {erase_progress.setTitle("Effaçage: 50%");erase_progress.setPercentage(250);}
							else if(i >= w_r*1.5 && i<w_r*1.33) {erase_progress.setTitle("Effaçage: 67%");erase_progress.setPercentage(330);}
							else if(i >= w_r*1.33 && i<w_r-1) {erase_progress.setTitle("Effaçage: 75%");erase_progress.setPercentage(375);}
							else if(i == w_r-1 && j == h_r-1) {erase_progress.setTitle("Effaçage: 100%");erase_progress.setPercentage(499);erase_progress.dispose();}
							if(resizedUpImage.getRGB(i, j) == Color.red.getRGB()) {
								resizedUpImage.setRGB(i, j, new Color(new Color(resizedImage.getRGB(i, j)).getRed(),new Color(resizedImage.getRGB(i, j)).getGreen(),new Color(resizedImage.getRGB(i, j)).getBlue(),0).getRGB());
							}
						}
					}
				}
				else {
					if(ratio!=1) {
						javax.swing.JOptionPane.showMessageDialog(null, "Veuillez sélectionner un zoom de 1x");
					}
					else {
						for(int i = 0; i<width; i++) {
							for(int j = 0; j<height; j++) {
								if(i == width/5) {erase_progress.setTitle("Effaçage: 20%");erase_progress.setPercentage(100);}
								else if(i == width/4) {erase_progress.setTitle("Effaçage: 25%");erase_progress.setPercentage(125);}
								else if(i == width/3) {erase_progress.setTitle("Effaçage : 33%");erase_progress.setPercentage(167);}
								else if(i == width/2) {erase_progress.setTitle("Effaçage: 50%");erase_progress.setPercentage(250);}
								else if(i >= width*1.5 && i<width*1.33) {erase_progress.setTitle("Effaçage: 67%");erase_progress.setPercentage(330);}
								else if(i >= width*1.33 && i<width-1) {erase_progress.setTitle("Effaçage: 75%");erase_progress.setPercentage(375);}
								else if(i == width-1 && j == height-1) {erase_progress.setTitle("Effaçage: 100%");erase_progress.setPercentage(499);erase_progress.dispose();}
								if(resizedUpImage.getRGB(i, j) == Color.red.getRGB()) {
									resizedUpImage.setRGB(i, j, new Color(new Color(resizedImage.getRGB(i, j)).getRed(),new Color(resizedImage.getRGB(i, j)).getGreen(),new Color(resizedImage.getRGB(i, j)).getBlue(),0).getRGB());
								}
								else if(resizedImage.getRGB(i, j) == Color.red.getRGB()) {
									resizedImage.setRGB(i, j,bimage.getRGB(i, j));
								}
							}
						}
					}
				}
				Main.label.removeAll();
				Main.label.setIcon(new ImageIcon(resizedImage));
	        	Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
				if(Main.upper_born!=16) {
					Main.nb_square = 0;
				}
				else{
					if(!Main.last_step_done || (Main.last_step_done && Main.i == 16)) {
						Main.square_array[Main.i-1] = 0;
					}
					else if(Main.last_step_done && Main.i!=16){
						Main.square_array[Main.i-2] = 0;
					}
				}
			}
		});
		
		groupSquaresF = new JFrame();
		groupSquaresF.setSize(200,100);
		groupSquaresF.setLocation(1000, 50);
		groupSquaresF.setVisible(true);
		
		JLabel groupSquaresL = new JLabel();
		
		grouper = new JButton("grouper carrés");
		grouper.setSize(200,100);
		grouper.setEnabled(false);
		
		groupSquaresL.add(grouper);
		groupSquaresF.add(groupSquaresL);
		
		grouper.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				
				grouper.setEnabled(false);
				
				int w_r = width*ratio;
				int h_r = height*ratio;
				int s_r = (square_size+8)*ratio;
				if(ratio == 1/2) {w_r = width/2; h_r = height/2; s_r = (square_size+8)/2;}
				if(ratio == 1/4) {w_r = width/4; h_r = height/4; s_r = (square_size+8)/4;}
				//erases the red square that is not needed 
				if(i_min - s_r > 0 && j_min - s_r > 0 && i_min+s_r < w_r && j_min + s_r < h_r) {
					for(int border_x = i_min - s_r; border_x <= i_min + s_r; border_x++) {
						for(int border_y = j_min - s_r; border_y <= j_min + s_r; border_y++) {
							resizedUpImage.setRGB(border_x, border_y, new Color(new Color(resizedImage.getRGB(border_x, border_y)).getRed(),new Color(resizedImage.getRGB(border_x, border_y)).getGreen(),new Color(resizedImage.getRGB(border_x, border_y)).getBlue(),0).getRGB());
						}
					}
				}
				if(i_min - s_r > 0 && j_max + s_r < h_r) {
					for(int border_x = i_min - s_r; border_x <= i_min + s_r; border_x++) {
						for(int border_y = j_max - s_r; border_y <= j_max + s_r; border_y++) {
							resizedUpImage.setRGB(border_x, border_y, new Color(new Color(resizedImage.getRGB(border_x, border_y)).getRed(),new Color(resizedImage.getRGB(border_x, border_y)).getGreen(),new Color(resizedImage.getRGB(border_x, border_y)).getBlue(),0).getRGB());
						}
					}
				}
				if(i_max + s_r < w_r && j_min - s_r > 0) {
					for(int border_x = i_max - s_r; border_x <= i_max + s_r; border_x++) {
						for(int border_y = j_min - s_r; border_y <= j_min + s_r; border_y++) {
							resizedUpImage.setRGB(border_x, border_y, new Color(new Color(resizedImage.getRGB(border_x, border_y)).getRed(),new Color(resizedImage.getRGB(border_x, border_y)).getGreen(),new Color(resizedImage.getRGB(border_x, border_y)).getBlue(),0).getRGB());
						}
					}
				}
				if(i_max + s_r < w_r && j_max + s_r < h_r) {
					for(int border_x = i_max - s_r; border_x <= i_max + s_r; border_x++) {
						for(int border_y = j_max - s_r; border_y <= j_max + s_r; border_y++) {
							resizedUpImage.setRGB(border_x, border_y, new Color(new Color(resizedImage.getRGB(border_x, border_y)).getRed(),new Color(resizedImage.getRGB(border_x, border_y)).getGreen(),new Color(resizedImage.getRGB(border_x, border_y)).getBlue(),0).getRGB());
						}
					}
				}
				//
				
				if(Main.upper_born != 16) {
				    if(Main.method == 1 || Main.method == 3) {
						if(Main.nb_square>0) {
							Main.nb_square--;
						}
						else {
							logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
							javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
						} 
					}
					else if(Main.method == 2) {
						if(Main.nb_square>14058) {
							Main.nb_square-=14058;
						}
						else {
							logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
							javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
						} 
					}
				}
				else {
				    if(Main.method == 1 || Main.method == 3) {
				    	for(int k=1; k<=16; k++) {
				    		if(Main.i == k) {
				    			if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
									if(Main.square_array[k-1]>0) {
										Main.square_array[k-1]--;
									}
									else {
										logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
										javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
									} 
				    			}
				    			else if(Main.last_step_done && Main.i!=16){
				    				if(Main.square_array[k-2]>0) {
										Main.square_array[k-2]--;
									}
									else {
										logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
										javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
									} 
				    			}
				    		}
				    	}
					}
					else if(Main.method == 2) {
						for(int k=1; k<=16; k++) {
				    		if(Main.i == k) {
				    			if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
									if(Main.square_array[k-1]>14058) {
										Main.square_array[k-1]-=14058;
									}
									else {
										logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
										javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
									} 
				    			}
				    			else if(Main.last_step_done && Main.i!=16){
				    				if(Main.square_array[k-2]>14058) {
										Main.square_array[k-2]-=14058;
									}
									else {
										logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
										javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
									} 
				    			}
				    		}
				    	}
					}
				}
				
				for(int w= i_min; w <= i_max; w++) {
					for(int h = j_min; h <= j_max; h++) {
						if(resizedUpImage.getRGB(w, h) == Color.red.getRGB() && resizedUpImage.getRGB(w, h+1) != Color.red.getRGB()) {
							if(Main.method == 1 || Main.method == 3) {
								if(Main.upper_born!=16) {
									if(Main.nb_square>0) {
										Main.nb_square--;
									}
									else {
										logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
										javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
									} 
								}
								else {
									for(int k=1; k<=16; k++) {
							    		if(Main.i == k) {
							    			if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
												if(Main.square_array[k-1]>0) {
													Main.square_array[k-1]--;
												}
												else {
													logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
													javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
												} 
							    			}
							    			else if(Main.last_step_done && Main.i!=16){
							    				if(Main.square_array[k-2]>0) {
													Main.square_array[k-2]--;
												}
												else {
													logger.fatal("Nombre de cellules à 0! Vous ne pouvez pas descendre plus"); 
													javax.swing.JOptionPane.showMessageDialog(null,"Nombre de cellules à 0! Vous ne pouvez pas descendre plus");
												} 
							    			}
							    		}
							    	}
								}
								if(resizedUpImage.getRGB(w+1,h) == Color.red.getRGB()) {
									while(resizedUpImage.getRGB(w+1,h) == Color.red.getRGB()) {
										w++;
									}
								}
							}
						}
					}
				}
				String number_cells;
				if(Main.method == 2) {
					number_cells = javax.swing.JOptionPane.showInputDialog(null,"Combien de cellules ont été entourées dans cette zone?");
					if(!number_cells.contains("0") && !number_cells.contains("1") && !number_cells.contains("2") && !number_cells.equals("3") && !number_cells.equals("4") && !number_cells.equals("5") && !number_cells.equals("6") && !number_cells.equals("7") && !number_cells.equals("8") && !number_cells.equals("9")) {
						while(!number_cells.contains("0") && !number_cells.contains("1") && !number_cells.contains("2") && !number_cells.equals("3") && !number_cells.equals("4") && !number_cells.equals("5") && !number_cells.equals("6") && !number_cells.equals("7") && !number_cells.equals("8") && !number_cells.equals("9")) {
							number_cells = javax.swing.JOptionPane.showInputDialog(null,"Combien de cellules ont été entourées dans cette zone? (veuillez inscrire un nombre)");
						}
					}
					if(Main.upper_born != 16) {
						if(Main.nb_square > Integer.valueOf(number_cells)*14058) {
							Main.nb_square -= Integer.valueOf(number_cells)*14058;
						}
						else {
							logger.fatal("Vous ne pouvez pas descendre plus bas le nombre de cellules!");
							javax.swing.JOptionPane.showMessageDialog(null,"Vous ne pouvez pas descendre plus");
						}
					}
					else {
						if(!Main.last_step_done || (Main.last_step_done && Main.i == 16)) {
							if(Main.square_array[Main.i-1]>Integer.valueOf(number_cells)*14058) {
								Main.square_array[Main.i-1]-=Integer.valueOf(number_cells)*14058;
							}
							else {
								logger.fatal("Vous ne pouvez pas descendre plus bas le nombre de cellules"); 
								javax.swing.JOptionPane.showMessageDialog(null,"Vous ne pouvez pas descendre plus");
							} 
						}
						else if(Main.last_step_done && Main.i!=16){
							if(Main.square_array[Main.i-2]>Integer.valueOf(number_cells)*14058) {
								Main.square_array[Main.i-2]-=Integer.valueOf(number_cells)*14058;
							}
							else {
								logger.fatal("Vous ne pouvez pas descendre plus bas le nombre de cellules"); 
								javax.swing.JOptionPane.showMessageDialog(null,"Vous ne pouvez pas descendre plus");
							} 
						}
					}
				}
				for(int w= i_min; w <= i_max; w++) {
					for(int h = j_min; h <= j_max; h++) {
						resizedUpImage.setRGB(w, h, new Color(new Color(resizedImage.getRGB(w, h)).getRed(),new Color(resizedImage.getRGB(w, h)).getGreen(),new Color(resizedImage.getRGB(w, h)).getBlue(),0).getRGB());
		 			    //set color translucent
					}
				}
				drawSquare(resizedUpImage,new Coordinate(i_min, j_min), new Coordinate(i_min,j_max), new Coordinate(i_max,j_min), new Coordinate(i_max,j_max));
				Main.label.setIcon(new ImageIcon(resizedImage));
	        	Main.label.add(new JLabel(new ImageIcon(resizedUpImage)));
				i_min = w_r;
				j_min = h_r;
				i_max = 0;
				j_max = 0;
				
					
			}
		});
		
		squareCountF = new JFrame();
		squareCountF.setSize(200,100);
		squareCountF.setLocation(1000,400);
		squareCountF.setVisible(true);
		
		JLabel squareCountL = new JLabel();
		
		JButton compter = new JButton("compter carrés");
		compter.setSize(200,100);
		
		squareCountL.add(compter);
		squareCountF.add(squareCountL);
		
		compter.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		if(Main.upper_born == 16) {
	    			if(!Main.last_step_done || (Main.last_step_done && Main.i==16)) {
		    			if(Main.method == 1 || Main.method == 3) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+Main.i+" : "+Main.square_array[Main.i-1]);
		    			}
		    			else if(Main.method == 2) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+Main.i +" : "+Main.square_array[Main.i-1]/14058);
		    			}
	    			}
	    			else if(Main.last_step_done && Main.i!=16){
	    				if(Main.method == 1 || Main.method == 3) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+String.valueOf(Main.i-1)+" : "+Main.square_array[Main.i-2]);
		    			}
		    			else if(Main.method == 2) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+String.valueOf(Main.i-1)+" : "+Main.square_array[Main.i-2]/14058);
		    			}
	    			}
	    		}
	    		else {
	    			if(Main.method == 1 || Main.method == 3) {
	    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel: "+Main.nb_square);
	    			}
	    			else if(Main.method == 2) {
	    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel"+Main.nb_square/14058);
	    			}
	    		}
	    	}
		});
		
		squareOptionsF = new JFrame();
		squareOptionsF.setLocation(1000,250);
		squareOptionsF.setSize(400, 150);
		squareOptionsF.setTitle("Taille des carrés");
		squareOptionsF.setVisible(true);
		
		JPanel squareOptionsP = new JPanel();
		
		BufferedImage ten_image = new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
		drawSquare(ten_image,new Coordinate(0,0),new Coordinate(0,9),new Coordinate(9,0),new Coordinate(9,9));
		BufferedImage twenty_image = new BufferedImage(20,20,BufferedImage.TYPE_INT_RGB);
		drawSquare(twenty_image,new Coordinate(0,0),new Coordinate(0,19),new Coordinate(19,0),new Coordinate(19,19));
		BufferedImage thirty_image = new BufferedImage(30,30,BufferedImage.TYPE_INT_RGB);
		drawSquare(thirty_image,new Coordinate(0,0),new Coordinate(0,29),new Coordinate(29,0),new Coordinate(29,29));
		BufferedImage fourty_image = new BufferedImage(40,40,BufferedImage.TYPE_INT_RGB);
		drawSquare(fourty_image,new Coordinate(0,0),new Coordinate(0,39),new Coordinate(39,0),new Coordinate(39,39));
		JButton ten_size = new JButton("10x10");
		ten_size.setSize(100,100);
		ten_size.setIcon(new ImageIcon(ten_image));
		JButton twenty_size = new JButton("20x20");
		twenty_size.setSize(100,100);
		twenty_size.setIcon(new ImageIcon(twenty_image));
		JButton thirty_size = new JButton("30x30");
		thirty_size.setSize(100,100);
		thirty_size.setIcon(new ImageIcon(thirty_image));
		
		squareOptionsP.add(ten_size);
		squareOptionsP.add(twenty_size);
		squareOptionsP.add(thirty_size);
		
		squareOptionsF.add(squareOptionsP);
		
		ten_size.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		square_size=10;
	    	}
		});
		
		twenty_size.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		square_size=20;
	    	}
		});
		
		thirty_size.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		square_size=30;
	    	}
		});
		
		
		
	    zoomOptionsF = new JFrame();
	    zoomOptionsF.setLocation(1000,150);
	    zoomOptionsF.setSize(300,100);
	    zoomOptionsF.setTitle("Options de zoomage");
	    zoomOptionsF.setVisible(true);
	    
	    JButton zoomplus2 = new JButton("x2");
	    zoomplus2.setSize(100,100);
	    JButton zoomplus4 = new JButton("x4");
	    zoomplus4.setSize(100,100);
	    JButton zoomnormal = new JButton("x1");
	    zoomnormal.setSize(100,100);
	    JButton zoommoins2 = new JButton("x1/2");
	    zoommoins2.setSize(100,100);
	    JButton zoommoins4 = new JButton("x1/4");
	    zoommoins4.setSize(100,100);
	    
	    JPanel zoomOptionsP =  new JPanel();
	    
	    zoomOptionsP.add(zoomplus2);
	    zoomOptionsP.add(zoomnormal);
	    zoomOptionsP.add(zoomplus4);
	    zoomOptionsP.add(zoommoins2);
	    zoomOptionsP.add(zoommoins4);
	    
	    zoomOptionsF.add(zoomOptionsP);
	    
	    zoomnormal.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		zoomplus2.setEnabled(true);
	    		zoomplus4.setEnabled(true);
	    		zoommoins2.setEnabled(true);
	    		zoommoins4.setEnabled(true);
	    		BufferedImage resizedUpImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    		BufferedImage resizedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2dup = resizedUpImage.createGraphics();
	    		g2dup.setColor(new Color(0,0,0,255));
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2dup.drawImage(ImageModifier.resizedUpImage,0,0,width,height,null);
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width, height, null);
	    		g2d.dispose();
	    		g2dup.dispose();
	    		Main.label.removeAll();
	    		ImageModifier.resizedImage = resizedImage;
	    		ImageModifier.resizedUpImage = resizedUpImage;
	    		JLabel up_label = new JLabel();
	    		up_label.setIcon(new ImageIcon(resizedUpImage));
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		Main.label.add(up_label);
	    		zoomnormal.setEnabled(false);
	    		ratio = 1;
	    		thirty_size.setEnabled(true);
	    	}
	     }
	    );
	    
	    zoomplus2.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent event) {
		    		zoomnormal.setEnabled(true);
		    		zoomplus4.setEnabled(true);
		    		zoommoins2.setEnabled(true);
		    		zoommoins4.setEnabled(true);
		    		BufferedImage resizedUpImage = new BufferedImage(width*2,height*2,BufferedImage.TYPE_INT_ARGB);
		    		BufferedImage resizedImage = new BufferedImage(width*2,height*2,BufferedImage.TYPE_INT_ARGB);
		    		Graphics2D g2dup = resizedUpImage.createGraphics();
		    		g2dup.setColor(new Color(0,0,0,255));
		    		Graphics2D g2d = resizedImage.createGraphics();
		    		g2dup.drawImage(ImageModifier.resizedUpImage,0,0,width*2,height*2,null);
		    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width*2, height*2, null);
		    		g2d.dispose();
		    		g2dup.dispose();
		    		Main.label.removeAll();
		    		ImageModifier.resizedImage = resizedImage;
		    		ImageModifier.resizedUpImage = resizedUpImage;
		    		JLabel up_label = new JLabel();
		    		up_label.setIcon(new ImageIcon(resizedUpImage));
		    		Main.label.setIcon(new ImageIcon(resizedImage));
		    		Main.label.add(up_label);
		    		zoomplus2.setEnabled(false);
		    		ratio = 2;
		    		thirty_size.setEnabled(true);
		    	}
	    	}
	    );
		
	    zoomplus4.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		zoomnormal.setEnabled(true);
	    		zoomplus2.setEnabled(true);
	    		zoommoins2.setEnabled(true);
	    		zoommoins4.setEnabled(true);
	    		BufferedImage resizedUpImage = new BufferedImage(width*4,height*4,BufferedImage.TYPE_INT_ARGB);
	    		BufferedImage resizedImage = new BufferedImage(width*4,height*4,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2dup = resizedUpImage.createGraphics();
	    		g2dup.setColor(new Color(0,0,0,255));
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2dup.drawImage(ImageModifier.resizedUpImage,0,0,width*4,height*4,null);
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width*4, height*4, null);
	    		g2d.dispose();
	    		g2dup.dispose();
	    		Main.label.removeAll();
	    		ImageModifier.resizedImage = resizedImage;
	    		ImageModifier.resizedUpImage = resizedUpImage;
	    		JLabel up_label = new JLabel();
	    		up_label.setIcon(new ImageIcon(resizedUpImage));
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		Main.label.add(up_label);
	    		zoomplus4.setEnabled(false);
	    		ratio = 4;
	    		thirty_size.setEnabled(false);
	    	}
    	 }
	    );
	    
	    zoommoins2.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		zoomnormal.setEnabled(true);
	    		zoomplus2.setEnabled(true);
	    		zoomplus4.setEnabled(true);
	    		zoommoins4.setEnabled(true);
	    		BufferedImage resizedUpImage = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_ARGB);
	    		BufferedImage resizedImage = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2dup = resizedUpImage.createGraphics();
	    		g2dup.setColor(new Color(0,0,0,255));
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2dup.drawImage(ImageModifier.resizedUpImage,0,0,width/2,height/2,null);
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width/2, height/2, null);
	    		g2d.dispose();
	    		g2dup.dispose();
	    		Main.label.removeAll();
	    		ImageModifier.resizedImage = resizedImage;
	    		ImageModifier.resizedUpImage = resizedUpImage;
	    		JLabel up_label = new JLabel();
	    		up_label.setIcon(new ImageIcon(resizedUpImage));
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		Main.label.add(up_label);
	    		zoommoins2.setEnabled(false);
	    		ratio = 1/2;
	    		thirty_size.setEnabled(true);
	    	}
    	 }
	    );
	    
	    zoommoins4.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		zoomnormal.setEnabled(true);
	    		zoomplus2.setEnabled(true);
	    		zoomplus4.setEnabled(true);
	    		zoommoins2.setEnabled(true);
	    		BufferedImage resizedUpImage = new BufferedImage(width/4,height/4,BufferedImage.TYPE_INT_ARGB);
	    		BufferedImage resizedImage = new BufferedImage(width/4,height/4,BufferedImage.TYPE_INT_ARGB);
	    		Graphics2D g2dup = resizedUpImage.createGraphics();
	    		g2dup.setColor(new Color(0,0,0,255));
	    		Graphics2D g2d = resizedImage.createGraphics();
	    		g2dup.drawImage(ImageModifier.resizedUpImage,0,0,width/4,height/4,null);
	    		g2d.drawImage(ImageModifier.resizedImage, 0, 0, width/4, height/4, null);
	    		g2d.dispose();
	    		g2dup.dispose();
	    		Main.label.removeAll();
	    		ImageModifier.resizedImage = resizedImage;
	    		ImageModifier.resizedUpImage = resizedUpImage;
	    		JLabel up_label = new JLabel();
	    		up_label.setIcon(new ImageIcon(resizedUpImage));
	    		Main.label.setIcon(new ImageIcon(resizedImage));
	    		Main.label.add(up_label);
	    		zoommoins4.setEnabled(false);
	    		ratio = 1/4;
	    		thirty_size.setEnabled(true);
	    	}
    	 }
	    );
	    
	    end_fr = new JFrame();
		end_fr.setLocation(1000,600);
		end_fr.setSize(300,100);
		end_fr.setVisible(true);
		
		JLabel end_lab = new JLabel();
		
		JButton end_button = new JButton("terminer la correction");
		end_button.setSize(300,100);
		
		end_lab.add(end_button);
		end_fr.add(end_lab);
		
		end_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Main.help_button.removeActionListener(Main.help_listener06);
				if(ratio==1) {
					if((Main.upper_born == 16 && !Main.last_step_done) || (Main.upper_born == 16 && Main.last_step_done && Main.i == 16)) {
						try {
							ImageIO.write(resizedUpImage,"png",new File(Main.directory+"image_carre"+Main.i+".png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre"+Main.i+".png");
						} catch (IOException e) {logger.fatal("IOException in ImageModifier.painter(BufferedImage,BufferedImage). This should never happen");}
					}
					else if(Main.upper_born == 16 && Main.last_step_done && Main.i != 16) {
						try {
							ImageIO.write(resizedUpImage,"png",new File(Main.directory+"image_carre"+String.valueOf(Main.i-1)+".png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre"+String.valueOf(Main.i-1)+".png");
						} catch (IOException e) {logger.fatal("IOException in ImageModifier.painter(BufferedImage,BufferedImage). This should never happen");}
					}
					else if(Main.upper_born!=16){
						try {
							ImageIO.write(resizedUpImage,"png",new File(Main.directory+"image_carre.png"));
							javax.swing.JOptionPane.showMessageDialog(null,"Image du calque avec les carrés sur l'image enregistrée sous "+Main.directory+"image_carre.png");
						} catch (IOException e) {logger.fatal("IOException in ImageModifier.painter(BufferedImage,BufferedImage). This should never happen");}
					}
					Main.label.setIcon(new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)));
					if(!Main.last_step_done) {
						Main.runetape3.setEnabled(true);
					}
					Main.label.removeMouseListener(mAdapter);
					Main.label.removeAll();
					end_fr.dispose();
					zoomOptionsF.dispose();
					squareOptionsF.dispose();
					squareCountF.dispose();
					eraseAllF.dispose();
					groupSquaresF.dispose();
			   		if((Main.upper_born == 16 && !Main.last_step_done) || (Main.upper_born == 16 && Main.last_step_done && Main.i==16)) {
			   			if(Main.method == 1 || Main.method == 3) {
			   				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+Main.i+" : "+Main.square_array[Main.i-1]);
		    			}
		    			else if(Main.method == 2) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+Main.i +" : "+Main.square_array[Main.i-1]/14058);
		    			}
		    		}
			   		else if(Main.upper_born == 16 && Main.last_step_done && Main.i!=16) {
			   			if(Main.method == 1 || Main.method == 3) {
			   				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+String.valueOf(Main.i-1)+" : "+Main.square_array[Main.i-2]);
		    			}
		    			else if(Main.method == 2) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+String.valueOf(Main.i-1)+" : "+Main.square_array[Main.i-2]/14058);
		    			}
		    		}
		    		else{
		    			if(Main.method == 1 || Main.method == 3) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel: "+Main.nb_square);
		    			}
		    			else if(Main.method == 2) {
		    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel : "+Main.nb_square/14058);
		    			}
		    		}
				}
				else {
					if(Main.last_step_done) {
						Main.label.setIcon(new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)));
						Main.label.removeMouseListener(mAdapter);
						Main.label.removeAll();
						end_fr.dispose();
						zoomOptionsF.dispose();
						squareOptionsF.dispose();
						squareCountF.dispose();
						eraseAllF.dispose();
						groupSquaresF.dispose();
						if(Main.upper_born == 16) {
				   			if(Main.method == 1 || Main.method == 3) {
				   				if(Main.i != 16) {
				   					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image: "+String.valueOf(Main.i-1)+" : "+Main.square_array[Main.i-2]);
				   				}
				   				if(Main.i == 16) {
				   					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image: "+Main.i+" : "+Main.square_array[Main.i-1]);
				   					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules total: "+Main.nb_square);
				   				}
				   			}
			    			else if(Main.method == 2) {
			    				if(Main.i != 16) {
			    					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+String.valueOf(Main.i-1) +" : "+Main.square_array[Main.i-2]/14058);
			    				}
			    				if(Main.i == 16) {
			    					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules de la sous-image "+Main.i +" : "+Main.square_array[Main.i-1]/14058);
				   					javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules total: "+Main.nb_square);
				   				}
			    			}
				   			//Enables all the possibilities of return
							for(int j=0;j<16;j++) {
								Main.images[j].setEnabled(true);
							}
			    		}
			    		else {
			    			if(Main.method == 1 || Main.method == 3) {
			    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel: "+Main.nb_square);
			    			}
			    			else if(Main.method == 2) {
			    				javax.swing.JOptionPane.showMessageDialog(null, "Nombre de cellules actuel : "+Main.nb_square/14058);
			    			}
			    		}
					}
					else {
						javax.swing.JOptionPane.showMessageDialog(null,"Veuillez sélectionner un zoom de 1x pour enregistrer l'image et passer à l'étape suivante.");
					}
				}
			}
		});
		
	    Main.mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * @throws IOException is square_file is null
	 * @precondition /
	 * @postcondition Redraws the squares on bimage (fuse the square image with bimage)
	 */
	public void mergeImages() throws IOException{
		
		Main.help_button.removeActionListener(Main.help_listener05);
		painter_activated = false;
		Progress progress_merging = new Progress("Initialisation de la fusion de l'image avec les carrés...");
		progress_merging.setPercentage(0);
		full_image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		higher_image_full =  new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		
		File square_file = null;
		if(Main.upper_born != 16) {
			square_file = new File(Main.directory+"image_carre.png");
		}
		else {
			square_file = new File(Main.directory+"image_carre"+Main.i+".png");
		}
		cutImage(square_file,"image_carre");
		for(int k=1; k<=16;k++) {
			progress_merging.setTitle("Fusion de la sous-image "+k+"/16 avec les carrés...");
			progress_merging.setPercentage(k*31);
			if(k==16) {progress_merging.dispose();}
			BufferedImage smim = null;
			BufferedImage small_image = null;
			try {
				smim = ImageIO.read(new File(Main.directory+"small_image"+k+"_file"+System.getProperty("file.separator")+"image_carre"+k+".png"));
				small_image = ImageIO.read(new File(Main.directory+"small_image"+k+"_file"+System.getProperty("file.separator")+"small_image"+k+".png"));
			} catch (IOException e) {logger.fatal("IOException! Failed to read "+Main.directory+"small_image"+k+"_file"+System.getProperty("file.separator")+"image_carre"+k+".png in ImageModifier.painter(BufferedImage,BufferedImage)");}
			Graphics g = full_image.getGraphics();
			Graphics u = higher_image_full.getGraphics();
			
			u.setColor(new Color(0,0,0,255));
			if (k>=1 && k<=4) {
				g.drawImage(small_image, (k-1)*width/4, 0, null);
				u.drawImage(smim, (k-1)*width/4, 0, null);
			}
			else if (k>=5 && k<=8) {
				g.drawImage(small_image, (k-5)*width/4, height/4, null);
				u.drawImage(smim, (k-5)*width/4, height/4, null);
			}
			else if(k>=9 && k<=12) {
				g.drawImage(small_image, (k-9)*width/4, (2*height/4)-1, null);
				u.drawImage(smim, (k-9)*width/4, (2*height/4)-1, null); 
			}
			else {
				g.drawImage(small_image, (k-13)*width/4, (3*height/4)-2, null);
				g.drawImage(smim, (k-13)*width/4, (3*height/4)-2, null);
			}
		}
		
		JLabel upper_label = new JLabel();
		upper_label.setIcon(new ImageIcon(higher_image_full));
		
		Main.label.setIcon(new ImageIcon(full_image));
		Main.label.add(upper_label);
		
		Main.enregistrer.setEnabled(true);
		filter_listener = new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	//fusion des images
	        	for(int i = 0; i<width; i++) {
	        		for(int j = 0; j<height; j++) {
	        			if(higher_image_full.getRGB(i, j)==Color.red.getRGB()) {
	        				full_image.setRGB(i, j, Color.red.getRGB());
	        			}
	        		}
	        	}
	        	//
	        	JFileChooser jfc_dir = new JFileChooser();
	        	jfc_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        	jfc_dir.setDialogTitle("Choisissez le dossier où enregistrer l'image fusionnée");
	        	jfc_dir.showOpenDialog(Main.mainframe);
	        	try {
					ImageIO.write(full_image, "png", new File(jfc_dir.getSelectedFile()+""+System.getProperty("file.separator")+"merged_picture.png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	jfc_dir.setVisible(false);
	        	javax.swing.JOptionPane.showMessageDialog(null, "Image enregistrée");
	        	Main.enregistrer.setEnabled(false);
	        	Main.enregistrer.removeActionListener(filter_listener);
	        	
	        }
	    };
	    Main.enregistrer.addActionListener(filter_listener);
	}
	
	/**
	 * @param bim : the BufferedImage where to draw the red square
	 * @param top_left : the top left corner's coordinate of the square
	 * @param bottom_left : the bottom left corner's coordinate of the square
	 * @param top_right : the top right corner's coordinate of the square
	 * @param bottom_right : the bottom right corner's coordinate of the square
	 * @precondition bim must exists
	 * @postcondition Draws a square on bim according to four coordinates : top_left, bottom_left, top_right and bottom_right (the corners of the square)
	 */
	private void drawSquare(BufferedImage bim, Coordinate top_left, Coordinate bottom_left, Coordinate top_right, Coordinate bottom_right) {
		assert(bim != null) : "The BufferedImage must exist!";
		
		for(int x = top_left.getX();x<=top_right.getX();x++) {
			bim.setRGB(x, top_left.getY(), Color.red.getRGB()); //up side
			bim.setRGB(x, bottom_left.getY(), Color.red.getRGB()); // down side
		}
		
		for(int y = top_left.getY();y<=bottom_left.getY();y++) {
			bim.setRGB(top_left.getX(), y, Color.red.getRGB()); //left side
			bim.setRGB(top_right.getX(), y, Color.red.getRGB()); // right side
		}
		
		if(Main.upper_born != 16) {
			if(!painter_activated || (painter_activated && Main.method == 1) || (painter_activated && Main.method == 3)) {
				Main.nb_square++;
			}
			
			else if (painter_activated && Main.method == 2){
				Main.nb_square+=14058;
			}
		}
		else {
			if(!painter_activated || (painter_activated && Main.method == 1) || (painter_activated && Main.method == 3)) {

				if(Main.last_step_done && Main.i!=16) {
					Main.square_array[Main.i-2]++;
				}
				else{
					Main.square_array[Main.i-1]++;
				}
			
			}
			
			else if (painter_activated && Main.method == 2){
				if(Main.last_step_done && Main.i!=16) {
					Main.square_array[Main.i-2]+=14058;
				}
				else {
					Main.square_array[Main.i-1]+=14058;
				}
			}
		}
		
	}
	
	/**
	 * @precondition /
	 * @postcondition Allows to draw and erase square on the original image with squares.
	 */
	public void lastProcess() {
		
		painter_activated = false;
		if(higher_image_full == null) {logger.fatal("up image must exists!");}
		if(full_image == null) {logger.fatal("basis image must exists");};
		painter(higher_image_full,full_image);
		
		Main.enregistrer.setEnabled(true);
		
		filter_listener = new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	//fusion sur l'image finale
	        	for(int i = 0; i<width; i++) {
	        		for(int j = 0; j<height; j++) {
	        			if(higher_image_full.getRGB(i, j)==Color.red.getRGB()) {
	        				full_image.setRGB(i, j, Color.red.getRGB());
	        			}
	        		}
	        	}
	        	//
	        	JFileChooser jfc_dir = new JFileChooser();
	        	jfc_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        	jfc_dir.setDialogTitle("Choisissez le dossier où enregistrer l'image finale");
	        	jfc_dir.showOpenDialog(Main.mainframe);
	        	try {
					ImageIO.write(full_image, "png", new File(jfc_dir.getSelectedFile()+""+System.getProperty("file.separator")+"final_picture.png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	jfc_dir.setVisible(false);
	        	javax.swing.JOptionPane.showMessageDialog(null, "Image enregistrée");
	        	Main.enregistrer.setEnabled(false);
	        	Main.enregistrer.removeActionListener(filter_listener);
	        	
	        }
	    };
	    Main.enregistrer.addActionListener(filter_listener);
	    
	    
	}
	
	/**
	 * @return bimage : the analyzed BufferedImage
	 */
	public BufferedImage getBufferedImage(){
		
		return bimage;
		
	}
	
	/**
	 * @precondition painter_activated = true
	 * @postcondition suppress all the painting tools on the main frame
	 */
	public void end_painter() {
		Main.help_button.removeActionListener(Main.help_listener06);
		Main.label.setIcon(new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)));
		Main.label.removeMouseListener(mAdapter);
		Main.label.removeAll();
		end_fr.dispose();
		zoomOptionsF.dispose();
		squareOptionsF.dispose();
		squareCountF.dispose();
		eraseAllF.dispose();
		groupSquaresF.dispose();

	}
	
}
