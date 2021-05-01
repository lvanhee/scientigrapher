package scientigrapher;

public class StringProcessing {
	
	public static String purgeAllPunctuation(String s)
	{
		s = s.replaceAll("\\.", " ").replaceAll("’", " ").replaceAll("\\?", " ").replaceAll("«", " ").replaceAll("»"," ").
				replaceAll("!"," ").
				replaceAll("…"," ");
        
		return s;
	}

}
