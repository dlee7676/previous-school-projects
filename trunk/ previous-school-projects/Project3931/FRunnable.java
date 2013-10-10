package Project3931;

import java.awt.Rectangle;

// performs forward Fourier and graphs the results as a separate thread 
public class FRunnable implements Runnable {
	private byte[] samples;
	private int lowerl;
	private int upperl;
	private String lowMain, upperMain;
	private boolean setM = false;
	private int rate;
	Capture in;

	public FRunnable(byte[] samplesIn, int lowerIn, int upperIn, int rateIn, Capture c) {
		samples = samplesIn;
		lowerl = lowerIn;
		upperl = upperIn;
		rate = rateIn;
		in = c;
	}
	
	public void run() {
		Fframe bar = new Fframe("Frequency graph"); // create the frame for the Fourier graph
		final Rectangle bounds = new Rectangle(10, 10, 1000, 500);
		bar.setBounds(bounds);
		bar.init(samples, lowerl, upperl, rate, in);
		bar.setVisible(true);
	}
}
