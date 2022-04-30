package scientigrapher.displays.wordcloud;

import textprocessing.BagOfWords;

public class WordcloudGenerator {

	public static String getCsvFileFrom(BagOfWords bw) {
		StringBuilder res = new StringBuilder("\"weight\";\"word\";\"color\";\"url\"\n");
		for(String s : bw.getWordsSortedByDecreasingNumberOfOccurrences())
		{
				res.append("\""+bw.getNbOccurrences(s)+"\";\""+s+"\";\"\";\"\"\n");
		}
		
		return res.toString();
	}

}
