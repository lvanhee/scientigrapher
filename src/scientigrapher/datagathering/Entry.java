package scientigrapher.datagathering;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scientigrapher.model.Author;

public class Entry {
	
	private String source;
	
	private String eid; private String decription; private Date coverDate;
	private String sourceType; private URL apiUrl; private String creator;
	private String keywords; private String volume; private int citedByCount;
	private String sourceSubType;private String title;private String issn;
	private String issue; private String sourceName; private String pageRange;
	private String doi; private String scopusId;
	private ArrayList<String> affiliations=new ArrayList<>();
	private ArrayList<Author> authors=new ArrayList<>();
	

	public Entry(String source) throws ParseException {
		super();
		this.source = source;
		this.parse();
	}
	
	public Entry(JSONObject source) throws ParseException {
		this(source.toString());
	}
	
	


	private void parse() throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(this.source);
		
		//get all fields
		this.eid=(String)jsonObject.get("eid");
		this.decription=(String)jsonObject.get("dc:description");
		try {
			this.coverDate = new SimpleDateFormat("yyyy-MM-dd").parse((String)jsonObject.get("prism:coverDate"));
		} catch (java.text.ParseException e) {
			this.coverDate = null;
		}
		this.sourceType=(String)jsonObject.get("prism:aggregationType");
		try {
			this.apiUrl=new URL((String)jsonObject.get("prism:url"));
		} catch (MalformedURLException e) {
			this.apiUrl=null;
		}
		this.creator=(jsonObject.get("dc:creator")!=null?( (String)jsonObject.get("dc:creator") ):"");
		this.keywords=(jsonObject.get("authkeywords")!=null?( (String)jsonObject.get("authkeywords")):"");
		this.volume=(jsonObject.get("prism:volume")!=null?((String)jsonObject.get("prism:volume")):"");
		this.citedByCount=(jsonObject.get("citedby-count")!=null?Integer.parseInt((String)jsonObject.get("citedby-count")):0);
		this.sourceSubType=(jsonObject.get("subtypeDescription")!=null?( (String)jsonObject.get("subtypeDescription")):"");
		this.title=(jsonObject.get("dc:title")!=null?( (String)jsonObject.get("dc:title")):"");
		this.issn=(jsonObject.get("prism:issn")!=null?( (String)jsonObject.get("prism:issn")):"");
		this.issue=(jsonObject.get("prism:issueIdentifier")!=null?( (String)jsonObject.get("prism:issueIdentifier")):"");
		this.sourceName=(jsonObject.get("prism:publicationName")!=null?( (String)jsonObject.get("prism:publicationName")):"");
		this.pageRange=(jsonObject.get("prism:pageRange")!=null?( (String)jsonObject.get("prism:pageRange")):"");
		this.doi=(jsonObject.get("prism:doi")!=null?( (String)jsonObject.get("prism:doi")):"");
		this.scopusId=(jsonObject.get("dc:identifier")!=null?( (String)jsonObject.get("dc:identifier")):"");
		
		//create  authors
		JSONArray jsAuthors = (JSONArray)jsonObject.get("author");
		if(jsAuthors!=null) {
			for(Object o: jsAuthors) {
				JSONObject jo = (JSONObject)o;
				this.authors.add(new Author(jo));
			}
		}
				
		//get affiliations of the article
		JSONArray affId=(JSONArray)jsonObject.get("affiliation");
		if(affId!=null) {
			for(Object o: affId) {
				JSONObject jo = (JSONObject)o;
				if(jsonObject.get("afid")!=null) this.affiliations.add((String)jo.get("afid"));
			}
		}
	}

	public String getSource() {
		return source;
	}

	public String getEid() {
		return eid;
	}

	public String getDecription() {
		return decription;
	}

	public Date getCoverDate() {
		return coverDate;
	}

	public String getSourceType() {
		return sourceType;
	}

	public URL getApiUrl() {
		return apiUrl;
	}

	public String getCreator() {
		return creator;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getVolume() {
		return volume;
	}

	public int getCitedByCount() {
		return citedByCount;
	}

	public String getSourceSubType() {
		return sourceSubType;
	}

	public String getTitle() {
		return title;
	}

	public String getIssn() {
		return issn;
	}

	public String getIssue() {
		return issue;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getPageRange() {
		return pageRange;
	}

	public String getDoi() {
		return doi;
	}

	public String getScopusId() {
		return scopusId;
	}

	public ArrayList<Author> getAuthors() {
		return authors;
	}
	
	

}