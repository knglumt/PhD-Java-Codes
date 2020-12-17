package javase.phD.countwordFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/***
 * 
 * @author Umit Kanoglu find each word count in a corpus file duration for
 *         826.680 words is 12 hours
 */
public class CountWordFile {

	static String corpusFile = "files/corpus.txt"; // input file
	static String countFile = "files/wordcounts.txt"; // output file
	static String tempFile1 = "files/myTempFile.txt"; // temp file

	public static void main(String[] args) throws Exception {
		String line;
		int count = 0;

		// Opens a file in read mode
		FileReader file = new FileReader(corpusFile);
		BufferedReader br = new BufferedReader(file);

		// Gets each line till end of file is reached
		while ((line = br.readLine()) != null) {
			// Splits each line into words
			String words[] = line.split(" ");
			// Counts each word
			count = count + words.length;
			// count of the word
			for (int i = 0; i < words.length; i++) {
				findWord(words[i]);
			}
		}

		br.close();

		System.out.println("Complated words: " + count);
	}

	public static void findWord(String word) throws IOException {

		File inputFile = new File(countFile);
		File tempFile = new File(tempFile1);

		BufferedReader reader = null;
		BufferedWriter writer = null;
		PrintWriter pw = null;

		reader = new BufferedReader(new FileReader(inputFile));
		writer = new BufferedWriter(new FileWriter(tempFile));
		pw = new PrintWriter(writer);

		String currentLine;
		String[] words;
		int count = 0;

		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			words = trimmedLine.split(" ");
			if (words[0].trim().equals(word.trim())) {
				count = Integer.parseInt(words[1]);
				pw.println(word + " " + (++count));
				continue;
			}
			pw.println(currentLine);
		}

		if (count == 0) {
			pw.println(word + " " + (++count));
		}

		pw.close();
		writer.close();
		reader.close();
		inputFile.delete();
		tempFile.renameTo(inputFile);
	}
}