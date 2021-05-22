package scientigrapher;

import java.util.Arrays;
import java.util.List;

public class StringProcessing {
	
	public static String purgeAllPunctuation(String s)
	{
		s = s.replaceAll("\\.", " ").replaceAll("’", " ").replaceAll("\\?", " ").replaceAll("«", " ").replaceAll("»"," ").
				replaceAll("!"," ").
				replaceAll("…"," ");
        
		return s;
	}

	public static List<String> toListOfWords(String text) {
		text = clearOfSymbols(text);
		text = text.toLowerCase();
		while(text.startsWith(" "))text = text.substring(1);
		while(text.contains("  "))text = text.replaceAll("  ", " ");
		
		return Arrays.asList(text.split(" "));
	}
	
	public static String clearOfSymbols(String string) {
		
		string = string.replaceAll("[^a-zA-Z]", " ");
		/*char c = (char)160;
		string = string.replaceAll(c+"", "");
		string = string.replaceAll(",", " ");
		string = string.replaceAll("%", " ");
		string = string.replaceAll("/", " ");
		string = string.replaceAll("-", " ");
		string = string.replaceAll("^", " ");
		string = string.replaceAll("”", " ");
		string = string.replaceAll("\\.", " ");
		string = string.replaceAll("\"", " ");
		string = string.replaceAll(";", " ");
		string = string.replaceAll("\\(", " ");
		string = string.replaceAll("\\)", " ");
		string = string.replaceAll("\\]", " ");
		string = string.replaceAll("\\[", " ");
		string = string.replaceAll("!", " ");
		string = string.replaceAll("\\|", " ");
		string = string.replaceAll(":", " ");
		string = string.replaceAll("\\?", " ");
		string = string.toLowerCase();
		string = string.replaceAll("\n", " ");
		string = string.replaceAll("–", " ");
		string = string.replaceAll("[0-9]", "");
		string = string.replaceAll("“", " ");
		string = string.replaceAll("•", " ");
		string = string.replaceAll("∈", " ");
		string = string.replaceAll("=", " ");
		string = string.replaceAll("α", " ");
		string = string.replaceAll("∧", " ");
		string = string.replaceAll("φ", " ");
		string = string.replaceAll("&", " ");
		string = string.replaceAll("→", " ");
		string = string.replaceAll("}", " ");
		string = string.replaceAll("\\+", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("≤", " ");
		string = string.replaceAll("δ", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");*/
		
		while(string.contains("  "))
			string = string.replaceAll("  ", " ");

		return string;
	}
	
}
