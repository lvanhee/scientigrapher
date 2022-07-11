package scientigrapher.input.restapi;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RestClient {
	
	private String ApiKey;
    private static String search_uri = "http://api.elsevier.com/content/search/index:SCOPUS";
    private String view = "complete";
    private int RESULTS_COUNT = 10;
    
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpGet httpget = new HttpGet();
    private CloseableHttpResponse response;
    private HttpEntity httpEntity = new BasicHttpEntity();
    
    
	public RestClient(String apiKey) {
		super();
		ApiKey = apiKey;
	}


	public SearchResult search(String query) {
		
		
			SearchResult sr;
			URI theURI = null; String res;
			
			try {
				sr = new SearchResult("");
				return sr;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new Error();
			}
						
	       
			/*
			//String theString= "query=doi("+URLEncoder.encode( doi , "UTF8" )+")"+ "&count="+ this.RESULTS_COUNT+"&view=" + this.view + "&" + this.ApiKey;
	 
	        try {
	             theURI = new URI(this.search_uri + "?" + theString);
	//System.out.println(theURI.toString());
	        } catch (URISyntaxException ex) {
	            Logger.getLogger(RESTclient.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        
	        try {
	            this.httpget.setURI(theURI);
	            this.response = httpclient.execute(this.httpget);
	        } catch (IOException ex) {
	            Logger.getLogger(RESTclient.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        //res = entityToString(this.response.getEntity());
	        this.response.close();
	        */
	        
	        //return sr;
	}


	public static void main(String[] args) throws IOException, URISyntaxException, ParseException
	{
		//https://api.elsevier.com/content/search/scopus?query=AF-ID(60031040)&count=100&start=0&APIKey=9f577f44be144470bcbd7ecd5176f8e0
		//https://api.elsevier.com/content/search/scopus?query=AU-ID(7004212771)&field=dc:identifier&count=100&&APIKey=9f577f44be144470bcbd7ecd5176f8e0
		String theString= "query=AF-ID(60031040)&"
				+ "7f44be144470bcbd7ecd5176f8e0";
		CloseableHttpClient httpclient = //HttpClients.createDefault();
				HttpClients.custom().disableContentCompression().build();
		HttpGet httpget = new HttpGet();
		URI theURI = null;
		CloseableHttpResponse response=null;
		try {
			theURI = new URI(search_uri + "?" + theString);
			//System.out.println(theURI.toString());
		} catch (URISyntaxException ex) {
			throw new Error();
		}


		try {
			httpget.setURI(new URI("https://api.elsevier.com/content/search/scopus?query=AU-ID(7004212771)&field=dc:identifier&count=100&&APIKey=9f577f44be144470bcbd7ecd5176f8e0"));
			response = httpclient.execute(httpget);
		} catch (IOException ex) {
			throw new Error();
		}
		System.out.println(response.getEntity());
		String responseStr = EntityUtils.toString(response.getEntity());
		response.close();
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(responseStr);

		JSONObject responseJson = (JSONObject) jsonObject.get("search-results");
		JSONArray entries = (JSONArray) responseJson.get("entry");
		
		
		throw new Error();

	}

    
    
    
}
