package javase.chapter2;

class Cake { //Top Level Class
	class Flour {} // Nested Class
	protected static int cakeQuantity = 1; //Field, try private, public, default, without static
}

public final class Brownie extends Cake { //Top Level Class
	public static void main(String[] args) { // Method
		System.out.println(Cake.cakeQuantity + " Brownie(s) was made!");
	}
}