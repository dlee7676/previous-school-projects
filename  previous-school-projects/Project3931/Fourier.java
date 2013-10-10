package Project3931;

public class Fourier {
	
// performs Fourier calculations	
	
	int length;
	int f;
	int magnitudecount;
	int rate;
	double[] waveCos;
	double[] waveSin;
	double[] ampcos;
	double[] ampsin;
	double[] magnitude;
	double re = 0;
	double im = 0;
	double[] sum1;
	double[] sum2;
	double[] inversecos;
	double[] inversesin;
	double[] inversesamples;
	String str;
	
	// initialize with sample length and sample rate
	public Fourier (int lengthIn, int rateIn) { 
	    length = lengthIn;	
	    rate = rateIn;
	    f = 0;
	    magnitudecount = 0;
	    waveCos = new double[length];
	    waveSin = new double[length];
		ampcos = new double[length];
		ampsin = new double[length];
		magnitude = new double[length];
		sum1 = new double[length];
		sum2 = new double[length];
		inversecos = new double[length];
		inversesin = new double[length];
		inversesamples = new double[length];
	}
	
	// foward Fourier transform
	public void calculate(byte[] samples, int lowerl, int upperl) {
		if (upperl > rate) // do not calculate bins higher than the maximum
			upperl = rate;

		for (int f = lowerl; f < upperl; f++) {
				re = 0;
				for (int t = 0; t < samples.length; t++) { // calculate real portion
					waveCos[t] = samples[t] * (Math.cos((2*Math.PI*f*t/rate)));
					re += waveCos[t];
				}
				im = 0;
				for (int t = 0; t < samples.length; t++) { // calculate imaginary 
					waveSin[t] = samples[t] * (Math.sin((2*Math.PI*f*t/rate)));
					im += waveSin[t];
				}
			ampcos[magnitudecount] = (1)*re;
			ampsin[magnitudecount] = (-1)*im;
			magnitude[magnitudecount] = Math.sqrt((re*re) + (im*im)); // calculate magnitude
			magnitudecount++;
		}
	}
	
	// Forward Fourier that takes a double array; used for testing purposes
	public void calculate(double[] samples, int lowerl, int upperl) {
		if (upperl > rate)
			upperl = rate;
			
		for (int f = lowerl; f < upperl; f++) {
			re = 0;
			for (int t = 0; t < samples.length; t++) {
				waveCos[t] = samples[t] * (Math.cos((2*Math.PI*f*t/rate)));
				re += waveCos[t];
			}
			im = 0;
			for (int t = 0; t < samples.length; t++) {
				waveSin[t] = samples[t] * (Math.sin((2*Math.PI*f*t/rate)));
				im += waveSin[t];
			}
			ampcos[magnitudecount] = re;
			ampsin[magnitudecount] = (-1)*im;
			magnitude[magnitudecount] = Math.sqrt((re*re) + (im*im));
			str = String.format("%.4g", magnitude[magnitudecount]);
			System.out.println("Magnitude of f" + magnitudecount + ": " + str);
			magnitudecount++;
		}
	}
	
	// inverse Fourier transform	
	public void calculateInverse(double[] samplescos, double[] samplessin, int lowerl, int upperl) {
		if (upperl > samplescos.length)
			upperl = samplescos.length;
			
		for (int t = 0; t < length; t++) {
			sum1[t] = 0;
			for (f = 0; f < length; f++) { // calculate real
				inversecos[f] = (samplescos[f] + samplessin[f]) * (Math.cos(2*Math.PI*f*t/(rate)));
				sum1[t] += inversecos[f];
			}
			sum2[t] = 0;
			for (f = 0; f < length; f++) { // calculate imaginary
				inversesin[f] = (samplescos[f] + samplessin[f]) * (Math.sin((2*Math.PI*f*t/(rate))));
				sum2[t] += inversesin[f];
			}		
		}
		
		for (int t = lowerl; t < upperl; t++) 
			inversesamples[t] = (sum1[t] + sum2[t]);
	}
	
// getters and setters
	public int getMagnitudeCount() {
		return magnitudecount;
	}
	
	public double[] getCos() {
		return ampcos;
	}
	
	public double[] getSin() {
		return ampsin;
	}
	
	public double[] getMagnitude() {
		return magnitude;
	}
	
	public double[] getInverseSamples() {
		return inversesamples;
	}
}
