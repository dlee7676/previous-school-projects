/* MorphFrame.java
   Frame for viewing the final morphed images.
*/

import javax.swing.JPanel;
import javax.swing.JFrame;
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
import javax.swing.Timer;

public class MorphFrame extends JFrame { 
	private BufferedImage[] images = new BufferedImage[512];
	private int currentImage;
	private int frameNum;
	private ActionListener buttonListener = new ButtonListener();
	private MorphPanel mPanel = new MorphPanel();
	private FrameMain parent;
	private final JPanel bar = new JPanel();
	private final JButton forward = new JButton("forward");
	private final JButton back = new JButton("back");
	private final JButton start = new JButton("start");
	private final JButton end = new JButton("end");
	private final JButton animate = new JButton("Animate");
	private final Timer timer = new Timer(100, buttonListener); // timer for animation; fires every 100 ms

	public MorphFrame(String title) {
		super(title);
	}
	
	public void init(BufferedImage[] img, FrameMain p, int frames) {
		parent = p;
		frameNum = frames;
		images = img; // associate with an array of images to choose from
		currentImage = 0; // index of the image to be displayed; start with the first one
		repaint();
		add(mPanel, BorderLayout.CENTER);
		add(bar, BorderLayout.NORTH);
		bar.add(start);
		bar.add(back);
		bar.add(forward);
		bar.add(end);
		bar.add(animate);
		start.addActionListener(buttonListener);
		end.addActionListener(buttonListener);
		back.addActionListener(buttonListener);
		forward.addActionListener(buttonListener);
		animate.addActionListener(buttonListener);
	}
	
	public class MorphPanel extends JPanel {
		public void paint (Graphics g) {
			// draw the currently selected image
			g.drawImage(images[currentImage], 0, 0, null);
		}
	}
	
	public class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == start) {
				// select the first image
				currentImage = 0;
				repaint();
			}
			if (e.getSource() == back) {
				// go back by 1 image
				if (currentImage - 1 >= 0) {
					currentImage -= 1;
					repaint();
				}
			}
			if (e.getSource() == forward) {
				// go forward 1 image
				if (currentImage + 1 <= frameNum+1) {
					currentImage += 1;
					repaint();
				}
			}
			if (e.getSource() == end) {
				// go to the last image
				currentImage = frameNum+1;
				repaint();
			}
			// play all images as an animation
			if (e.getSource() == animate) {
				currentImage = 0; // go back to the first image
				repaint();
				timer.start(); // start a timer that sends an event every 100 ms
			}
			if (e.getSource() == timer) {
				// when the timer signals, go forward 1 image if the last image is not being displayed
				if (currentImage < frameNum+1) { 
					currentImage++;
					repaint();
				}
				else timer.stop(); // stop the timer at the last image
			}
		}
	}
}