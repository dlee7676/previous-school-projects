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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import java.awt.event.*;
import javax.swing.event.MouseInputAdapter;


public class Timeframe extends JFrame
{
    // draws the time domain graph

    byte[] samples;
    byte[] temp;
	Capture record = new Capture(); 
	int loweri = 0;
	int upperi = 600;
	int lower = 0;
	int upper = 1000;
	int x, y, curx, cury, xSample, xCurSample;
	boolean selected = false;
	boolean outside = false;

    public Timeframe(final String title) {
        super(title);
    }
	
    public void init(byte[] input) {
		// set up the frame
		GridLayout layout = new GridLayout();
		final GraphPanel panel = new GraphPanel(); 
		final JPanel bar = new JPanel(layout); 
		samples = input;                              
		add(panel, BorderLayout.CENTER);
		JButton forward = new JButton("forward");
		JButton back = new JButton("back");
		JButton cut = new JButton("cut");
		JButton copy = new JButton("copy");
		JButton paste = new JButton("paste");
		add(bar, BorderLayout.NORTH);
		bar.add(back);
		bar.add(forward);
		bar.add(cut);
		bar.add(copy);
		bar.add(paste);		
		
		// listener for moving the graph forward
		ActionListener forwardListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				if (upper + upperi < samples.length) { // advance the samples to graph
					lower = lower + upperi;
					upper = upper + upperi;
					panel.repaint();
				}
            }
        };
		forward.addActionListener(forwardListener);
		
		// listener for moving the graph back
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
		
		// listener for cutting the graph
		ActionListener cutListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				// copy the cut samples into a temp array 
				temp = new byte[xCurSample - xSample + 1];
				for (int i = xSample, j = 0; i <= xCurSample; i++, j++)
					temp[j] = samples[i];
				// replace the cut samples with the samples ahead of the cut area
				for (int i = xSample, j = 0; (xCurSample + 1 + j) < samples.length; i++, j++) 
					samples[i] = samples[xCurSample + 1 + j];
	            panel.repaint();
            }
        };
        cut.addActionListener(cutListener);
        
        // listener for copying
        ActionListener copyListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				selected = false;
				outside = false;
				// make a temp array containing the copied samples
				temp = new byte[xCurSample - xSample + 1];
				for (int i = xSample, j = 0; i <= xCurSample; i++, j++)
					temp[j] = samples[i];
	            panel.repaint();
            }
        };
        copy.addActionListener(copyListener);
        
        // listener for pasting
        ActionListener pasteListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				byte[] buffer = new byte[samples.length + temp.length];
				selected = false;
				outside = false;
				// copy the area ahead of the pasting area into a buffer
				for (int i = xSample, j = 0; 
				     (xSample + temp.length + 1 + j) < samples.length; i++, j++) {
					buffer[xSample + temp.length + 1 + j] = samples[i];
				}
				// set the samples ahead of the pasting area to equal the buffer samples
				// (make room for the pasted content)
				for (int j = 0; (xSample + temp.length + 1 + j) < samples.length; j++) {
					samples[xSample + temp.length + 1 + j] = 
					   buffer[xSample + temp.length + 1 + j];
				}
				// copy the samples from the temp array into the sample array,
				// overwriting the samples within the length of the temp array
				for (int i = xSample, j = 0; j < temp.length; i++, j++) {
					samples[i] = temp[j];
				}
	            panel.repaint();
            }
        };
        paste.addActionListener(pasteListener);
        
		MouseEvents mouseListen = new MouseEvents(); // listener for mouse actions
		addMouseListener(mouseListen);
		addMouseMotionListener(mouseListen); 
    }
	
	public class GraphPanel extends JPanel { // panel for displaying time graph
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		
			int spacing = 50;
			int inputnum;
			int lastY = -1;
			double[] magnitude;
			int length;
			String cont = "y";
			
			// draw axes and labels
			g.drawString(Integer.toString(lower), 60, 420);
			g.drawString(Integer.toString((lower+upper)/2), 500, 420);
			g.drawString(Integer.toString(upper), 950, 420); 
			drawAxes(g);
			
			for (int i = lower; i < upper; i++) {
				g.setColor(Color.green); // draw lines between sample points
				inputnum = (2*samples[i]);
				spacing = spacing + 1;
				if (lastY != -1) {
					g.drawLine((spacing-1) - 1, -1 * lastY + 275, 
					(spacing - 1), -1 * inputnum + 275);
				}
				lastY = inputnum;
			}
			if(selected == true) {
				// draw selection rectangle if mouse clicked
				g.setColor(new Color(0,0,255,128));  
				if (curx - x > 0)
					g.fillRect(x, 0, curx - x, 600);
				else g.fillRect(x, 0, 1, 600);
			}
			if (outside == true) {
				// scroll graph forward if mouse is dragged outside window
				if (upper + 12 < samples.length) {
					lower = lower + 12;
					upper = upper + 12;
					x = x - 12;
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
			xSample = x + lower;
			curx = x;
			cury = y;
			System.out.println("selected: " + xSample);
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
			xCurSample = curx + lower;
			System.out.println("end of selection: " + xCurSample);
		}
		
		public void mouseExited(MouseEvent e) {
			// set a flag if mouse is outside the window
			outside = true;
		}
		
		public void MouseEntered(MouseEvent e) {
			// unset flag if mouse is in the window
			outside = false;
		}	
	}

	public void drawAxes(Graphics g) { // draw axes and numbers
		g.drawString("Sample", 490, 475);
		g.drawString("0", 10, 275);
		g.drawString("50", 10, 175);
		g.drawString("100", 10, 75);
		g.drawString("-50", 10, 375);
		g.drawString("-100", 10, 475);
		g.setColor(new Color(0,0,0,128));
		g.drawLine(50, 0, 50, 1300);
		g.drawLine(0, 275, 1300, 275);
	}
}

