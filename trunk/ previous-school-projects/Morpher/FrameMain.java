/* FrameMain. java
   This frame contains the main window used to control the program.
*/

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.PixelGrabber;

public class FrameMain extends JFrame {
	private JButton open1 = new JButton("Open source");
	private JButton open2 = new JButton("Open destination");
	private ActionListener buttonListener = new ButtonListener();
	private PictureFrame pFrame1 = new PictureFrame("picture 1");
	private PictureFrame pFrame2 = new PictureFrame("picture 2");
	private PictureFrame[] pFrame3 = new PictureFrame[512]; // frames for holding warped destination images
	private PictureFrame[] pFrame4 = new PictureFrame[512]; // frames for holding warped source images
	private BufferedImage[] result = new BufferedImage[512]; // warped destination images
	private BufferedImage[] result2 = new BufferedImage[512]; // warped source images
	private MorphFrame endFrame; // frame for viewing final combined images
	private BufferedImage image1, image2; // source and destination images
	private BufferedImage[] finalResult = new BufferedImage[512]; // final combined images
	private FrameMain theFrame = this; // reference to the parent frame
	private Line[] lines1 = new Line[512]; // array of control lines for the source image
	private Line[] lines2 = new Line[512]; // array of control lines for the destination image
	private File file1, file2; // source and destination image files
	private int numLines = 0;
	private int size = 1024;
	private int frameNum = 0;
	private final Rectangle pictureBounds = new Rectangle(400, 10, 400, 400); // bounds for child windows
	private final JLabel frameLabel = new JLabel("Intermediate frames");
	private final JButton morph = new JButton("Morph!");
	private final JTextField numFrames = new JTextField("3", 5);
	private final JLabel parameters = new JLabel("Morph parameters:       ");
	private final JLabel aLabel = new JLabel("a (constant):");
	private final JTextField aField = new JTextField("0.1", 3);
	private final JLabel bLabel = new JLabel("b (influence of distance from lines):");
	private final JTextField bField = new JTextField("2", 3);
	private final JLabel pLabel = new JLabel("p (influence of line length):");
	private final JTextField pField = new JTextField("0", 3);
	private double testX, testY;
	private boolean done = false;
	
	public FrameMain(String title) {
		super(title);
		MainPanel mainPanel = new MainPanel();
		add(mainPanel);
		mainPanel.init();
	}

	// add newly drawn lines to the source and destination frames
	public void updateLines(Line next) {
		Line l1 = new Line(next); 
		Line l2 = new Line(next);
		lines1[numLines] = l1; // add a separate copy of the new line to the source and destination arrays
		lines2[numLines] = l2;
		pFrame1.draw(lines1, numLines); // draw the lines on each panel
		pFrame2.draw(lines2, numLines);
		numLines++; // update the count of lines
	}
	
	// remove a line from the visible area
	public void delete(int pointNum) {
		lines1[pointNum].setStartX(-100);
		lines1[pointNum].setStartY(-100);
		lines1[pointNum].setEndX(-101);
		lines1[pointNum].setEndY(-101);
		lines2[pointNum].setStartX(-100);
		lines2[pointNum].setStartY(-100);
		lines2[pointNum].setEndX(-101);
		lines2[pointNum].setEndY(-101);
	}
	
	// panel to hold the components for the main window
	public class MainPanel extends JPanel {
		// add components to panel
		public void init() {
			add(open1);
			open1.addActionListener(buttonListener);
			add(open2);
			open2.addActionListener(buttonListener);
			add(frameLabel);
			add(numFrames);
			add(morph);
			morph.addActionListener(buttonListener);
			add(parameters);
			add(aLabel);
			add(aField);
			add(bLabel);
			add(bField);
			add(pLabel);
			add(pField);
			Line firstline1 = new Line(50, 130, 200, 130);
			updateLines(firstline1);
		}
	}
	
	// opens an image file
	public File openImage(BufferedImage img, PictureFrame frame) {
		File inFile = new File("file"); 
		JFileChooser fc = new JFileChooser();
		// open file through file chooser
		int returnVal = fc.showOpenDialog(FrameMain.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			inFile = fc.getSelectedFile();
		}
		try {
			// set image to the opened file and initialize a frame containing it
			img = ImageIO.read(inFile);
			frame.setBounds(pictureBounds);
			frame.setVisible(true);
			frame.init(img, theFrame);
		}
		catch (IOException ex) {
			System.out.println("canceled");
		}
		return inFile;
	}
	
