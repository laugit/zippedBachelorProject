
public class Coordinate {
	
	private int x;
	private int y;
	
	/**
	 * Create a new parametered instance of this
	 * @param x : the absciss value of this
	 * @param y : the ordinate value of this
	 */
	public Coordinate(int x,int y) {
		this.x= x;
		this.y= y;
	}
	
	
	/**
	 * @return this.x
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return this.y
	 */
	public int getY() {
		return y;
	}
	
}
