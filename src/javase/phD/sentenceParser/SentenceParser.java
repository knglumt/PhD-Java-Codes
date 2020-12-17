package javase.phD.sentenceParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * generate penntreebank.txt file from *.test or *.train files
 * Umit	Kanoglu	8760.test-8839.test complated in DropBox
 */

public class SentenceParser {

	public static void main(String[] args) throws IOException {

		int j = 1000; // file name min value
		int max = 1001; // file name max value
		String ext = ".test"; // file extension

		String phrase;
		BufferedReader reader;

		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;

		fw = new FileWriter("penntreebank2.txt", true); // output file

		bw = new BufferedWriter(fw);
		pw = new PrintWriter(bw);
		pw.println("<DOC>	<DOC>+BDTag");

		while (j <= max) {

			reader = new BufferedReader(new FileReader("files/" + j + ext));
			phrase = reader.readLine();

			String delims = "[ ]+";
			String[] tokens = phrase.split(delims);

			pw.println("<S>");
			for (int i = 0; i < tokens.length; i++) {
				
				if (tokens[i].indexOf("morphologicalAnalysis") < 0) 
					continue;
				
				tokens[i] = tokens[i].replace("{turkish=", "");
				tokens[i] = tokens[i].replace("{morphologicalAnalysis=", "'\u0009'");
				tokens[i] = tokens[i].replace("}", "");
				tokens[i] = tokens[i].replace("'", "");
				tokens[i] = tokens[i].substring(0, tokens[i].indexOf("{metaMorphemes"));
				pw.println(tokens[i]);
			}

			pw.println("<\\S>");

			reader.close();

			j++;
		}

		pw.println("</DOC>	</DOC>+EDTag");

		pw.close();
		bw.close();
		fw.close();
		
		System.out.println("Completed!");
	}

}
