package scientigrapher.displays.wordcloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import scientigrapher.StringProcessing;

public class WordcloudFilter {

	static String purgeTermsThatAreIrrelevantForWordClouds(String outOfFile) {
		
		outOfFile = outOfFile.toLowerCase();
		
		
		
		for(String s: Arrays.asList(
				"section","Table","part","Report","Study", "thank","within","less","good","either","might",
				"using", "still","also","overall","presented","others","often","ways","back","long","will","org",
				"without","way","much","whole",
				"make","can","and",
				"One", "one","two","three",
				"based","many", "ACM","use", "well",
				"used","the", "of","a","to","in","is","that","for",
				"are","an","this","as","by","with","on",
				"s","we","it","be","not","or","which","from",
				"=", "e","i","p","r","if","t","have","m","at","social","j",
				"such","g","c","has","their","these","b","other","all","each",
				"between","more","its","some","our","d","different","they","where",
				"example","when","about","but","then","may","x","f","there","how","l",
				"should","n","case","only","do","given","no","first","h","w","o","been",
				"however","et","into","al","both","any","so","what","would","was","does",
				"over","same","de","figure","than","because","them","y","through","fig","k",
				"must","most","pp","q","out","non","his","up",
				"0","1","2","3","4","5","6","7","8","9","http","since","ing","tion",
				"us","re","several","under","before","ag","vu","con","while","since","ofagent",
				"onother","every","see","paper","ofth","per","eds","see","he","very","table",
				"itself","even",
				"called","could","id","high","cannot","done","com","press","u","v","those",
				"Journal","thus","one", "new","made","org"))
			outOfFile = outOfFile.replaceAll(" "+s+" ", " ");
		
		for(String s: Arrays.asList("0","1","2","3","4","5","6","7","8","9"))
			outOfFile = outOfFile.replaceAll(s, "");
		
		return outOfFile;
	}

	public static Map<List<String>, Integer> getNGrams(String parsedText, int nb) {
		Map<List<String>, Integer> allNgrams = new HashMap<>();
		
		
		List<String> words = StringProcessing.toListOfWords(parsedText);
		List<String> lastFew = new LinkedList<>();
		for(String s: words)
		{
			lastFew.add(s);
			if(lastFew.size()==nb)
			{
				List<String> copy = lastFew.stream().collect(Collectors.toList());
				if(!allNgrams.containsKey(copy))
					allNgrams.put(copy, 0);
				allNgrams.put(copy, allNgrams.get(copy)+1);
				lastFew.remove(0);				
			}
		}
		return allNgrams;
	}

	public static String getCsvFileFrom(String outOfFile) {
		
		Map<List<String>, Integer> ngrams = getNGrams(outOfFile, 1);
		
		/*Set<Character> allCharacters = new HashSet<>();
		for(int i = 0; i < outOfFile.length();i++)
		{
			allCharacters.add(outOfFile.charAt(i));
		}
		
		for(Character c:allCharacters)
		{
			System.out.println(c.charValue()+" "+(int)c.charValue());
		}*/
		
		
		StringBuilder res = new StringBuilder("\"weight\";\"word\";\"color\";\"url\"\n");
		for(List<String> s : ngrams.keySet())
		{
			if(ngrams.get(s)>2)
				res.append("\""+ngrams.get(s)+"\";\""+s.get(0)+"\";\"\";\"\"\n");
		}
		
		return res.toString();
	}
	

}
