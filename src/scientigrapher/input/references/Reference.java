package scientigrapher.input.references;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import scientigrapher.datagathering.GoogleScholarGatherer;
import scientigrapher.datagathering.PdfLinksGatherer;

public class Reference {
	
	private static final String YEAR_KEY = "year";
	private static final String AUTHOR_KEY = "author";
	private static final String JOURNAL_KEY = "journal";
	private static final String VOLUME_KEY = "volume";
	private static final String TITLE_KEY = "title";
	private static final String PAGES_KEY = "pages";
	private static final String DOI_KEY = "doi";
	private static final String DOCUMENTTYPE_KEY = "documenttype";
	private static final Object URL_KEY = "url";
	private static final Object BOOK_TITLE_KEY = "booktitle";
	private static final Object PUBLISHER_KEY = "publisher";
	
	private final Map<String,String> input;
	private final int id;

	public Reference(Map<String,String> input, int id) {
		this.input = input;
		this.id = id;
		
	}

	public boolean hasDoi() {
		return input.containsKey(DOI_KEY);
	}

	public String getDoi() {
		return input.get(DOI_KEY);
	}

	public String getTitle() {
		return input.get(TITLE_KEY);
	}

	private static final Map<Integer, Map<String,String>> mappedInput = new HashMap<>();
	public static Reference newInstance(Map<String, String> input, int id) {
		if(mappedInput.containsKey(id) && !mappedInput.get(id).equals(input))
			throw new Error();
		
		return new Reference(input,id);
	}

	public static Reference parse(String rawTextBibtexEntry, int id)
	{
		String aUmlaut = "\\{\\\\\"\\{a\\}\\}";
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll(aUmlaut, "ä");
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll("\\{\\\\aa\\}", "å");
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll("\\\\'\\{e\\}", "é");
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll("\\{\\\\'e\\}", "é");
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll("\\{\\\\`e\\}", "è");
		rawTextBibtexEntry = rawTextBibtexEntry.replaceAll("\\{\\\\\"\\\\i\\}", "ï");
		rawTextBibtexEntry = rawTextBibtexEntry.trim();
		
		
		String type = rawTextBibtexEntry.substring(0,rawTextBibtexEntry.indexOf("{"));
		rawTextBibtexEntry = rawTextBibtexEntry.substring(type.length()+1);
		
		String citationkey = rawTextBibtexEntry.substring(0,rawTextBibtexEntry.indexOf(","));
		rawTextBibtexEntry = rawTextBibtexEntry.substring(citationkey.length()+1);
		
		if(rawTextBibtexEntry.charAt(rawTextBibtexEntry.length()-1)!='}')
			throw new Error();
		rawTextBibtexEntry=rawTextBibtexEntry.substring(0,rawTextBibtexEntry.length()-1).trim();
		
		
		Map<String, String> allEntries = new HashMap<>();
		allEntries.put("documenttype", type);
		allEntries.put("citationkey", citationkey);
		
		while(rawTextBibtexEntry.contains("="))
		{
			String left = rawTextBibtexEntry.substring(0,rawTextBibtexEntry.indexOf("=")).trim();
			if(left.startsWith(","))
				left = left.substring(1).trim();
			rawTextBibtexEntry = rawTextBibtexEntry.substring(rawTextBibtexEntry.indexOf("{")+1);
			int countOpen = 1;
			
			String right = "";
			while(countOpen>0)
			{
				if(rawTextBibtexEntry.startsWith("{"))
					countOpen++;
				else if(rawTextBibtexEntry.startsWith("}"))
					countOpen--;
				else right+=rawTextBibtexEntry.charAt(0);
				rawTextBibtexEntry = rawTextBibtexEntry.substring(1);
			}
			
			if(allEntries.containsKey(left))
				throw new Error();
			
			if(left.equals("doi"))
				right = right.replaceAll("<","%3C").replaceAll(">","%3E");
			
			allEntries.put(left, right);
		}
		
		
	
		return Reference.newInstance(allEntries,id);
	}

	public int getId() {
		return id;
	}

	public String toString()
	{
		return id+" "+getTitle();
	}

