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
import java.util.function.Function;
import java.util.stream.Collectors;

import scientigrapher.datagathering.GoogleScholarGatherer;
import scientigrapher.datagathering.PdfLinksGatherer;
import scientigrapher.datagathering.ScopusSearcher;

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
	private static final String SCOPUS_ID = "scopus_id";
	private static final String LOCAL_ID = "local_id";
	
	private final Map<String,String> input;
	public Reference(Map<String,String> input) {
		this.input = input.keySet().stream().collect(Collectors.toMap(x->x, x->input.get(x).replaceAll("\\|\\|","")));
		
		if(!hasId()||getScopusId().equals("NO_CITATIONS_FOUND"))
			throw new Error();
		
		if(isFoundOnScopus())
		{
			if(!input.containsKey("asjc_classifications"))
				throw new Error();
			if(!input.containsKey("cited_reference_ids"))
				throw new Error();
		}
	}

	private boolean hasId() {
		if(hasScopusId())
			return true;
		if(hasLocalId())
			return true;
		throw new Error("No ID for:"+this.getTitle());
	}

	private boolean hasLocalId() {
		return this.input.containsKey(LOCAL_ID);
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
	private static final String AUTHOR_IDS_KEY = "author_ids";
	public static final String CITED_REF_ID_KEY = "cited_reference_ids";

	public static Reference newInstance(Map<String, String> input) {
		if(mappedInput.containsKey(input.get(SCOPUS_ID)) && !mappedInput.get(input.get(SCOPUS_ID)).equals(input))
			throw new Error();
		
		if(input.containsValue(null))
			throw new Error();
		
		if(input.containsKey("dc:title")){
			if(input.containsKey("title")) {
				if(!input.get("title").equals("dc:title")) throw new Error();}
			input.put("title", input.get("dc:title"));
			input.remove("dc:title");
		}
		
		
		return new Reference(input);
	}

	public static Reference parse(String rawTextBibtexEntry)
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
		
		
	
		return Reference.newInstance(allEntries);
	}

	public String toString()
	{
		return getId()+" "+getTitle();
	}

	private String getId() {
		if(hasScopusId())return getScopusId();
		if(hasLocalId()) return getUniqueLocalId();
		throw new Error();
	}

	private String getScopusId() {
		return input.get(SCOPUS_ID);
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
							Reference.parse(x)
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

	public boolean equals(Object o) {
		Reference other = (Reference)o;
		if(other.input.keySet().size()!=input.keySet().size()) 
			throw new Error();
		for(String s:other.input.keySet())
			if(!input.get(s).equals(other.input.get(s)))
				throw new Error();
		return ((Reference)o).input.equals(input);
	}
	public int hashCode() {
		if(hasScopusId())
			return (int)Long.parseLong(getScopusId()); 
		return input.hashCode();}
	
	private boolean hasScopusId() {
		return input.containsKey(SCOPUS_ID);
	}

	public static String toParsableString(Reference r, Set<String>forbiddenSubstrings)
	{
		for(String s:forbiddenSubstrings)
			if(r.input.toString().contains(s))
				throw new Error();
		String res = "";
		for(String s: r.input.keySet())
			res+="||"+s+"="+r.input.get(s);
		
		String end = res;
		
		Reference backFromParsable = fromParsableString(end);
		if(!backFromParsable.equals(r))
			throw new Error();
		
		return end;
	}

	public static Reference fromParsableString(String s) {
		if(s.isBlank())
			throw new Error();
		String[] split = s.split("\\|\\|");
		Map<String, String> input = new HashMap<>();
		for(String part:Arrays.asList(split).subList(1, split.length))
		{
			if(!part.contains("="))
				throw new Error();
			String left = part.substring(0, part.indexOf("="));
			String right = part.substring(part.indexOf("=")+1);
			input.put(left, right);
		}
		return newInstance(input);
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
		case "Advances in Intelligent Systems and Computing":
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

	public String getUniqueLocalId() {
		return input.get(LOCAL_ID);		
	}

	public String getUniqueId() {
		if(hasScopusId())
			return getScopusId();
		if(hasLocalId())
			return getUniqueLocalId();
		else throw new Error();
	}

	public boolean hasPublisher() {
		return input.containsKey(PUBLISHER_KEY);
	}

	public Set<String> getAuthorScopusIdList() {
		if(hasScopusId())
		{
			Set<String> res = Arrays.asList(input.get(AUTHOR_IDS_KEY).split(",")).stream().collect(Collectors.toSet()); 
			return res;
		}
		throw new Error();
	}

	public List<String> getReferencesIdsCitedByThePaper() {
		if(input.get(CITED_REF_ID_KEY).equals("NO_CITATIONS_FOUND"))
			return new ArrayList<>();
		return Arrays.asList(input.get(CITED_REF_ID_KEY).split(",")); 
	}

	public List<String> getAsjcCodes() 
	{
		
		if(!input.keySet().contains("asjc_classifications"))
			{
			if(!isFoundOnScopus())
			return new ArrayList<>();
			}
		if(input.get("asjc_classifications").equals("NO_CLASSIFICATION_FOUND_ON_SCOPUS"))
			return new ArrayList<>();
		return Arrays.asList(input.get("asjc_classifications").split(","));
	}

	private boolean isFoundOnScopus() {
		if(input.containsKey("found_on_scopus"))
			return Boolean.parseBoolean(input.get("found_on_scopus"));
		return true;
	}

	public List<Reference> getReferencesCitedByThePaper() {
		return getReferencesIdsCitedByThePaper()
				.stream().map(x->ScopusSearcher.getReferenceFromPaperId(x))
				.collect(Collectors.toList());
	}

}
