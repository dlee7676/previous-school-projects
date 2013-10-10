/* PictureFrame.java
   Frame for viewing source and destination images and manipulating control lines.
*/

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSlider;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PictureFrame extends JFrame { 

	// corrections for error in coordinate positions in the panel vs. cursor position on the screen
	private static final int XCORRECTION = 10;
	private static final int YCORRECTION = 64;
	private Line[] lines = new Line[128]; // array of line objects to keep track of control lines
	private BufferedImage image;
	private PicturePanel pPanel = new PicturePanel();
	private FrameMain parent;
	private boolean newLine = false, drawing = false;
	private boolean startPointSelected = false, endPointSelected = false;
	private boolean linesExist = false;
	private boolean pointSet = false;
	private int startx, starty, curx, cury;
	private double sourceX, sourceY;
	private int numLines = 0;
	private int pointNum;
	private int size = 512;
	private int[][] colorval; // stores color values for pixels
	private final JPanel bar = new JPanel();
	private final JRadioButton draw = new JRadioButton("draw");
	private final JRadioButton select = new JRadioButton("select");
	private final JButton delete = new JButton("Delete line");

	public PictureFrame(String title) {
		super(title);
	}
	
	// set the array of color values to be the size of the image in the panel
	public void setSize(BufferedImage img) {
		colorval = new int[img.getHeight()][img.getWidth()];
	}
	
	// initialize the frame components
	public void init(BufferedImage img, FrameMain p) {
		parent = p;
		MouseEvents mouseListen = new MouseEvents(); // listener for mouse actions
		addMouseListener(mouseListen);
		addMouseMotionListener(mouseListen);
		repaint();
		image = img; // set the image to be shown in the panel
		add(pPanel, BorderLayout.CENTER);
		add(bar, BorderLayout.NORTH); // top bar with buttons
		bar.add(draw);
		draw.setSelected(true);
		bar.add(select);
		bar.add(delete);
		
		// set up radio buttons
		ButtonGroup tools = new ButtonGroup();
		tools.add(draw);
		tools.add(select);
		
		// action listener for the delete line button
		ActionListener deleteListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.delete(pointNum);
				draw(lines, numLines);
			}
		};
		delete.addActionListener(deleteListener);
	}
	
	public void showPoint (double x, double y) {
		pointSet = true;
		sourceX = x;
		sourceY = y;
		System.out.println("frame generated, origin moved to " + sourceX + ", " + sourceY);
	}
	
	// draw all lines when the panel is painted
	public void draw(Line[] l, int count) {
		lines = l;
		numLines = count; // keep track of the number of lines
		drawing = true;
		repaint();
	}
	
	// store the color value at a given pixel
	public void setColorValue(int value, int x, int y, BufferedImage img) {
		colorval[y][x] = value;
	}
	
	// get the color value of a pixel
	public int getColorValue(int x, int y) {
		return colorval[y][x];
	}
	
	// the panel for displaying the images
	public class PicturePanel extends JPanel {
		// actions performed when the panel is painted
		public void paint (Graphics g) {
			g.drawImage(image, 0, 0, null); // draw the loaded image
			if (linesExist) // draw all existing lines when panel is painted (eg. on resize)
				draw(lines, numLines);
			if (newLine == true) { // rubberband drawing of lines
				g.setColor(Color.magenta);
				g.drawLine(startx, starty, curx, cury);
			}
			if (drawing == true) { // draw lines when the draw() function is called
				for (int i = 0; i <= numLines; i++) {
					g.setColor(Color.black);
					g.drawLine(lines[i].getStartX(), lines[i].getStartY(), lines[i].getEndX(), lines[i].getEndY());
					g.setColor(Color.green); // mark start point with a green circle and end with a red one
					g.fillOval(lines[i].getStartX() - 3, lines[i].getStartY() - 3, 8, 8);
					g.setColor(Color.red);
					g.fillOval(lines[i].getEndX() - 3, lines[i].getEndY() - 3, 8, 8);
				}
				drawing = false;
			}
			if (startPointSelected || endPointSelected) { // highlight selected points with a blue circle
				g.setColor(Color.blue);
				g.fillOval(curx - 5, cury - 5, 8, 8);
			}
			if (pointSet)
				g.setColor(Color.magenta);
				g.fillOval((int)(sourceX - 5), (int)(sourceY - 5), 8, 8);
		}
	}
	
	// listener for mouse events
	public class MouseEvents extends MouseInputAdapter {
		public void mousePressed(MouseEvent e) { // set position of selection when mouse 
												 // is pressed
			startPointSelected = false;
			endPointSelected = false;
			if (draw.isSelected()){ // in draw mode, set points for rubberband drawing of lines
				startx = e.getX() - XCORRECTION;
				starty = e.getY() - YCORRECTION;
				curx = e.getX() - XCORRECTION;
				cury = e.getY() - YCORRECTION;
				newLine = true;
			}
			if (select.isSelected()) { // in select mode, check if an endpoint is close to the cursor
				pointNum = 0;
				for (int i = 0; i <= numLines; i++) { // for each line, check if the start point is within
													  // a certain radius of the line's start point
					if (lines[i].isStartPoint(e.getX(), e.getY())) {
						startPointSelected = true;
						repaint();
						pointNum = i; // remember which line is having its point selected
						break;
					}
					if (lines[i].isEndPoint(e.getX(), e.getY())) { // for each line, check if the end point is within
													               // a certain radius of the line's end point
						endPointSelected = true;
						repaint();
						pointNum = i; // remember which line is having its point selected
						break;
					}
				}
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			if (linesExist) // keep lines visible while mouse is being dragged
				draw(lines, numLines);
			curx = e.getX() - XCORRECTION; // update current point position as mouse is dragged
			cury = e.getY() - YCORRECTION;
			repaint();
			// if a start point is selected, set the line's start coordinates to match the current x and y position
			if (startPointSelected == true) { 
				lines[pointNum].setStartX(e.getX() - XCORRECTION);
				lines[pointNum].setStartY(e.getY() - YCORRECTION);
				draw(lines, numLines);
			}
			// if an end point is selected, set the line's end coordinates to match the current x and y position
			if (endPointSelected == true) {
				lines[pointNum].setEndX(e.getX() - XCORRECTION);
				lines[pointNum].setEndY(e.getY() - YCORRECTION);
				draw(lines, numLines);
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			// in draw mode, draw a line from the point where the mouse was pressed to where it was released
			if (draw.isSelected()) { 
				newLine = false;
				Line l = new Line(startx, starty, curx, cury);
				parent.updateLines(l); // draw the line in both source and destination frames
				linesExist = true; // flag that lines have been drawn
			}
		}
	}
}