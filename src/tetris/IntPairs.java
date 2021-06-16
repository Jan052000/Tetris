package tetris;

public class IntPairs {
	private int xPos, yPos;
	private static int xStart, yStart, width;

	static {
		xStart = 50;
		yStart = -1;
		width = 17;
	}

	public IntPairs(int x, int y) {
		xPos = x;
		yPos = y;
	}

	public double getActualX() {
		return xStart + (xPos * width);
	}

	public double getActualY() {
		return yStart + (yPos * width);
	}
}
