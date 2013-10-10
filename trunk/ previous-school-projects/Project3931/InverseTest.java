package Project3931;

public class InverseTest {
	public static void main(String[] args) {
		
		//  INVERSE FOURIER TEST: the inverse samples match the samples of the 
		//  					  initial wave
		
		double[] wave1 = new double[100];
		Fourier f = new Fourier(wave1.length, wave1.length);
		for (int i = 0; i < wave1.length; i++) { // create a test wave
			wave1[i] = Math.sin(2*Math.PI*15*i/wave1.length) + Math.sin(2*Math.PI*27*i/wave1.length);
			System.out.println("time " + i + " " + wave1[i]); // print sample values
		}
		int end = 50;
		int start = 0;
		int endi = 50;
		int starti = 0;
		while (end <= wave1.length) { // perform forward Fourier on the test wave
			f.calculate(wave1, start, end);
			start += endi;
			end += endi;
		}
		
		f.calculateInverse(f.getCos(), f.getSin(), 0, wave1.length); // perform inverse
		double[] inv = f.getInverseSamples();
		for (int i = 0; i < wave1.length; i++) // print inverse results
			System.out.println("inverse sample " + i + ": " + (-1*inv[i]/(wave1.length)));	
	}
}
