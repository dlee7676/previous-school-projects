/* Line.java
   Class containing data associated with control lines.
*/

public class Line {
	private int startx, starty, endx, endy;
	private int lengthx, lengthy;
	private int vecXP2x, vecXP2y, vecPQ2x, vecPQ2y, vecNormalPQ2x, vecNormalPQ2y;
	private double dPointToLine, dPerpendicular, ratio;
	private double xline, yline;

	public Line(int startx_, int starty_, int endx_, int endy_) {
		startx = startx_;
		starty = starty_;
		endx = endx_;
		endy = endy_;
	}
	
	// "copy constructor" for lines
	public Line(Line another) {
		this.startx = another.startx;
		this.starty = another.starty;
		this.endx = another.endx;
		this.endy = another.endy;
	}
	
	// flag if a coordinate is within a radius of 10 pixels from a line's start point
	public boolean isStartPoint(int x, int y) {
		x -= 10;
		y -= 64;
		if (x <= startx + 10 && x >= startx - 10 && y <= starty + 10 && y >= starty - 10)
			return true;
		return false;
	}
	
	// flag if a coordinate is within a radius of 10 pixels from a line's end point
	public boolean isEndPoint(int x, int y) {
		x -= 10;
		y -= 64;
		if (x <= endx + 10 && x >= endx - 10 && y <= endy + 10 && y >= endy - 10)
			return true;
		return false;
	}
	
	// getters and setters for start and end coordinates
	public int getStartX() {
		return startx;
	}
	
	public int getStartY() {
		return starty;
	}
	
	public int getEndX() {
		return endx;
	}
	
	public int getEndY() {
		return endy;
	}
	
	public void setStartX(int x) {
		startx = x;
	}
	
	public void setStartY(int y) {
		starty = y;
	}
	
	public void setEndX(int x) {
		endx = x;
	}
	
	public void setEndY(int y) {
		endy = y;
	}
	
	// calculate the distance from a point to a line, the perpendicular bisect, and the fractional distance
	// of the intersection point on the line.
	public double calculateDistances(int x, int y) {
		int p2x = this.getStartX();
		int p2y = this.getStartY();
		int q2x = this.getEndX();
		int q2y = this.getEndY();
		// calculate x and y values of vectors
		vecXP2x = p2x - x;
		vecXP2y = p2y - y;
		//if (x == 0 && y == 0)
			//System.out.println ("XP vector: " + vecXP2x + ", " + vecXP2y);
		vecPQ2x = q2x - p2x;
		vecPQ2y = q2y - p2y;
		//if (x == 0 && y == 0)
			//System.out.println ("PQ vector: " + vecPQ2x + ", " + vecPQ2y);
		// normal of the line vector
		vecNormalPQ2x = -1*vecPQ2y;
		vecNormalPQ2y = vecPQ2x;
		//if (x == 0 && y == 0)
			//System.out.println ("normal vector: " + vecNormalPQ2x + ", " + vecNormalPQ2y);
		// projection of the vector from the destination point to the start point of the line 
		// onto the normal of the line
		dPointToLine = ((vecNormalPQ2x*vecXP2x + vecNormalPQ2y*vecXP2y)/ 
				(Math.sqrt(vecNormalPQ2x*vecNormalPQ2x + vecNormalPQ2y * vecNormalPQ2y)));
		//if (x == 0 && y == 0)
			//System.out.println ("distance to line: " + dPointToLine);
		// projection of the vector from the start point of the line to the destination point onto the line vector
		dPerpendicular = ((vecPQ2x*vecXP2x + vecPQ2y*vecXP2y)/
				 (Math.sqrt(vecPQ2x*vecPQ2x + vecPQ2y*vecPQ2y)));
		// calculate fractional distance
		ratio = dPerpendicular/Math.sqrt(vecPQ2x*vecPQ2x + vecPQ2y*vecPQ2y);
		return ratio;
	}
	
	public void setRatio(double ratio_) {
		ratio = ratio_;
	}
	
	// getters for values calculated above
	public double getDistanceToLine() {
		return dPointToLine;
	}
	
	public double getPerpendicularDistance() {
		return dPerpendicular;
	}
	
	public double getRatio() {
		return ratio;
	}
	
	// getters for the x and y components of the line vector
	public int getLengthX() {
		return vecXP2x;
	}
	
	public int getLengthY() {
		return vecXP2y;
	}
	
	// calculates the coordinates of a source point for one line
	public void calculateSourceForLine(double d1, double ratio_) {
		//System.out.println(ratio_);
		int p1x = this.getStartX();
		int p1y = this.getStartY();
		int q1x = this.getEndX();
		int q1y = this.getEndY(); 
		int vecPQ1x = q1x - p1x;
		int vecPQ1y = q1y - p1y;
		int vecNormalPQ1x = -1*vecPQ1y;
		int vecNormalPQ1y = vecPQ1x;
		// calculation of the source coordinate = start point of line + vector of the line*ratio + 
		// distance from the destination point to the line * the normalized normal vector
		xline = p1x - vecPQ1x*ratio_ - d1*vecNormalPQ1x/(Math.sqrt
					(vecNormalPQ1x*vecNormalPQ1x + vecNormalPQ1y*vecNormalPQ1y));
		/*if (d1 == 130) {
			System.out.println ("ratio: " + this.getRatio());
			System.out.println (p1x - vecPQ1x*ratio);
		}*/
		yline = p1y - vecPQ1y*ratio_ - d1*vecNormalPQ1y/(Math.sqrt
					(vecNormalPQ1x*vecNormalPQ1x + vecNormalPQ1y*vecNormalPQ1y));
	}
	
	// getters for the source point coordinates for this line
	public double getXLine() {
		return xline;
	}
	
	public double getYLine() {
		return yline;
	}
}