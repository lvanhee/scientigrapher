package scientigrapher.input.restapi;

//from dkremmydas/Scopus-Java-API; thanks to him/her!
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scientigrapher.datagathering.Entry;

public class SearchResult {
	
	private String source;
	
	private int totalResults;
	private String searchTerms;
	private ArrayList<Entry> entries=new ArrayList<>();
	
	public SearchResult(String source) throws ParseException {
		super();
		this.source = source;
		this.parse();
	}
	
	private void parse() throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)((JSONObject) parser.parse(this.source)).get("search-results");
		
		this.totalResults=Integer.parseInt((String)jsonObject.get("opensearch:totalResults"));
		this.searchTerms=Utilities.unescapeCharacters(
				(String)((JSONObject)jsonObject.get("opensearch:Query")).get("@searchTerms")
		);
		
		if(this.totalResults>0) {
			JSONArray jsEntries = (JSONArray)jsonObject.get("entry");
			for(Object o: jsEntries) {
				JSONObject jo = (JSONObject)o;
				this.entries.add(new Entry(jo));
			}
		}
		
		
		
	}
	
	
	public String getSource() {
		return source;
	}
	public int getTotalResults() {
		return totalResults;
	}
	public String getSearchTerms() {
		return searchTerms;
	}
	public ArrayList<Entry> getEntries() {
		return entries;
	}
	
	
	
	
	
	

}