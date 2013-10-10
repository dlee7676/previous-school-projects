package Project3931;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.EventQueue;
import java.awt.Rectangle;

public class Main {
// Starts the wave analyzer. 

	public static void main(String args[]) {
		JFrame frame = new Capture(); // invoke the control panel
		final Rectangle mainBounds = new Rectangle(10, 70, 1000, 200);
		frame.setBounds(mainBounds);
		frame.show();
    }
}
