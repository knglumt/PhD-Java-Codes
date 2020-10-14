package javase.chapter2;
//Oracle official example
class Point {
	int x, y;

	private Point() {
		reset();
	}

	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	private void reset() {
		this.x = 0;
		this.y = 0;
	}
}

class ColoredPoint extends Point {
	int color;

	void clear() {
		reset();
	} // error
}

class TestMembers1 {
	public static void main(String[] args) {
		ColoredPoint c = new ColoredPoint(0, 0); // error
		c.reset(); // error
	}
}
