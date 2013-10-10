package Project3931;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import com.sun.media.sound.*;

public class Capture extends JFrame {
	
	/* The main control panel for the wave analyzer.
	   The audio capture, stop, and play functions were taken from the 
	   example at 
	   http://www.java-tips.org/java-se-tips/javax.sound/capturing-audio-with-java-sound-api.html.
	   Other code is original. */

	protected boolean running;
	ByteArrayOutputStream out;
	public boolean recorded = false;
	public byte[] samples;
	public byte[] original;
	double[] multipliers;
	final JTextField fourierLower = new JTextField("0");
	final JTextField fourierUpper = new JTextField("0");
	final JTextField filterLower = new JTextField("0");
	final JTextField filterUpper = new JTextField("0");
	final JTextField filterPoints = new JTextField("32");
	final JButton capture = new JButton("Capture");
	final JButton stop = new JButton("Stop");
	final JButton play = new JButton("Play");
	final JButton time = new JButton("Time graph");
	final JButton fourier = new JButton("Fourier graph");
	final JButton window = new JButton("Apply window");
	final JButton filter = new JButton("Filter");
	final JButton save = new JButton("Save");
	final JButton open = new JButton("Open");
	final JButton revert = new JButton("Revert");
	final JFileChooser fc = new JFileChooser();
	final JLabel length = new JLabel("Sample length: ");
	final JLabel filterLabel2 = new JLabel("(Hz)");
	final JLabel fourierLimits = new JLabel ("Fourier bounds");
	final JLabel fourierLabel2 = new JLabel ("(range of f)");
	final JLabel filterLimits = new JLabel ("Filter bounds");
	final JLabel filterPointsLabel = new JLabel ("Filter points");
	final JLabel rateLabel = new JLabel("Sample rate (Hz)");
	final JLabel levelLabel = new JLabel("Quantization levels (bits)");
	final JLabel channelLabel = new JLabel("Channels");
	final JLabel options = new JLabel("Options:");
	final JLabel spacer = new JLabel(" ");
	String[] windowTypes = {"No window", "Hamming", "Hann", "Triangular"};
	String[] sampleRates = {"8000", "16000", "22050"};
	String[] levels = {"8", "16"};
	String[] stereo = {"1", "2"}; 
	final JComboBox windows = new JComboBox(windowTypes);
	final JComboBox selectRate = new JComboBox(sampleRates);
	final JComboBox quantization = new JComboBox(levels);
	final JComboBox selectChannels = new JComboBox(stereo);
	final JSlider equalizerLower = new JSlider(0, 4000, 0);
	final JSlider equalizerUpper = new JSlider(0, 4000);
	final JCheckBox eqOn = new JCheckBox("Equalizer (sliders in Hz)");
	float sampleRate = 8000;
	int sampleSizeInBits = 8;
	int channels = 1;
	boolean signed = true;
	boolean bigEndian = true;

