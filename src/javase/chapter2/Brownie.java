package javase.chapter2;


class Cake { //Top Level Superclass
	
	class Flour {} // Nested Class
	
	protected static int cakeQuantity; //Static Field, try private, public, default, without static
	String cakeType; //Field
	
	static { //Static Initializer runs first
		cakeQuantity = 0;
	}
	
	{//Initializer runs second
		cakeType = "Vanilla";
		cakeQuantity = 1;
	}
	
	Cake(){ //Default Constructor runs third 
		
	}
	
	Cake(String type, int quantity) { //Constructor runs third 
		cakeType = type;
		cakeQuantity = quantity;
	}
	
	protected Integer bringCoffee(int number) { //method
		return number;
	}
}

public final class Brownie extends Cake { //Top Level Subclass
	
	Brownie(){
		super("Chocolate", 1);
	}
	
	public static void main(String[] args) { // Method
		Brownie brownie = new Brownie();
		brownie.bringCoffee(1);
		System.out.println(Cake.cakeQuantity + " " + brownie.cakeType  + " Brownie(s) was made!");
	}
}