
/*
* @(#)JavaSE Training
*
* Copyright (c) 2020 knglumt
* 06000 Ankara, Turkey
* All rights reserved.
*/
package javase.chapter1;
//java.lang package imported automatically 
import static java.lang.System.out;

/*
* Basic java coding class
*/
public class HelloUniverse {
	/*
	 * Main method to start the application
	 */
	public static void main(String[] args) {

		String from = "";

		// get parameter from command line
		if (args.length > 0)
			from = " from " + args[0];

		System.out.println("Hello to The Universe" + from);
		// when import System.out we could write;
		out.println("Hi to The Universe" + from);
	}
}
