
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFileChooser;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 * 
 * @author Laura Bastin
 * Main contains main frame and the main operations that will be executed
 *
 */
public class Main extends JFrame{
	
	static BufferedImage img_reader;
	static int nb_square;
	static JFrame mainframe;
	static JButton firstStepButton;
	static JButton secondStepButton;
	static JButton thirdStepButton;
	static JButton lastStepButton;
	static boolean first_step_done;
	static boolean second_step_done;
	static boolean third_step_done;
	static boolean last_step_done;
	static JFileChooser jfc;
	static File fileChoosed;
	static JLabel label;
	static JPanel panel;
	static JFrame compare_frame;
	static String drawing_mode;
	static int i, k;
	static int upper_born = 1;
	static JMenuItem enregistrer;
	static JMenuItem runetape2;
	static JMenuItem runetape3;
	static JMenuItem options_retouche;
	static JMenuItem help_button;
	static JMenuItem contour1;
	static JMenuItem contour2;
	static JMenuItem color_filtered;
	static ImageModifier image_modif2, image_modif4;
	static Logger logger = Logger.getLogger(Main.class);
	static boolean is_treated;
	static JMenu options;
	static JMenuBar menu;
	static JMenuItem image1;
	static JMenuItem image2;
	static JMenuItem image3;
	static JMenuItem image4;
	static JMenuItem image5;
	static JMenuItem image6;
	static JMenuItem image7;
	static JMenuItem image8;
	static JMenuItem image9;
	static JMenuItem image10;
	static JMenuItem image11;
	static JMenuItem image12;
	static JMenuItem image13;
	static JMenuItem image14;
	static JMenuItem image15;
	static JMenuItem image16;
	static JMenuItem images[] = {image1,image2, image3, image4, image5, image6, image7, image8, image9, image10, image11, image12, image13, image14, image15, image16};
	static int[] square_array = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static boolean[] already_activated;
	static int method;
	static ActionListener help_listener00;
	static ActionListener help_listener01;
	static ActionListener help_listener02;
	static ActionListener help_listener03;
	static ActionListener help_listener04;
	static ActionListener help_listener05;
	static ActionListener help_listener06;
	static int r, g, b;
	static int phase;
	static Color low_color = null;
	static Color high_color = null;
	static File e_d_file, surr_file, colors_file, size_file;
	static String e_d;
	//if surrounding mode must be permanent during the runtime
	static boolean perm_surround_runtime1; 
	static boolean perm_surround_runtime2;
	static boolean perm_surround_runtime3;
	//
	static JMenuItem back;
	static String step;
	static int sq_size3; //squares size for the third method (cfr DrawRedSquaresOnImage() in ImageModifier)
	static String directory;
	/**
	 * @precondition /
	 * @postcondition display the main frame and allows the interaction with the elements in the frame
	 * @param args
	 * @throws IOException if the BufferedImage is null
	 */
	public static void main(String[] args) throws IOException {
		
		BasicConfigurator.configure();
		logger.info("Entering in app");
		String os = System.getProperty("os.name").toLowerCase();
		File dir_file = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"dir.txt");
		if(!dir_file.exists()) {
			dir_file.createNewFile();
			PrintWriter wr_empty = new PrintWriter(dir_file);
			wr_empty.println("");
			wr_empty.close();
		}
		else if(dir_file != null) {
			dir_file.getParentFile().mkdirs();
		}
		String br = new BufferedReader(new FileReader(dir_file)).readLine();
		if(os.indexOf("win") >= 0) {
			if(br.isEmpty()) {
				directory = System.getProperty("user.home")+"//Pictures//"; //Default path for images on Windows
			}
			else {
				directory = br;
			}
		}
		else if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0 || os.indexOf("mac") >= 0){
			if(br.isEmpty()) {
				directory = System.getProperty("user.home")+System.getProperty("file.separator")+"Images"+System.getProperty("file.separator"); //Default path for images on Linux and Mac
			}
			else {
				directory = br;
			}
		}
		nb_square = 0;
		sq_size3 = 15; //default size
		already_activated = new boolean[16];
		
		mainframe = new JFrame();
		mainframe.setSize(1000, 650);
		mainframe.setLocation(0,50);
		mainframe.setTitle("RENOCE");

		
		panel = new JPanel();
		panel.setSize(1000,650);
		panel.setVisible(true);
		panel.setBackground(Color.lightGray);
		
		label = new JLabel();
		label.setSize(1000,550);
		label.setLocation(0, 100);
		label.setLayout(new GridBagLayout()); //allows to give another layer to the label
		
		panel.add(label);
		
		menu = new JMenuBar();
		
		JMenu fichier = new JMenu("Fichier");
		JMenu etape1 = new JMenu("Etape 1");
		JMenu etape2 = new JMenu("Etape 2");
		JMenu etape3 = new JMenu("Etape 3");
		JMenu etape4 = new JMenu("Etape 4");
		JMenu help = new JMenu("Aide");
		options = new JMenu("options");
		JMenu infos = new JMenu("A propos");
		
		JMenuItem ouvrir = new JMenuItem("Ouvrir");
		enregistrer = new JMenuItem("Enregistrer sous");
		enregistrer.setEnabled(false);
		JMenuItem runetape1 = new JMenuItem("Exécuter la reconnaissance de cellules");
		runetape2 = new JMenuItem("Exécuter l'entourage des cellules");
		runetape3 = new JMenuItem("Exécuter la fusion de l'image de base et l'image des carrés");
		JMenuItem runetape4 = new JMenuItem("Exécuter les dernières retouches");
		options_retouche = new JMenuItem("options de retouches");
		help_button = new JMenuItem("Aide");
		contour1 = new JMenuItem("entourage petites images");
		contour2 = new JMenuItem("entourage images larges");
		color_filtered = new JMenuItem("filtrage des couleurs");
		back = new JMenuItem("retour en arrière");
		JMenuItem size = new JMenuItem("taille des carrés pour la méthode de comptage précis");
		JMenuItem infos_click = new JMenuItem("A propos de Renoce");
		
		back.setEnabled(false);
		runetape1.setEnabled(false);
		runetape2.setEnabled(false);
		runetape3.setEnabled(false);
		runetape4.setEnabled(false);
		options_retouche.setEnabled(true);
		size.setEnabled(true);
		infos_click.setEnabled(true);
		
		color_filtered.setEnabled(true);
		help_button.setEnabled(true);
		
		fichier.add(ouvrir);
		fichier.add(enregistrer);
		
		etape1.add(runetape1);
		etape2.add(runetape2);
		etape3.add(runetape3);
		etape4.add(runetape4);
		options.add(options_retouche);
		options.add(color_filtered);
		options.add(contour1);
		options.add(contour2);
		options.add(back);
		options.add(size);
		help.add(help_button);
		infos.add(infos_click);
		
		menu.add(fichier);
		menu.add(etape1);
		menu.add(etape2);
		menu.add(etape3);
		menu.add(etape4);
		menu.add(options);
		menu.add(help);
		menu.add(infos);
		
		mainframe.add(panel);
		mainframe.setJMenuBar(menu);
		mainframe.setVisible(true);
		
		for(int index = 0; index<16; index++) {
			images[index] = new JMenuItem();
		}
		
		try {
			if(os.indexOf("win") >= 0) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //set Windows look and feel
			}
			else if(os.contains("mac")) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //set Mac look and feel
			}
			else {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //set Linux look and feel
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException| UnsupportedLookAndFeelException e1) {
				logger.fatal("Error with look and feel in Main.main(String[])");
		}
		try {
			SwingUtilities.updateComponentTreeUI(mainframe);
		}catch(NullPointerException npe) {}
		
		
		
		jfc = new JFileChooser();
		jfc.setDialogTitle("Choississez l'image à traiter");
		jfc.setFileFilter(new FileNameExtensionFilter("Image file","jpg","png","gif"));
		
		Progress dir_progress = new Progress("Début de la vérification de l'existence des dossiers");
		dir_progress.setPercentage(0);
		for(int k=1; k<=16; k++) {
			dir_progress.setTitle("Vérification de l'existence de small_image"+k+"_file");
			dir_progress.setPercentage(k*31);
			if(k==16) {dir_progress.dispose();}
			File small_im_dir = new File(directory+"small_image"+k+"_file");
			if(!small_im_dir.exists()) {
				dir_progress.setTitle("Création de small_image"+k+"_file");
				small_im_dir.mkdir();
			}
		}
		
		infos_click.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				javax.swing.JOptionPane.showMessageDialog(null, "Renoce est un programme développé dans un cadre universitaire pour l'Université de Namur par Laura BASTIN.\nVous utilisez la version 4.6 du programme (du 03-02-2018).");
			}
		});
		
		surr_file = new File(directory+"entourage.txt");
		if(!surr_file.exists()) {
			surr_file.createNewFile();
			PrintWriter wr_empty = new PrintWriter(surr_file);
			wr_empty.println("");
			wr_empty.close();
		}
		else if(surr_file != null) {
			surr_file.getParentFile().mkdirs();
		}
		contour1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int keep = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous conserver le mode d'entourage petites images pour toutes les exécutions du programme à venir?", "Garder le mode?", JOptionPane.YES_NO_OPTION);
				if(keep == JOptionPane.YES_OPTION) {
					try {
						perm_surround_runtime1 = false;
						perm_surround_runtime2 = false;
						perm_surround_runtime3 = false;
						PrintWriter pw = new PrintWriter(surr_file);
						pw.println("1");
						pw.close();
					} catch (FileNotFoundException e1) {logger.fatal("Unable to write in entourage.txt in Main(String[])");}
					javax.swing.JOptionPane.showMessageDialog(null,"Modifications enregistrées");
				}
				else {
					perm_surround_runtime1 = true;
					perm_surround_runtime2 = false;
					perm_surround_runtime3 = false;
				}
			}
		});
		
		contour2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int precision = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous un entourage moins précis mais un comptage plus précis?", "Entourage précis?", JOptionPane.YES_NO_OPTION);
				int keep = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous conserver ce mode d'entourage pour toutes les exécutions du programme à venir?", "Garder le mode?", JOptionPane.YES_NO_OPTION);
				if(keep == JOptionPane.YES_OPTION) {
					try {
						perm_surround_runtime1 = false;
						perm_surround_runtime2 = false;
						perm_surround_runtime3 = false;
						PrintWriter pw = new PrintWriter(surr_file);
						if(precision == JOptionPane.NO_OPTION) {
							pw.println("2");
						}
						else {
							pw.println("3");
						}
						pw.close();
					} catch (FileNotFoundException e1) {logger.fatal("Unable to write in entourage.txt in Main(String[])");}
					javax.swing.JOptionPane.showMessageDialog(null,"Modifications enregistrées");
				}
				else {
					if(precision == JOptionPane.NO_OPTION) {
						perm_surround_runtime1 = false;
						perm_surround_runtime2 = true;
						perm_surround_runtime3 = false;
					}
					else {
						perm_surround_runtime1 = false;
						perm_surround_runtime2 = false;
						perm_surround_runtime3 = true;
					}
				}
			}
		});
		e_d = null;
		e_d_file = new File(directory+"erosions_dilations.txt");
		if(!e_d_file.exists()) {
			e_d_file.createNewFile();
			PrintWriter wr_empty = new PrintWriter(e_d_file);
			wr_empty.println("");
			wr_empty.close();
		}
		else if(e_d_file != null){
			e_d_file.getParentFile().mkdirs();
		}
		
		JFrame textFrame = new JFrame();
		JTextField textFieldErDil = new JTextField();
		JButton enreg = new JButton("enregistrer");
		
		Main.options_retouche.setEnabled(true);
		
		ActionListener options_listener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Main.help_listener02 = new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						javax.swing.JOptionPane.showMessageDialog(null, "Tapez e pour faire une érosion et d pour faire une dilation. Vous ne pouvez taper qu'une suite de 10 caractères (e ou d).\nUne érosion renforce les points tandis qu'une dilation a tendance à les faire disparaître.\nPar défaut, si vous n'inscrivez rien, une érosion et deux dilations seront faites.");
					}
				};
				
				Main.help_button.addActionListener(Main.help_listener02);
				textFrame.setSize(500,100);
				textFrame.setLocationRelativeTo(null);
				textFrame.setTitle("Entrée des érosions et dilations à faire");
				textFrame.setVisible(true);
				
				JPanel textPanel = new JPanel();
				
				enreg.setSize(100,50);
				enreg.setLocation(400,50);
				
				textFieldErDil.setColumns(10);
				textFieldErDil.setToolTipText("edd (par défaut si rien n'est inscrit)");
				
				textPanel.add(textFieldErDil);
				textPanel.add(enreg);
				
				textFieldErDil.addKeyListener(new KeyAdapter() {
				    public void keyTyped(KeyEvent e) { 
				        if (textFieldErDil.getText().length() >= 10 || (e.getKeyChar()!='e' && e.getKeyChar()!='d')) { //limit to 10 characters and those characters must be 'd' or 'e'
				            e.consume(); 
				        }
				    }  
				});
				
				textFrame.add(textPanel);
			}
		};
		options_retouche.addActionListener(options_listener);
			
		enreg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				
				int confirm = javax.swing.JOptionPane.showConfirmDialog(null,"Enregistrer les modifications pour les prochaines utilisations du programme?","Enregistrement?",JOptionPane.YES_NO_OPTION);
				if(confirm == JOptionPane.YES_OPTION) {
					e_d = textFieldErDil.getText();
					if(e_d.length()==0) {e_d = "edd";}
					try {
						PrintWriter pw = new PrintWriter(e_d_file);
						pw.println(e_d);
						pw.close();
					} catch (FileNotFoundException e) {logger.fatal("Unable to write in erosions_dilations.txt in CutAndModifyImage()");}
					e_d = null;
					javax.swing.JOptionPane.showMessageDialog(null,"Modifications enregistrées");
				}
				else {
					e_d = textFieldErDil.getText();
				}
				textFrame.dispose();
				options_retouche.removeActionListener(options_listener);
				
			}
		});
		
		colors_file = new File(directory+"teintes.txt");
		if(!colors_file.exists()) {
			colors_file.createNewFile();
			PrintWriter wr_empty = new PrintWriter(colors_file);
			wr_empty.println("");
			wr_empty.close();
		}
		else if(colors_file != null){
			colors_file.getParentFile().mkdirs();
		}
		
		color_filtered.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				phase = 1;
				JFrame colorF = new JFrame();
				colorF.setTitle("En dessous de quelle teinte voulez-vous filtrer?");
				colorF.setSize(1260,650);
				
				JLabel colorL = new JLabel();
				colorL.setSize(1260,650);
				
				JButton default_config = new JButton("Teintes par défaut");
				default_config.setSize(1260, 100);
				default_config.setLocation(0, 550);
				
				int locX = 0;
				int locY = 0;
				for(int r = 0; r<=255; r+=29) {
					for(int g = 0; g<=255; g+=29) {
						for(int b = 0; b<=255; b+=29) {
							JButton button_col = new JButton();
							button_col.setSize(30,30);
							BufferedImage buffim = new BufferedImage(29,29,BufferedImage.TYPE_INT_RGB);
							for(int x = 0; x<29; x++) {
								for(int y =0; y<29; y++) {
									buffim.setRGB(x, y, new Color(r,g,b).getRGB());
								}
							}
							button_col.setIcon(new ImageIcon(buffim));
							button_col.setLocation(locX,locY);
							button_col.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if(phase == 2) {
										high_color = new Color(buffim.getRGB(0, 0));
										colorF.dispose();
										int conserv = javax.swing.JOptionPane.showConfirmDialog(null, "Voulez-vous conserver ces teintes pour les éxecutions du programme à venir?", "Conserver les teintes?", JOptionPane.YES_NO_OPTION);
										if(conserv == JOptionPane.YES_OPTION) {
											PrintWriter pw = null;
											try {
												pw = new PrintWriter(colors_file);
											} catch (FileNotFoundException e1) {logger.fatal("teintes.txt not found in Main(String[])");}
											String red_low = "";
										    String green_low = "";
										    String blue_low = "";
										    String red_high = "";
										    String green_high = "";
										    String blue_high = "";
										    if(String.valueOf(low_color.getRed()).length() == 2){red_low = "0".concat(String.valueOf(low_color.getRed()));}
										    else if(String.valueOf(low_color.getRed()).length() == 1){red_low = "00".concat(String.valueOf(low_color.getRed()));}
										    else {red_low = String.valueOf(low_color.getRed());}
										    if(String.valueOf(low_color.getGreen()).length() == 2){green_low = "0".concat(String.valueOf(low_color.getGreen()));}
										    else if(String.valueOf(low_color.getGreen()).length() == 1){green_low = "00".concat(String.valueOf(low_color.getGreen()));}
										    else {green_low = String.valueOf(low_color.getGreen());}
										    if(String.valueOf(low_color.getBlue()).length() == 2){blue_low = "0".concat(String.valueOf(low_color.getBlue()));}
										    else if(String.valueOf(low_color.getBlue()).length() == 1){blue_low = "00".concat(String.valueOf(low_color.getBlue()));}
										    else {blue_low = String.valueOf(low_color.getBlue());}
										    if(String.valueOf(high_color.getRed()).length() == 2){red_high = "0".concat(String.valueOf(high_color.getRed()));}
										    else if(String.valueOf(high_color.getRed()).length() == 1){red_high = "00".concat(String.valueOf(high_color.getRed()));}
										    else {red_high = String.valueOf(high_color.getRed());}
										    if(String.valueOf(high_color.getGreen()).length() == 2){green_high = "0".concat(String.valueOf(high_color.getGreen()));}
										    else if(String.valueOf(high_color.getGreen()).length() == 1){green_high = "00".concat(String.valueOf(high_color.getGreen()));}
										    else {green_high = String.valueOf(high_color.getGreen());}
										    if(String.valueOf(high_color.getBlue()).length() == 2){blue_high = "0".concat(String.valueOf(high_color.getBlue()));}
										    else if(String.valueOf(high_color.getBlue()).length() == 1){blue_high = "00".concat(String.valueOf(high_color.getBlue()));}
										    else {blue_high = String.valueOf(high_color.getBlue());}
										    pw.println(red_low+","+green_low+","+blue_low+";"+red_high+","+green_high+","+blue_high);
											pw.close();
											low_color = null;
											high_color = null;
										}
									}
									else if(phase == 1) {
										low_color = new Color(buffim.getRGB(0, 0));
										button_col.setEnabled(false);
										phase = 2;
										colorF.setTitle("Au dessus de quelle teinte voulez-vous filtrer?");
									}
								}
							});
							colorL.add(button_col);
							if(locX < 1200) {
								locX += 30;
							}
							else {
								locX = 0;
								locY += 30;
							}
						}
					}
				}
				default_config.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						low_color = null;
						high_color = null;
						PrintWriter wr_empty = null;
						try {
							wr_empty = new PrintWriter(colors_file);
						} catch (FileNotFoundException e1) {logger.fatal("teintes.txt not found in Main(String[])");}
						wr_empty.println("");
						wr_empty.close();	
					}
				});
				colorL.add(default_config);
				colorF.getContentPane().add(new JScrollPane(colorL));
				colorF.setVisible(true);
			}
		});
		
		back.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				step = javax.swing.JOptionPane.showInputDialog(null,"A quelle étape voulez-vous revenir?");
				if(second_step_done && !third_step_done && !last_step_done) {
					if(!step.equals("1")) {
						while(!step.equals("1")) {
							step = javax.swing.JOptionPane.showInputDialog(null,"A quelle étape voulez-vous revenir? (l'étape ne peut être que 1)");
						}
					}
					runetape1.setEnabled(true);
					if(upper_born != 16) {
						nb_square = 0;
					}
					else {
						square_array[i-1] = 0;
					}
					first_step_done = false;
					second_step_done = false;
					try {
						image_modif2.end_painter();
					}
					catch(NullPointerException npe) {logger.debug("No existing painting frame");}
					javax.swing.JOptionPane.showMessageDialog(null,"Vous pouvez désormais re cliquer sur Etape1");
				}
				else if(second_step_done && third_step_done && !last_step_done) {
					if(!step.equals("1") && !step.equals("2")) {
						while(!step.equals("1") && !step.equals("2")) {
							step = javax.swing.JOptionPane.showInputDialog(null,"A quelle étape voulez-vous revenir? (l'étape peut être 1 ou 2)");
						}
					}
					else if(step.equals("1")) {
						runetape1.setEnabled(true);
						if(upper_born != 16) {
							nb_square = 0;
						}
						else {
							square_array[i-1] = 0;
						}
						first_step_done = false;
						second_step_done = false;
						third_step_done = false;
						label.removeAll();
						javax.swing.JOptionPane.showMessageDialog(null,"Vous pouvez désormais re cliquer sur Etape1");
					}
					else if(step.equals("2")) {
						if(upper_born != 16) {
							nb_square = 0;
						}
						else {
							square_array[i-1] = 0;
						}
						runetape2.setEnabled(true);
						second_step_done = false;
						third_step_done = false;
						label.removeAll();
						javax.swing.JOptionPane.showMessageDialog(null,"Vous pouvez désormais re cliquer sur Etape2");
					}
				}
				else if(second_step_done && third_step_done && last_step_done) {
					if(!step.equals("1") && !step.equals("2") && !step.equals("3")) {
						while(!step.equals("1")&& !step.equals("2") && !step.equals("3")) {
							step = javax.swing.JOptionPane.showInputDialog(null,"A quelle étape voulez-vous revenir? (l'étape peut être 1, 2 ou 3)");
						}
					}
					else if(step.equals("1")) {
						runetape1.setEnabled(true);
						if(upper_born != 16) {
							nb_square = 0;
						}
						else {
							square_array[i-1] = 0;
						}
						first_step_done = false;
						second_step_done = false;
						third_step_done = false;
						last_step_done = false;
						image_modif4.end_painter();
						i--;
						javax.swing.JOptionPane.showMessageDialog(null,"Vous pouvez désormais re cliquer sur Etape1");
					}
					else if(step.equals("2")) {
						if(upper_born != 16) {
							nb_square = 0;
						}
						else {
							square_array[i-1] = 0;
						}
						runetape2.setEnabled(true);
						second_step_done = false;
						third_step_done = false;
						last_step_done = false;
						image_modif4.end_painter();
						i--;
						javax.swing.JOptionPane.showMessageDialog(null,"Vous pouvez désormais re cliquer sur Etape2");
					}
					else if(step.equals("3")) {
						Main.label.removeAll();
						runetape3.setEnabled(true);
						third_step_done = false;
						last_step_done = false;
						image_modif4.end_painter();
						i--;
						javax.swing.JOptionPane.showMessageDialog(null, "Vous pouvez désormais re cliquer sur Etape3");
					}
				}
			}
		});
		
		size_file = new File(directory+"taille_carres.txt");
		if(!size_file.exists()) {
			size_file.createNewFile();
			PrintWriter wr_empty = new PrintWriter(size_file);
			wr_empty.println("");
			wr_empty.close();
		}
		else if(size_file != null){
			size_file.getParentFile().mkdirs();
		}
		size.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = javax.swing.JOptionPane.showInputDialog(null,"Veuillez choisir une taille des carrés pour l'entourage peu précis mais comptage précis: ");
				if(!s.contains("0")&&!s.contains("1")&&!s.contains("2")&&!s.contains("3")&&!s.contains("4")&&!s.contains("5")&&!s.contains("6")&&!s.contains("7")&&!s.contains("8")&&!s.contains("9")) {
					while(!s.contains("0")&&!s.contains("1")&&!s.contains("2")&&!s.contains("3")&&!s.contains("4")&&!s.contains("5")&&!s.contains("6")&&!s.contains("7")&&!s.contains("8")&&!s.contains("9")) {
						s =  javax.swing.JOptionPane.showInputDialog(null,"Veuillez choisir une taille des carrés pour l'entourage peu précis mais comptage précis (cela doit être un nombre): ");
					} 
				}
				int keep = javax.swing.JOptionPane.showConfirmDialog(null,"Voulez-vous conserver cette taille de carrés pour la méthode de comptage précis pour les exécutions du programme à venir?","Conserver taille?",JOptionPane.YES_NO_OPTION);
				if(keep == JOptionPane.YES_OPTION) {
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(size_file);
					} catch (FileNotFoundException e1) {logger.fatal("taille_carres.txt not found in Main(String[])");}
					pw.println(s);
					pw.close();
				}
				else {
					sq_size3 = Integer.valueOf(s);
					PrintWriter empty = null;
					try {
						empty = new PrintWriter(size_file);
					} catch (FileNotFoundException e1) {logger.fatal("taille_carres.txt not found in Main(String[])");}
					empty.println("");
					empty.close();
				}
			}
		});
		//step 0
		
		help_listener00 = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				javax.swing.JOptionPane.showMessageDialog(null, "Vous êtes dans la toute première étape. \nVeuillez cliquer sur fichier puis ouvrir pour choisir une image à traiter.");
			}
		};
		help_button.addActionListener(help_listener00);
		
		
	    ouvrir.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
				help_button.removeActionListener(help_listener00);
	        	logger.info(first_step_done);
				logger.info(second_step_done);
				logger.info(third_step_done);
				
				jfc.showOpenDialog(mainframe);
				mainframe.add(jfc);
				
				Progress processus = null;
				try {
					processus = new Progress("Chargement de l'image...");
					processus.setPercentage(0);
					img_reader = ImageIO.read(jfc.getSelectedFile());
				} catch (IOException e) {logger.fatal("Non existing image in Main.main(String[]). We should never have landed here.");}
				
				if (img_reader.getWidth()>6000 && img_reader.getHeight()>6000) {
					label.setIcon(new ImageIcon(img_reader));
					processus.setPercentage(250);
					mainframe.getContentPane().add(new JScrollPane(panel));
					processus.setPercentage(330);
					mainframe.repaint();
					processus.setPercentage(499);
					jfc.setVisible(false);
					javax.swing.JOptionPane.showMessageDialog(null, "Image de plus 6000x6000 détectée. La procédure va être montrée sous-image\n par sous-image.");
					processus.dispose();
					processus = new Progress("Procédure lancée, cela risque de prendre du temps...");
					processus.setPercentage(250);
					ImageModifier image_modif0 = new ImageModifier(jfc.getSelectedFile());

					try {
						processus.setPercentage(499);
						processus.dispose();
						image_modif0.cutImage(jfc.getSelectedFile(), "basis_image");
						processus = new Progress("Finalisation...");
						processus.setPercentage(0);
					} catch (IOException e) {logger.fatal("Non existing main file in Main.main(String[])");}
					upper_born = 16;
					i=1;
	        		try {
	        			processus.setPercentage(499);
	        			processus.dispose();
						label.setIcon(new ImageIcon(ImageIO.read(new File(directory+"small_image1_file"+System.getProperty("file.separator")+"basis_image1.png"))));
					} catch (IOException e) {logger.fatal("Unable to read basis_image1.png in Main.main(String[])");}
				}
				
				else {
					processus.setPercentage(499);
					processus.dispose();
					label.setIcon(new ImageIcon(img_reader));
					mainframe.getContentPane().add(new JScrollPane(panel));
					mainframe.repaint();
					jfc.setVisible(false);
				}
				
				runetape1.setEnabled(true);
				
				help_listener01 = new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						javax.swing.JOptionPane.showMessageDialog(null, "Veuillez cliquer sur Etape1 puis Exécuter la reconnaissance de cellules pour passer à la suite. \nCela provoquera le traitement de l'image de manière à ne faire apparaître que les noyaux de cellules.");
					}
				};
				
				help_button.addActionListener(help_listener01);
					
	        }
	    });
	    
	    
	    //step 1
	    
	    runetape1.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	
	        	back.setEnabled(false);
	        	color_filtered.setEnabled(true);
	        	help_button.removeActionListener(help_listener01);
	        	ouvrir.setEnabled(false);
	        	if(upper_born == 16) {
	        		try {
						label.setIcon(new ImageIcon(ImageIO.read(new File(directory+"small_image"+i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png"))));
					} catch (IOException e) {logger.fatal("Unable to read basis_image.png in Main.main(String[])");}
	        		javax.swing.JOptionPane.showMessageDialog(null, "Numéro de la sous-image actuelle : " + i );
	        	}
	        	img_reader.flush();
	        	System.gc();
	        	
	        	ImageModifier image_modif = null; 
				try {
					if(upper_born == 16) {
						fileChoosed = new File(directory+"small_image"+Main.i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png");
					}
					else {
						fileChoosed = jfc.getSelectedFile();
					}
					image_modif = new ImageModifier(fileChoosed);
					image_modif.cutAndModifyImage();
				} catch (IOException e) {logger.fatal("IOException in Main.main(String[]). We should never have landed here!");}
				
				first_step_done = true;
				second_step_done = false;
				third_step_done = false;
				last_step_done = false;
				
				runetape1.setEnabled(false);
				runetape3.setEnabled(false);
				runetape4.setEnabled(false);
	        }
	    });
	
			
		//second step
		runetape2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
					
					back.setEnabled(true);
					image_modif2 = null;
					if(upper_born == 16) {
						image_modif2 = new ImageModifier(new File(directory+"small_image"+i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png"));
					}
					else {
						image_modif2 = new ImageModifier(fileChoosed);
					}
					try {
						image_modif2.DrawRedSquaresOnImage();
					} catch (NonExistingLabelException e) {
						logger.fatal("NonExistingLabelException in Main that should never occur");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.fatal("IOException in Main that should never occur");
					}
					
					first_step_done = true;
					second_step_done = true;
					third_step_done = false;
					last_step_done = false;
					
					runetape1.setEnabled(false);
					runetape2.setEnabled(false);
					runetape4.setEnabled(false);
					
				
			}
		});
		
		//third step
		
		runetape3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ImageModifier image_modif3 = null;
					if(upper_born == 16) {
						image_modif3 = new ImageModifier(new File(directory+"small_image"+i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png"));
					}
					else {
						image_modif3 = new ImageModifier(jfc.getSelectedFile());
					}
					try {
						image_modif3.mergeImages();
					} catch (IOException e) {logger.fatal("selected file null!");}
					
					first_step_done = true;
					second_step_done = true;
					third_step_done = true;
					last_step_done = false;
					
					runetape1.setEnabled(false);
					runetape2.setEnabled(false);
					runetape3.setEnabled(false);
					runetape4.setEnabled(true);
				}
		});
		
		mainframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		//last step
		
		runetape4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				image_modif4 = null;
				if(upper_born == 16) {
					image_modif4 = new ImageModifier(new File(directory+"small_image"+i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png"));
				}
				else {
					image_modif4 = new ImageModifier(jfc.getSelectedFile());
				}
				image_modif4.lastProcess();
				runetape4.setEnabled(false);
				
				last_step_done = true;
				logger.info(first_step_done);
				logger.info(second_step_done);
				logger.info(third_step_done);
			    if(upper_born == 16) {
			    	runetape1.setEnabled(true);
			    	if(i>=2 && !already_activated[i-2]) {
		    			images[i-2] = new JMenuItem("Revenir à l'image "+String.valueOf(i-1));
		    			int ancient_i = i-1;
		    			options.add(images[i-2]);
		    			menu.add(options);
			    		images[i-2].addActionListener(new ActionListener() {
			    			public void actionPerformed(ActionEvent event) {
			    				i = ancient_i;
						    	already_activated[i-1] = true;
			    				square_array[i-1] = 0;
			    				image_modif4.end_painter(); //remove all the listeners
			    				try {
									label.setIcon(new ImageIcon(ImageIO.read(new File(directory+"small_image"+i+"_file"+System.getProperty("file.separator")+"basis_image"+i+".png"))));
								} catch (IOException e) {logger.fatal("Unable to read basis_image"+i+"in Main.main(String[])");}
			    				javax.swing.JOptionPane.showMessageDialog(null, "Retour à la sous-image "+ i +" effectué.");
								disableAllReturns();
			    			}
			    		});
				    	}
				    	
				    nb_square = 0;
					for(int n = 0; n<16; n++) {
						nb_square += square_array[n];
					}
					if(i!=16) {
						i++;
					}
		    	}
			}
			});
		
		}
	
		/**
		 * @precondition /
		 * @postcondition Disables all the possibilities to return to a previous image
		 */
		private static void disableAllReturns() {
			
			for(int j=0;j<16;j++) {
				images[j].setEnabled(false);
			}
			
		}
	
	
	
	
	
}
