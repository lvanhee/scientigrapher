package scientigrapher.displays.wordcloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import textprocessing.BagOfWords;
import textprocessing.TextProcessingUtils;

public class ScientificWordFilter {
	
	public static Set<String> IRRELEVANT_WORDS = 
			Arrays.asList(
					"section","Table","part","Report","Study", "thank","within","less","good","either","might",
					"using", "still","also","overall","presented","others","often","ways","back","long","will","org",
					"without","way","much","whole",
					"make","can","and","had",
					"One", "one","two","three","seen",
					"based","many", "ACM","use", "well",
					"used","the", "of","a","to","in","is","that","for",
					"are","an","this","as","by","with","on","van",
					"s","we","it","be","not","or","which","from",
					"=", "e","i","p","r","if","t","have","m","at","social","j",
					"such","g","c","has","their","these","b","other","all","each",
					"between","more","its","some","our","d","different","they","where",
					"example","when","about","but","then","may","x","f","there","how","l",
					"should","n","case","only","do","given","no","first","h","w","o","been",
					"however","et","into","al","both","any","so","what","would","was","does",
					"over","same","de","figure","than","because","them","y","through","fig","k",
					"must","most","pp","q","out","non","his","up","list","call","now",
					"vol","ma","added","come","seem","basi","book","seem","pre","once","th","ch",
					"her","singh","certain","take","oi","ri","ac","hence","let","iff","sub",
					"0","1","2","3","4","5","6","7","8","9","http","since","ing","tion",
					"next","who","give","view","end","john","takes","www","why","etc","too",
					"us","re","several","under","before","ag","vu","con","while","since","ofagent",
					"onother","every","see","paper","ofth","per","eds","see","he","very","table",
					"itself","even","in","andsign","da","co","set","le","cm","note","say","doing",
					"three","item","acm","last","due","below","ic","ti","ii","di","li","via",
					"st","doi","want","third","ieee","meyer","gives","just","york","best",
					"show","until","whose","review","verlag","aim","get","were",
					"called","could","id","high","cannot","done","com","press","u","v","those",
					"Journal","thus","one", "new","made","org").stream().collect(Collectors.toSet());

	public static String purgeTermsThatAreNotSpecificToScience(String outOfFile) {
		
		outOfFile = outOfFile.toLowerCase();
		
		
		
		for(String s: IRRELEVANT_WORDS)
			outOfFile = outOfFile.replaceAll(" "+s+" ", " ");
		
		for(String s: Arrays.asList("0","1","2","3","4","5","6","7","8","9"))
			outOfFile = outOfFile.replaceAll(s, "");
		
		return outOfFile;
	}
	
	public static Predicate<String> INTERESTING_SCIENTIFIC_TERM = x->
	{
		if(IRRELEVANT_WORDS.contains(x))return false;
		if(x.length()<=1)return false;
		return true;
	};

	

	public static String getCsvFileFrom(String outOfFile) {
		
		Map<List<String>, Integer> ngrams = TextProcessingUtils.getNGrams(outOfFile, 1);
		
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



	public static String getCsvFileFrom(BagOfWords bw) {
		StringBuilder res = new StringBuilder("\"weight\";\"word\";\"color\";\"url\"\n");
		for(String s : bw.getWordsSortedByDecreasingNumberOfOccurrences())
		{
				res.append("\""+bw.getNbOccurrences(s)+"\";\""+s+"\";\"\";\"\"\n");
		}
		
		return res.toString();
	}
	

}
