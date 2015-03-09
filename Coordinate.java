import java.io.Serializable;

/**
 * Class Coordinate is a model class used to store the coordinates of a peer
 * 
 * @author vaibhavgandhi
 *
 */
public class Coordinate implements Serializable {

	/**
	 * Shared variables
	 */
	double lowerX, lowerY, upperX, upperY, centerX, centerY;
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param lowerX
	 * @param lowerY
	 * @param upperX
	 * @param upperY
	 * @param centerX
	 * @param centerY
	 */
	public Coordinate(double lowerX, double lowerY, double upperX,
			double upperY, double centerX, double centerY) {
		this.lowerX = lowerX;
		this.lowerY = lowerY;
		this.upperX = upperX;
		this.upperY = upperY;
		this.centerX = centerX;
		this.centerY = centerY;
	}

	/**
	 * Constructor without center coordinates
	 * 
	 * @param lowerX
	 * @param lowerY
	 * @param upperX
	 * @param upperY
	 */
	public Coordinate(double lowerX, double lowerY, double upperX, double upperY) {
		this.lowerX = lowerX;
		this.lowerY = lowerY;
		this.upperX = upperX;
		this.upperY = upperY;
		centerX = (lowerX + upperX) / 2;
		centerY = (lowerY + upperY) / 2;
	}

	@Override
	public String toString() {
		return ("\nlowerX:" + lowerX + "\tupperX: " + upperX + "\nlowerY: "
				+ lowerY + "\tupperY: " + upperY + "\n");
	}

	/**
	 * contains checks if the point lies in the current coordinate system
	 * 
	 * @param x
	 * @param y
	 *            x and y are points that need to be checked
	 * @return
	 */
	public boolean contains(double x, double y) {
		if (lowerX <= x && upperX >= x && lowerY <= y && upperY >= y)
			return true;
		return false;
	}

	/**
	 * distance calculates the distance of point to the center of the peer
	 * 
	 * @param x
	 * @param y
	 *            x, y is point that has to be checked
	 * @return
	 */
	public double distance(double x, double y) {
		return Math.sqrt(Math.pow(centerY - y, 2) + Math.pow(centerX - x, 2));
	}

	/**
	 * splitVertically checks if the node has to be split peer vertically
	 * 
	 * @return true if answer is yes
	 */
	public boolean splitVertically() {
		if (upperX - lowerX >= upperY - lowerY)
			return true;
		return false;
	}

	/**
	 * splits the coordinates vertically
	 */
	public void performSplitVertically() {
		upperX = (lowerX + upperX) / 2;
		centerX = (lowerX + upperX) / 2;
		centerY = (upperY + lowerY) / 2;
	}

	/**
	 * splits the coordinates horizontally
	 */
	public void performSplitHorizontally() {
		upperY = (lowerY + upperY) / 2;
		centerY = (lowerY + upperY) / 2;
		centerX = (lowerX + upperX) / 2;
	}

	/**
	 * checks if given node is a subset in X
	 * 
	 * @return true if x is a subset
	 */
	public boolean isSubsetX(Coordinate c) {
		if ((c.lowerX >= lowerX && c.upperX <= upperX)
				|| (c.lowerX <= lowerX && c.upperX >= upperX))
			return true;
		return false;
	}

	/**
	 * checks if given node is a subset in Y
	 * 
	 * @return true if y is a subset
	 */
	public boolean isSubsetY(Coordinate c) {
		if ((c.lowerY >= lowerY && c.upperY <= upperY)
				|| (c.lowerY <= lowerY && c.upperY >= upperY))
			return true;
		return false;
	}

	/**
	 * checks if given node is a adjacent in X
	 * 
	 * @param c
	 * @return
	 */
	public boolean isAdjacentX(Coordinate c) {
		if (c.lowerX == upperX || c.upperX == lowerX)
			return true;
		return false;
	}

	/**
	 * checks if given node is a adjacent in Y
	 * 
	 * @param c
	 * @return
	 */
	public boolean isAdjacentY(Coordinate c) {
		if (c.lowerY == upperY || c.upperY == lowerY)
			return true;
		return false;
	}

	/**
	 * checks if given coordinate is of same size as current
	 * 
	 * @param c
	 * @return
	 */
	public boolean isSameSize(Coordinate c) {
		if (((upperX - lowerX) == (c.upperX - c.lowerX))
				&& ((upperY - lowerY) == (c.upperY - c.lowerY)))
			return true;
		return false;
	}

	/**
	 * updates the current coordinates with the given coordinates
	 * 
	 * @param c
	 * @return
	 */
	public Coordinate updateCoordinate(Coordinate c) {
		if (c.lowerX >= upperX) {
			c.lowerX = lowerX;
		} else if (c.upperX <= lowerX) {
			c.upperX = upperX;
		}

		if (c.lowerY >= upperY) {
			c.lowerY = lowerY;
		} else if (c.upperY <= lowerY) {
			c.upperY = upperY;
		}
		return c;
	}
}