	// action listener for the various buttons
	public class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// open source image
			if(e.getSource() == open1) {
				file1 = openImage(image1, pFrame1);
				try {
					image1 = ImageIO.read(file1);
				}
				catch (IOException ex) {
					System.out.println("canceled");
				}
			}
			
			// open destination image
			if(e.getSource() == open2) {
				file2 = openImage(image2, pFrame2);
				try {
					image2 = ImageIO.read(file2);
				}
				catch (IOException ex) {
					System.out.println("canceled");
				}
			}
			
			// perform morphing on the source and destination images
			if(e.getSource() == morph) {
				// get the specified number of intermediate frames
				frameNum = Integer.parseInt(numFrames.getText());
				// create frames to hold the intermediate images
				for (int i = 0; i < frameNum; i++) {
					try {
						result[i] = ImageIO.read(file2);
						result2[i] = ImageIO.read(file1);
					}
					catch (IOException ex) {
						System.out.println("canceled");
					}
				}
				int[][] color1 = new int[image2.getHeight()][image2.getWidth()];
				int[][] color2 = new int[image1.getHeight()][image1.getWidth()];
				
				// generate reverse-mapped warped images from the destination image
				for (int frame = 0; frame < frameNum; frame++) {
					pFrame3[frame] = new PictureFrame("morphingdest" + frame);
					// set the size of the intermediate to be the larger of the two images
					if ((image1.getWidth() * image1.getHeight()) >= (image2.getWidth() * image2.getHeight()))
						pFrame3[frame].setSize(image1);
					else pFrame3[frame].setSize(image2);
					// warp image
					generateResultImage(lines1, lines2, numLines, frame, color1, image2, result, pFrame3);
					pFrame3[frame].setBounds(pictureBounds);
					pFrame3[frame].init(result[frame], theFrame);
					pFrame3[frame].showPoint(testX, testY);
					//pFrame3[frame].setVisible(true);
				}
				
				// generate reverse-mapped warped images from the source image
				for (int frame = 0; frame < frameNum; frame++) {
					pFrame4[frame] = new PictureFrame("morphingsrc" + frame);
					// set the size of the intermediate to be the larger of the two images
					if ((image1.getWidth() * image1.getHeight()) >= (image2.getWidth() * image2.getHeight()))
						pFrame4[frame].setSize(image1);
					else pFrame4[frame].setSize(image2);
					// warp image
					generateResultImage(lines2, lines1, numLines, frame, color2, image1, result2, pFrame4);
					pFrame4[frame].setBounds(pictureBounds);
					pFrame4[frame].init(result2[frame], theFrame);
					pFrame4[frame].showPoint(testX, testY);
					//pFrame4[frame].setVisible(true);
				}
				
				// cross-dissolve the warped images into the final intermediate frames
				for (int frame = 0; frame < frameNum; frame++) {
					// morphResult[frame] = new PictureFrame("final" + frame);
					// set size of intermediate frames to be the larger of the two images
					if ((image1.getWidth() * image1.getHeight()) >= (image2.getWidth() * image2.getHeight())) {
						try {
							finalResult[frame] = ImageIO.read(file2);
						}
						catch (IOException ex) {
							System.out.println("canceled");
						}
					}
					else {
						try {
							finalResult[frame] = ImageIO.read(file1);
						}
						catch (IOException ex) {
							System.out.println("canceled");
						}
					}
					// cross dissolve all pixels of the warped images into the intermediate frame
					for (int y = 0; y < finalResult[frame].getHeight(); y++) {
						for (int x = 0; x < finalResult[frame].getWidth(); x++) {
							crossDissolve(finalResult, frame, x, y);
						}
					}
				}
				
				// set initial source and destination images to be the first and last frames
				for (int i=frameNum-1; i >= 0; i--) {
					finalResult[i+1] = finalResult[i];
				}
				finalResult[0] = image1;
				finalResult[frameNum+1] = image2;
				// set up window for viewing the final series of frames
				endFrame = new MorphFrame("Morph");
				endFrame.setBounds(pictureBounds);
				endFrame.setVisible(true);
				endFrame.init(finalResult, theFrame, frameNum);
			}
		}
		
		// calculate the source point for a pixel for each control line
		public void generateResultImage(Line[] dest, Line[] src, int numLines, int frame, int[][] color, BufferedImage image, 
							BufferedImage[] result, PictureFrame[] pFrame) {
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					double[] deltax = new double[512];
					double[] deltay = new double[512];
					double[] weight = new double[512];
					for (int line = 0; line < numLines; line++) {
						if (dest[line].getStartX() >= 0) {
							// compute distance from the point to the line, the perpendicular bisect,
							// and the fractional distance along the line of the intersection point
							double ratio = dest[line].calculateDistances(x, y);
							double d1 = dest[line].getDistanceToLine();
							//System.out.println("d1: " + d1);
							double d2 = dest[line].getPerpendicularDistance();
							//double ratio = lines2[line].getRatio();
							//System.out.println("ratio: " + ratio);
							// calculate the source point for the destination point for this control line
							src[line].calculateSourceForLine(d1, ratio);
							double xline = src[line].getXLine(); // x and y coordinates of source point for this line
							double yline = src[line].getYLine();
							if (x == 0 && y == 0) {
								testX = xline;
								testY = yline;
							}
							// find the delta value for the source point with respect to the destination point
							deltax[line] = xline - x;
							deltay[line] = yline - y;
							// apply user-specified morphing values
							double a = Double.parseDouble(aField.getText());
							double b = Double.parseDouble(bField.getText());
							double p = Double.parseDouble(pField.getText());
							int lengthx = dest[line].getLengthX();
							int lengthy = dest[line].getLengthY();
							// calculate weight for the line
							weight[line] = Math.pow(((Math.pow(Math.sqrt(lengthx*lengthx + lengthy*lengthy), p))
													  /(a+d1)), b);
							// apply weight to the delta values
							deltax[line] *= weight[line];
							deltay[line] *= weight[line];
						}
					}
					// switch the colour of the destination pixel to that of the source pixel
					changePixels(x, y, deltax, deltay, weight, frame, color[y][x], image, result, pFrame);
				}
			}
		}
		
		// switch pixel values of the destination and source image
		public void changePixels(int x, int y, double[] deltax, double[] deltay, double[] weight, int frame, int color, BufferedImage image
									, BufferedImage[] result, PictureFrame[] pFrame) {
			double totaldeltax = 0, totaldeltay = 0, totalweight = 0;
			// add up the delta and weight values from all lines
			for (int i = 0; i < numLines; i++) {
				totaldeltax += deltax[i];
				totaldeltay += deltay[i];
				totalweight += weight[i];
			}
			// find the overall weighted delta values
			totaldeltax /= totalweight;
			totaldeltay /= totalweight;
			// find the source point based on the final delta value
			double sourcex = totaldeltax + x;
			double sourcey = totaldeltay + y;
			// get the ratio of the frame number to the total number of frames
			double fractionx = (frame+1)/Double.parseDouble(numFrames.getText());
			double fractiony = (frame+1)/Double.parseDouble(numFrames.getText());
			try {
				// swap the color value of the destination pixel with the color value of the source, with 
				// linear interpolation according to the ratio of frame number to total number of frames
				color = image.getRGB((int)((sourcex - x) * fractionx + x),
				(int)((sourcey - y) * fractiony + y));
				pFrame[frame].setColorValue(color, x, y, result[frame]);
				result[frame].setRGB(x,y, color);
			}
			// draw a black pixel if an out of bounds coordinate is used
			catch (Exception ex) {
				result[frame].setRGB(x,y,-1);
			}
		}
		
		// creates a composite image by blending the colours from two other images
		public void crossDissolve(BufferedImage[] finalResult, int frame, int x, int y) {
			int r1, g1, b1, r2, g2, b2;
			// convert the pixel colours of the two images to RGB values from 0 - 255
			r1 = (pFrame4[frame].getColorValue(x,y) >> 16) & 0xff;
			g1 = (pFrame4[frame].getColorValue(x,y) >> 8) & 0xff;
			b1 = (pFrame4[frame].getColorValue(x,y) & 0xff);
			r2 = (pFrame3[frameNum-1-frame].getColorValue(x,y) >> 16) & 0xff;
			g2 = (pFrame3[frameNum-1-frame].getColorValue(x,y) >> 8) & 0xff;
			b2 = (pFrame3[frameNum-1-frame].getColorValue(x,y) & 0xff);
			int totalR, totalG, totalB;
			// combine the colors from the two images, with more weight on the source image in
			// early frames and more weight on the destination in later frames
			totalR = (int)((frameNum-frame)*r1/frameNum + frame*r2/frameNum);
			totalG = (int)((frameNum-frame)*g1/frameNum + frame*g2/frameNum);
			totalB = (int)((frameNum-frame)*b1/frameNum + frame*b2/frameNum);
			Color finalColor = new Color(totalR, totalG, totalB);
			// set the color of each pixel in the result to be the combined color
			try {
				finalResult[frame].setRGB(x,y,finalColor.getRGB()); 
			}
			// draw a black pixel if an out of bounds coordinate is used
			catch (Exception ex) {
				finalResult[frame].setRGB(x,y,-1);
			}
		}
	}
}