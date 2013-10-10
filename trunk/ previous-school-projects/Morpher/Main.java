/* Main.java
   The entry point for the morpher program. 
*/

import javax.swing.JFrame;
import java.awt.Rectangle;

public class Main {
	public static void main(String[] args) {
		FrameMain frameMain = new FrameMain("Morpher");
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameMain.getContentPane();
		final Rectangle bounds = new Rectangle (10, 10, 300, 180);
		frameMain.setBounds(bounds);
		//frameMain.pack();
		frameMain.setVisible(true);
	}
}