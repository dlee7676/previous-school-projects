package Project3931;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.Color;
import java.util.Scanner;
import java.awt.event.*;
import javax.swing.event.MouseInputAdapter;

public class Fframe extends JFrame
{

    // draws the Fourier graph

    byte[] samples;
    double[] samplesa = new double[1024];
	private Capture theRecorder; 
	int loweri = 0;
	int upperi = 125;
	int upper = 330;
	int lower = 0;
	int lowerl, upperl;
	String setlower, setupper;
	boolean filterset = false;
	double[] magnitude;
	int length;
	int x, y, curx, cury, xSample, xCurSample, xPos;
	boolean selected = false;
	boolean outside = false;
	JButton forward = new JButton("forward");
	JButton back = new JButton("back");
	final JButton set = new JButton("Set filter");

    public Fframe(final String title) {
        super(title);
    }

	double[] wave1 = new double[500];
	
    public void init(byte[] input, int lowerin, int upperin, int rate, Capture c) {
		// set up frame
		theRecorder = c;
		GridLayout layout = new GridLayout();
		final GraphPanel panel = new GraphPanel(); 
		final JPanel bar = new JPanel(layout); 
		samples = input;                              
		add(panel, BorderLayout.CENTER);
		lowerl = lowerin;
		upperl = upperin;
		add(bar, BorderLayout.NORTH);
		bar.add(back);
		bar.add(forward);
		bar.add(set);
		
		Fourier f = new Fourier(samples.length, rate);
		int end = 50;
		int start = 0;
		int endi = 50;
		int starti = 0;
		while ((lowerl + end) <= upperl) { // perform forward Fourier in 50-sample chunks
			f.calculate(samples, lowerl + start, lowerl + end);
			start += endi;
			end += endi;
		}
		magnitude = f.getMagnitude();

		// listener for button to move forward in the graph
		ActionListener forwardListener = new ActionListener() { 
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				if (upper + upperi < samples.length) {
					lower = lower + upperi; // advance the values to display
					upper = upper + upperi;
					panel.repaint();
				}
            }
        };
		forward.addActionListener(forwardListener);
		
		// listener for moving back in the graph
		ActionListener backListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				if (lower - upperi >= 0) {
					lower = lower - upperi;
					upper = upper - upperi;
					panel.repaint();
				}
            }
        };
		back.addActionListener(backListener);
		
		// listener for setting filter graphically
		ActionListener setListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				theRecorder.setFilter(xSample, xCurSample); // sets the filter bounds
															// to the highlighted region
            }
        };
		set.addActionListener(setListener);
		
		MouseEvents mouseListen = new MouseEvents(); // listener for mouse actions
		addMouseListener(mouseListen);
		addMouseMotionListener(mouseListen); 
	}

	public class GraphPanel extends JPanel { // panel for displaying Fourier graph
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
	
			int j = 60;
			int inputnum;
			int lastY = -1;
			
			// draw axes and labels
			g.drawString(Integer.toString(lower + lowerl), 60, 400);
			g.drawString(Integer.toString((int)Math.floor((lower+upper)/2 + lowerl)), 490, 400);
			g.drawString(Integer.toString(upper + lowerl), 950, 400); 
			drawAxes(g);
			
			for (int i = lower; i < upper; i++) {
				inputnum = (int)(magnitude[i]/10); // draw bars representing the magnitudes
				g.setColor(Color.red);
				g.fillRect(j, 380 - inputnum, 2, inputnum); 
				j = j + 3;
			}
			if(selected == true) { // display selection area when clicking and dragging mouse
				g.setColor(new Color(0,0,255,128));
				if (curx - x > 0)
					g.fillRect(x, 0, curx - x, 500);
				else g.fillRect(x, 0, 1, 500);
			}
			if (outside == true) { // scroll graph forward when dragging off the window
				if (upper + 6 < samples.length) {
					lower = lower + 6;
					upper = upper + 6;
					x = x - 18;
					repaint();
				}
			}
		}
	}
	
	public class MouseEvents extends MouseInputAdapter {
		public void mousePressed(MouseEvent e) { // set position of selection when mouse 
												 // is pressed
			repaint();
			x = e.getX();
			y = e.getY();
			// determine the sample shown at these coordinates
			xSample = (int)Math.floor(x/3) + lower + lowerl; 
			while (xSample % 3 != 0)
				xSample--;
			curx = x;
			cury = y;
			selected = true;
			outside = false;
			repaint();
		}
		
		public void mouseDragged(MouseEvent e) {
			curx = e.getX(); // update selection position as mouse is dragged
			cury = e.getY();
			repaint();
		}
		
		public void mouseReleased(MouseEvent e) {
			outside = false;
			// determine sample at end of selection when mouse is released
			xCurSample = (int)Math.floor(curx/3) + lower + lowerl;
			while (xCurSample % 3 != 0)
				xCurSample--;
		}
		
		public void mouseExited(MouseEvent e) {
			outside = true; // set a flag if mouse is outside the window
		}
		
		public void MouseEntered(MouseEvent e) {
			outside = false; // unset flag if mouse is in the window
		}	
	}
	
	public void drawAxes(Graphics g) { // draw axes
		g.drawString("Frequency bin", 450, 420);
			g.drawString("1000", 10, 280);
			g.drawString("2000", 10, 180);
			g.drawString("3000", 10, 80);
			g.setColor(new Color(0,0,0,128));
			g.drawLine(50, 0, 50, 1300);
			g.drawLine(0, 380, 1300, 380);
	}
}

