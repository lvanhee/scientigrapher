package scientigrapher.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Author {
	
	private String source;	private String firstName;
	private String surname;	private URL authorUrl;
	private String scopusID;
	private ArrayList<String> affiliations=new ArrayList<>();
	
	public Author(String source) throws ParseException {
		super();
		this.source = source;
		this.parse();
	}
	
	public Author(JSONObject source) throws ParseException {
		this(source.toString());
	}

	
	private void parse() throws ParseException {
		/*
		 * private String source;	private String firstName;
	private String surname;	private URL authorUrl;
	private String scopusID;
		 */
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(this.source);
		
		//get all fields
		this.firstName=(jsonObject.get("given-name")!=null?((String)jsonObject.get("given-name")):"");
		this.surname=(jsonObject.get("surname")!=null?((String)jsonObject.get("surname")):"");
		try {
			this.authorUrl=new URL((String)jsonObject.get("author-url"));
		} catch (MalformedURLException e) {
			this.authorUrl=null;
		}
		this.scopusID=(jsonObject.get("authid")!=null?((String)jsonObject.get("authid")):"");
		
		//get afifliation id
		JSONArray affId=(JSONArray)jsonObject.get("afid");
		if(affId!=null) {
			for(Object o: affId) {
				JSONObject jo = (JSONObject)o;
				this.affiliations.add((String)jo.get("$"));
			}
		}
		
	}

	public String getSource() {
		return source;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSurname() {
		return surname;
	}

	public URL getAuthorUrl() {
		return authorUrl;
	}

	public String getScopusID() {
		return scopusID;
	}

	public ArrayList<String> getAffiliations() {
		return affiliations;
	}
	
	

}