	public static Set<Reference> referencesFromBibFile(File fileName){
		String entries;
		try {
			entries = Files.readString(fileName.toPath());

			AtomicInteger currentRefNumber = new AtomicInteger();
			
			if(!entries.startsWith("@"))
				entries = entries.substring(entries.indexOf("@")+1);
			
			List<String> allStrings = Arrays.asList(entries.split("\n@"));
			return 
					allStrings
					.stream()
					.filter(x->
					!x.isEmpty()//&&
					//x.contains("@")
							)
					.map(
							x-> (Reference)
							Reference.parse(x, 
									currentRefNumber.incrementAndGet())
							)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	public int getYear() {
		return Integer.parseInt(this.input.get(YEAR_KEY));
	}
	
	public boolean equals(Object o) { return ((Reference)o).id==id && ((Reference)o).getTitle().equals(getTitle());}
	public int hashCode() {return id;}
	
	public static String toParsableString(Reference r, Set<String>forbiddenSubstrings)
	{
		for(String s:forbiddenSubstrings)
			if(r.input.toString().contains(s))
				throw new Error();
		String res = "";
		for(String s: r.input.keySet())
			res+="||"+s+"="+r.input.get(s);
		
		return r.id+res;
	}

	public static Reference fromParsableString(String s) {
		String[] split = s.split("\\|\\|");
		//return new Reference(split[2], split[3], Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		int id = Integer.parseInt(split[0]);
		Map<String, String> input = new HashMap<>();
		for(String part:Arrays.asList(split).subList(1, split.length))
		{
			String left = part.substring(0, part.indexOf("="));
			String right = part.substring(part.indexOf("=")+1);
			input.put(left, right);
		}
		return newInstance(input, id);
	}

	public String getAuthors() {
		return this.input.get(AUTHOR_KEY);
	}

	public String getJournal() {
		return this.input.get(JOURNAL_KEY);
	}

	public String getVolume() {
		return this.input.get(VOLUME_KEY);
	}

	public String getPages() {
		if(!hasPages()) throw new Error();
		return this.input.get(PAGES_KEY);
	}

	public int getNumberOfCitationsFromGoogleScholar() {
		return GoogleScholarGatherer.getNumberOfCitationsFor(this);
	}

	public String getISI() {
		String journal = getJournal();
		switch(journal) {
		case "Journal of Artificial Societies and Social Simulation": return "2.55";
		case "Minds and Machines": return "3.404";
		case "Journal of Artificial Intelligence Research": return "2.441";
		default:throw new Error();
		}
	}

	public int getLevelInNorwegianRegister() {
		String journal = getJournal();
		if(journal.startsWith("Lecture Notes in Computer Science"))
			throw new Error();
		switch(journal) {
		case "Journal of Artificial Societies and Social Simulation": return 1;
		case "Minds and Machines": return 1;
		case "Journal of Artificial Intelligence Research": return 2;
		}
		throw new Error();
	}

	public boolean isJournal() {
		return getDocumentType().toLowerCase().equals("article")&&
				input.containsKey(JOURNAL_KEY);
	}

	public boolean hasPages() {
		return this.input.containsKey(PAGES_KEY);
	}

	private URL linkToPaper = null; 
	public URL getBestLinkToPaper() {
		if(linkToPaper!=null)
			return linkToPaper;
		linkToPaper = PdfLinksGatherer.getOneWorkingLinkToAPdfFor(this);
		if(linkToPaper==null) 
			linkToPaper= PdfLinksGatherer.getANonWorkingLinkToAPdfFor(this);
		return linkToPaper;
	}

	public boolean hasUrl() {
		return this.input.containsKey(URL_KEY);
	}

	public URL getUrl() {
		try {
			return new URL(this.input.get(URL_KEY));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error();
		}
	}

	public boolean isBookChapter() {
		return getDocumentType().toLowerCase().equals("bookchapter");
	}

	private String getDocumentType() {
		return input.get(DOCUMENTTYPE_KEY);
	}

	public String getBookTitle() {
		return input.get(BOOK_TITLE_KEY);
	}

	public String getPublisher() {
		return input.get(PUBLISHER_KEY);
	}

	public boolean isConferencePaper() {
		return getDocumentType().toLowerCase().equals("conference");
	}

	public String getGenericVenue() {
		if(input.containsKey(JOURNAL_KEY))return input.get(JOURNAL_KEY);
		if(input.containsKey(BOOK_TITLE_KEY))return input.get(BOOK_TITLE_KEY);
		throw new Error();
	}

	public String getCoreRanking() {
		String venue = getGenericVenue();
		switch(venue)
		{
		case "IEEE International Conference on Intelligent Robots and Systems":return "A";
		case "AAAI Conference on Artificial Intelligence":return "A*";
		case "Conference on Human Factors in Computing Systems":return "A*";
		case "Springer Proceedings in Complexity":return "N/A";
		case "International Joint Conference on Autonomous Agents and Multiagent Systems":return "A*";
		case "IEEE International Smart Cities Conference":return "N/A";
		case "International Workshop on Multi-Agent-Based Simulation":return "N/A";
		case "Journées Francophones sur la Planification, la Décision et l'Apprentissage pour la conduite de systèmes": return "N/A";
		}
		throw new Error();
	}

	public String getEraRanking() {
		String venue = getGenericVenue();
		
		switch(venue)
		{
		case "IEEE International Conference on Intelligent Robots and Systems":return "A";
		case "Springer Proceedings in Complexity":return "N/A";
		case "AAAI Conference on Artificial Intelligence":return "A";
		case "Conference on Human Factors in Computing Systems":return "A";
		case "International Joint Conference on Autonomous Agents and Multiagent Systems":return "A";
		case "IEEE International Smart Cities Conference":return "N/A";
		case "International Workshop on Multi-Agent-Based Simulation":return "N/A";
		case "Journées Francophones sur la Planification, la Décision et l'Apprentissage pour la conduite de systèmes": return "N/A";
		}
		throw new Error();
		
	}

	public String getQualisRanking() {
		String venue = getGenericVenue();
		switch(venue)
		{
		case "IEEE International Conference on Intelligent Robots and Systems":return "A1";
		case "AAAI Conference on Artificial Intelligence":return "A1";
		case "Springer Proceedings in Complexity":return "N/A";
		case "Conference on Human Factors in Computing Systems":return "A1";
		case "International Joint Conference on Autonomous Agents and Multiagent Systems":return "A1";
		case "IEEE International Smart Cities Conference":return "N/A";
		case "International Workshop on Multi-Agent-Based Simulation":return "B3";
		case "Journées Francophones sur la Planification, la Décision et l'Apprentissage pour la conduite de systèmes": return "N/A";
		}
		throw new Error();
	}

}