	public Capture() {
		// set up components for the frame
		super("Capture Sound Demo");
		final Capture theRecorder = this;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container content = getContentPane();
		GridLayout layout = new GridLayout();
		GridLayout rows = new GridLayout(2,1);
		GridLayout extras = new GridLayout(3,4);
		JPanel east = new JPanel(layout);
		JPanel west = new JPanel(layout);
		JPanel north = new JPanel(layout);
		JPanel mid = new JPanel(rows);
		JPanel south = new JPanel(extras);
		JPanel filterSubPanel = new JPanel(rows);
		JPanel pointsPanel = new JPanel(rows);
		equalizerLower.setMajorTickSpacing(1000);
		equalizerLower.setPaintLabels(true);
		equalizerUpper.setMajorTickSpacing(1000);
		equalizerUpper.setPaintLabels(true);
		
		// add everything to the frame
		addContent(content, north, west, mid, east, south, filterSubPanel,
		           pointsPanel);

		// disable buttons that are not usable without a sample
		capture.setEnabled(true);
		stop.setEnabled(false);
		play.setEnabled(false);
		time.setEnabled(false);
		fourier.setEnabled(false);
		filter.setEnabled(false);
		revert.setEnabled(false);

		// listener for capturing audio
		ActionListener captureListener = 
			new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			capture.setEnabled(false);
			stop.setEnabled(true);
			play.setEnabled(false);
			
			captureAudio();
		  }
		};
		capture.addActionListener(captureListener);

		// listener for stopping capture
		ActionListener stopListener = 
			new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			capture.setEnabled(true);
			stop.setEnabled(false);
			play.setEnabled(true);
			time.setEnabled(true);
			fourier.setEnabled(true);
			filter.setEnabled(true);
			revert.setEnabled(true);
			running = false;
			
			samples = out.toByteArray(); // set sample array to the bytes captured
			original = new byte[samples.length];
			for (int i = 0; i < samples.length; i++)
				original[i] = samples[i];
				
			String strtext = ("Sample length: " + samples.length);
			length.setText(strtext);
		  }
		};
		stop.addActionListener(stopListener);

		// listener for playing sample
		ActionListener playListener = 
			new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			playAudio();
		  }
		};
		play.addActionListener(playListener);
	  
		ActionListener timeListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Timeframe time = new Timeframe("Time graph"); // invokes a time domain graph
				final Rectangle bounds = new Rectangle(10, 10, 1000, 600);
				time.setBounds(bounds);
				time.setVisible(true);
				time.init(samples);
			}
		};
		time.addActionListener(timeListener);
		
		ActionListener fourierListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] fourierInput = samples;
				int lowbound = Integer.parseInt(fourierLower.getText());
				int upperbound = Integer.parseInt(fourierUpper.getText());
				
				// applies windowing options
				fourierInput = windowing(fourierInput, lowbound, upperbound);
				System.out.println("calculating");
				// perform Fourier calculations and make graph in a separate thread
				FRunnable r1 = new FRunnable(fourierInput, lowbound, upperbound, (int)sampleRate, theRecorder);
				new Thread(r1).start();
			}
		};
		fourier.addActionListener(fourierListener);
		
		ActionListener filterListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int points = Integer.parseInt(filterPoints.getText());
				int numbins = (int)Math.floor(sampleRate/points);
				
				// convolve samples according to the chosen values in the GUI
				samples = filter(samples, Integer.parseInt(filterLower.getText())/numbins,
								Integer.parseInt(filterUpper.getText())/numbins,
								points);
			}
		};
		filter.addActionListener(filterListener);
		
		ActionListener revertListener =
		new ActionListener() {
			// reverts samples to the initial captured values
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < samples.length; i++) {
					samples[i] = 0;
					samples[i] = original[i];
				}
			}
		};
		revert.addActionListener(revertListener);
		
		ActionListener openListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File inFile = new File("a.wav"); 
				// open file through file chooser
				int returnVal = fc.showOpenDialog(Capture.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					inFile = fc.getSelectedFile();
				}
				
				try {
					samples = openFile(samples, inFile); // get the samples as a byte array
				}
				catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
				play.setEnabled(true);
				time.setEnabled(true);
				fourier.setEnabled(true);
				filter.setEnabled(true);
			}
		};
		open.addActionListener(openListener);
		
		ActionListener saveListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File outFile = new File("out.wav"); 
				// get filename with a file chooser
				int returnVal = fc.showSaveDialog(Capture.this); 
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					outFile = fc.getSelectedFile();
				}
		
				try {
					// write to a file with the current audio format settings
					WaveFileWriter writer = new WaveFileWriter();
					InputStream input = new ByteArrayInputStream(samples);
					AudioFormat format = getFormat();
					AudioInputStream ais = new AudioInputStream(input, format, 
						samples.length / format.getFrameSize());
					writer.write(ais, AudioFileFormat.Type.WAVE, outFile);
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};
		save.addActionListener(saveListener);
		
		ItemListener formatListener = 
			new ItemListener() {
			// change audio format settings when combo box selecctions are changed
			public void itemStateChanged(ItemEvent e) { 
				if (e.getSource() == selectRate) {
					// change sample rate to new selection
					sampleRate = Float.parseFloat(selectRate.getSelectedItem().toString());
					// change equalizer labels according to sample rate
					equalizerLower.setMaximum((int)sampleRate/2);
					equalizerLower.setMajorTickSpacing((int)(sampleRate/2));
					equalizerLower.setPaintLabels(true);
					equalizerUpper.setMaximum((int)sampleRate/2);
					equalizerUpper.setMajorTickSpacing((int)(sampleRate/2));
					equalizerUpper.setPaintLabels(true);
				}
				if (e.getSource() == quantization)
					// change quantization levels
					sampleSizeInBits = Integer.parseInt(quantization.getSelectedItem().toString());
				if (e.getSource() == selectChannels)
					// change channels
					channels = Integer.parseInt(selectChannels.getSelectedItem().toString());
			}
		};
		selectRate.addItemListener(formatListener);
		quantization.addItemListener(formatListener);
		selectChannels.addItemListener(formatListener);
	}

	private void captureAudio() {
		try {
		  final AudioFormat format = getFormat(); // get the current audio format
		  DataLine.Info info = new DataLine.Info(
			TargetDataLine.class, format);
		  final TargetDataLine line = (TargetDataLine)
			AudioSystem.getLine(info); // line for getting audio input
		  line.open(format);
		  line.start();
		  Runnable runner = new Runnable() {
			int bufferSize = (int)format.getSampleRate() 
			  * format.getFrameSize();
			byte buffer[] = new byte[bufferSize]; // set up a buffer to hold byte data

			public void run() {
			  out = new ByteArrayOutputStream();
			  running = true;
			  try {
				int index = 0;
				while (running) {
				  index++;
				  // read the audio data on the line
				  int count = 
					line.read(buffer, 0, buffer.length); 
				  if (count > 0) {
				  // write the audio data into a byte array
					out.write(buffer, 0, count);  
				  }
				}
				out.close();
			  } catch (IOException e) {
				System.err.println("I/O problems: " + e);
				System.exit(-1);
			  }
			}
		  };
		  Thread captureThread = new Thread(runner);
		  captureThread.start(); // run capture in separate thread
		} catch (LineUnavailableException e) {
		  System.err.println("Line unavailable: " + e);
		  System.exit(-2);
		}
	}

	private void playAudio() {
		try {
		  byte audio[] = samples; // get the sample byte array
		  InputStream input = 
			new ByteArrayInputStream(audio);
		  final AudioFormat format = getFormat(); // get current audio format
		  // convert byte array into a playable form
		  final AudioInputStream ais = 
			new AudioInputStream(input, format, 
			audio.length / format.getFrameSize());  
		  DataLine.Info info = new DataLine.Info(
			SourceDataLine.class, format);
		  final SourceDataLine line = (SourceDataLine)
			AudioSystem.getLine(info); // line for outputting audio
		  line.open(format);
		  line.start();

		  Runnable runner = new Runnable() {
			int bufferSize = (int) format.getSampleRate() 
			  * format.getFrameSize();
			byte buffer[] = new byte[bufferSize]; // set up buffer to hold audio data

			public void run() {
			  try {
				int count;
				int points = Integer.parseInt(filterPoints.getText());
				int numbins = (int)Math.floor(sampleRate/points);
				
				// read the audio data and store chunks in the buffer
				while ((count = ais.read(
					buffer, 0, buffer.length)) != -1) {
						
				  if (count > 0) {	
					// if equalizer is on, perform filtering each time the buffer is read
					if (eqOn.isSelected()) 
						buffer = filter(buffer, equalizerLower.getValue()/numbins, 
										equalizerUpper.getValue()/numbins, 
										points); 
					// write the data in the buffer to the output line
					line.write(buffer, 0, count);
				  }
				}
				line.drain();
				line.close();
			  } catch (IOException e) {
				System.err.println("I/O problems: " + e);
				System.exit(-3);
			  }
			}
		  };
		  Thread playThread = new Thread(runner);
		  playThread.start(); // run audio playing as a separate thread
		} catch (LineUnavailableException e) {
		  System.err.println("Line unavailable: " + e);
		  System.exit(-4);
		} 
	}

	private void setFormat(float rate, int size, int channel) {
		sampleRate = rate;
		sampleSizeInBits = size;
		channels = channel;
	}

	private AudioFormat getFormat() { // get the current audio format
		return new AudioFormat(sampleRate, 
		  sampleSizeInBits, channels, signed, bigEndian);
	}

	public byte[] getSamples() { // get the sample array
		byte audio[] = out.toByteArray();
		return audio;  
	}

	public void setFilter(int lower, int upper) { // set the filter parameters
		filterLower.setText(Integer.toString(lower));
		filterUpper.setText(Integer.toString(upper));
	}
   
    public void addContent(Container content, JPanel north, JPanel west, 
						JPanel mid, JPanel east, JPanel south, 
						JPanel filterSubPanel, JPanel pointsPanel) {
		// add all content
		content.add(west, BorderLayout.WEST);
		content.add(east, BorderLayout.EAST);
		content.add(mid, BorderLayout.CENTER);
		content.add(north, BorderLayout.NORTH);
		content.add(south, BorderLayout.SOUTH);

		north.add(capture);
		north.add(stop);
		north.add(play);
		west.add(time);
		west.add(fourier);
		west.add(windows);
		mid.add(fourierLimits);
		mid.add(fourierLabel2);
		mid.add(fourierLower);
		mid.add(fourierUpper);
		east.add(filterSubPanel);
		filterSubPanel.add(filterLimits);
		filterSubPanel.add(filterLabel2);
		filterSubPanel.add(filterLower);
		filterSubPanel.add(filterUpper);
		east.add(pointsPanel);
		pointsPanel.add(filterPointsLabel);
		pointsPanel.add(filterPoints);
		east.add(filter);
		south.add(revert);
		south.add(open);
		south.add(save);
		south.add(length);
		south.add(spacer);
		south.add(options);
		south.add(rateLabel);
		south.add(selectRate);
		south.add(levelLabel);
		south.add(quantization);
		south.add(channelLabel);
		south.add(selectChannels);
		south.add(eqOn);
		south.add(equalizerLower);
		south.add(equalizerUpper);
	}
   
	public byte[] windowing(byte[] input, int lowbound, int upperbound) {
		double[] weights = new double[input.length];
		// apply the specified windowing function to the samples
		if (windows.getSelectedItem().toString().equals("Hamming")) {
			for (int i = lowbound, j = 0; i < upperbound; i++, j++) {
				weights[j] = 0.54 - (0.46*Math.cos(2*Math.PI*j/(
							 (double)upperbound-(double)lowbound)));
				input[i] *= weights[j];
			}
		}
		if (windows.getSelectedItem().toString().equals("Hann")) {
			for (int i = lowbound, j = 0; i < upperbound; i++, j++) {
				weights[j] = 0.5*(1 - Math.cos(2*Math.PI*j/(
							(double)upperbound-(double)lowbound)));
				input[i] *= weights[j];
			}
		}
		if (windows.getSelectedItem().toString().equals("Triangular")) {
			for (int i = lowbound, j = 0; i < upperbound; i++, j++) {
				weights[j] =(2/((double)upperbound-(double)lowbound+1)) * 
							((((double)upperbound-(double)lowbound+1)/2) - 
							Math.abs(j - ((double)upperbound-(double)lowbound)/2));
				input[i] *= weights[j];
			}
		}
		return input;
	}
   
   public byte[] filter(byte[] input, double lowbound, double upperbound, int points) {
		multipliers = new double[points];
		double lowboundr = points - upperbound;
		double upperboundr = points - lowbound;
		double[] zero = new double[points];
		double sum;
		double[] result;
			
		for (int i = 0; i < zero.length; i++)
			zero[i] = 0;
		// set up the filter bins to have 1's where the desired frequencies are
		// and 0's elsewhere
		for (int i = 0; i < points; i++) {
			if(i < lowbound)
				multipliers[i] = 0;
			else if (i > upperbound && i < lowboundr)
				multipliers[i] = 0;
			// handle reflections on the other side of the Nyquist limit
			else if (i > upperboundr)
				multipliers[i] = 0;
			else multipliers[i] = 1;
		}
		Fourier f = new Fourier(multipliers.length, multipliers.length);
		// inverse Fourier on the filter
		f.calculateInverse(multipliers, zero, 0, multipliers.length);
		result = f.getInverseSamples();
		// generate the filter weights
		for (int i = 0; i < result.length; i++)
			result[i] /= multipliers.length;
		// convolution of the samples on the filter weights
		for (int i = 0; i + points < input.length; i++) {
			sum = 0;
			for(int j = i, k = 0; k < points; j++, k++) {
				sum += input[j] * result[k];
			}
			input[i] = (byte)Math.floor(sum);
			input[i] *= 2;
		}
		return input;
	}


	public byte[] openFile(byte[] samples, File inFile) throws UnsupportedAudioFileException, 
	         IOException {
		WaveFileReader reader = new WaveFileReader();
		final AudioInputStream ais = reader.getAudioInputStream(inFile);
		final AudioFormat format = reader.getAudioFileFormat(inFile).getFormat();
		samples = new byte[(int)ais.getFrameLength() * format.getFrameSize()];

		for (int i = 0; i < samples.length; i++) 
			ais.read(samples);
		File temp = new File("temp.wav");
		WaveFileWriter writer = new WaveFileWriter();
		InputStream input = new ByteArrayInputStream(samples);
		AudioFormat format2 = getFormat();
		AudioInputStream ais2 = new AudioInputStream(input, format2, 
			samples.length / format.getFrameSize());
		writer.write(ais2, AudioFileFormat.Type.WAVE, temp);

		final AudioInputStream ais3 = reader.getAudioInputStream(temp);
		final AudioFormat format3 = reader.getAudioFileFormat(inFile).getFormat();
		samples = new byte[(int)ais3.getFrameLength() * format3.getFrameSize()];
		for (int i = 0; i < samples.length; i++) 
			ais3.read(samples);
		length.setText("Sample length: " + Integer.toString(samples.length));
		return samples;
	}
}

