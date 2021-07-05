import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author Laura Bastin
 * JUnit tests for ImageAnalyzer class
 *
 */

public class TestImageAnalyser {
	
	private static File buffer_file;
	private static ImageAnalyser iman;
	
	@BeforeClass
	public static void setUpBeforeClass(){
		
		buffer_file = null;
		iman = null;
	}
	
	@AfterClass
	public static void EraseAfterClass() {
		
		buffer_file = null;
		iman = null;
		
	}
	
	@Before
	public void setUp() throws IOException {
		
		buffer_file = new File("C://Users//hp//Desktop//programmation//projet_individuel_laura//BastinLauraProjetIndividuel-master//master//Project//src//image_test_2pixels.png");
		iman = new ImageAnalyser(buffer_file);
		
	}
	
	@After
	public void EraseBufferAfter() {
		
		buffer_file = null;
		iman = null;
		
	}
	
	/**
	 * 
	 * @throws IOException if buffer_file == null
	 * Test method for {@link ImageAnalyser#isLessPurpleORDarker(int, int)}
	 */
	@Test
	public void testDarker() throws IOException {
		assertTrue((iman.isLessPurpleORDarker(0, 0)));
	}
	
	/**
	 * 
	 * @throws IOException if buffer_file == null
	 * Test method for {@link ImageAnalyser#isLessPurpleORDarker(int, int)}
	 */
	@Test
	public void testLessPurple() throws IOException{
		assertTrue(iman.isLessPurpleORDarker(1, 0));
	}
	
}
