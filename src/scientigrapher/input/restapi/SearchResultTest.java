package scientigrapher.input.restapi;


import org.json.simple.parser.ParseException;

public class SearchResultTest {
	
	public static void main(String[] args)
	{
	
	String s="{\"search-results\":{\"opensearch:totalResults\":\"1\",\"opensearch:startIndex\":\"0\",\"opensearch:itemsPerPage\":\"1\","
			+ "\"opensearch:Query\":"
			+ "{\"@role\":"
				+ "\"request\", "
				+ "\"@searchTerms\": "
			+ "\"title(&amp;quot;A Prototype Macroeconomic Model of Foreign Direct Investment&amp;quot;)\", \"@startPage\": \"0\"},\"link\": [{\"@_fa\": \"true\", \"@ref\": \"self\", \"@href\": \"http://api.elsevier.com/content/search/scopus?start=0&count=10&query=title%28%22A+Prototype+Macroeconomic+Model+of+Foreign+Direct+Investment%22%29&view=complete&apiKey=7af7f7c604683071160a82016d42789f\", \"@type\": \"application/json\"},{\"@_fa\": \"true\", \"@ref\": \"first\", \"@href\": \"http://api.elsevier.com/content/search/scopus?start=0&count=10&query=title%28%22A+Prototype+Macroeconomic+Model+of+Foreign+Direct+Investment%22%29&view=complete&apiKey=7af7f7c604683071160a82016d42789f\", \"@type\": \"application/json\"}],\"entry\": [{\"@_fa\": \"true\", \"link\": [{\"@_fa\": \"true\", \"@ref\": \"self\", \"@href\": \"http://api.elsevier.com/content/abstract/scopus_id/0028581448\"},{\"@_fa\": \"true\", \"@ref\": \"author-affiliation\", \"@href\": \"http://api.elsevier.com/content/abstract/scopus_id/0028581448?field=author,affiliation\"},{\"@_fa\": \"true\", \"@ref\": \"scopus\", \"@href\": \"https://www.scopus.com/inward/record.uri?partnerID=HzOxMe3b&scp=0028581448&origin=inward\"},{\"@_fa\": \"true\", \"@ref\": \"scopus-citedby\", \"@href\": \"https://www.scopus.com/inward/citedby.uri?partnerID=HzOxMe3b&scp=0028581448&origin=inward\"},{\"@_fa\": \"true\", \"@ref\": \"full-text\", \"@href\": \"http://api.elsevier.com/content/article/eid/1-s2.0-0304387894900094\"}],\"prism:url\":\"http://api.elsevier.com/content/abstract/scopus_id/0028581448\",\"dc:identifier\":\"SCOPUS_ID:0028581448\",\"eid\":\"2-s2.0-0028581448\",\"dc:title\":\"A prototype macroeconomic model of foreign direct investment\",\"dc:creator\":\"Malley J.\",\"prism:publicationName\":\"Journal of Development Economics\",\"prism:issn\":\"03043878\",\"prism:volume\":\"43\",\"prism:issueIdentifier\":\"2\",\"prism:pageRange\":\"295-315\",\"prism:coverDate\":\"1994-01-01\",\"prism:coverDisplayDate\":\"April 1994\",\"prism:doi\":\"10.1016/0304-3878(94)90009-4\",\"pii\":\"0304-3878(94)90009-4\",\"dc:description\":\"This paper constructs a prototype macroeconomic model of a small open economy which is a recipient of foreign direct investment. Foreign firms invest in the domestic economy in order to take advantage of lower wage costs. The rate at which such investment takes place is determined by the time that elapses between development of new products in the rest of the world and the acquisition of knowledge by the domestic labour force for producing those products. The model is used to investigate the effects of policy measures and of changes in the rates of innovation and technology transfer on the main macroeconomic variables. © 1994.\",\"citedby-count\":\"1\",\"affiliation\": [{\"@_fa\": \"true\", \"affiliation-url\":\"http://api.elsevier.com/content/affiliation/affiliation_id/60025200\",\"afid\":\"60025200\",\"affilname\":\"University of Stirling\",\"affiliation-city\":\"Stirling\",\"affiliation-country\":\"United Kingdom\"}],\"prism:aggregationType\":\"Journal\",\"subtype\":\"ar\",\"subtypeDescription\":\"Article\",\"author-count\":{\"@limit\": \"100\", \"$\" :\"2\"},\"author\": [{\"@_fa\": \"true\", \"@seq\": \"1\", \"author-url\":\"http://api.elsevier.com/content/author/author_id/7005176975\",\"authid\":\"7005176975\",\"authname\":\"Malley J.\",\"surname\":\"Malley\",\"given-name\":\"Jim\",\"initials\":\"J.\",\"afid\": [{\"@_fa\": \"true\", \"$\" :\"60025200\"}]},{\"@_fa\": \"true\", \"@seq\": \"2\", \"author-url\":\"http://api.elsevier.com/content/author/author_id/6603124439\",\"authid\":\"6603124439\",\"authname\":\"Moutos T.\",\"surname\":\"Moutos\",\"given-name\":\"Thomas\",\"initials\":\"T.\",\"afid\": [{\"@_fa\": \"true\", \"$\" :\"60025200\"}]}],\"authkeywords\":\"Foreign direct investment | Multinational corporations | Product innovation | Technology transfer\",\"source-id\":\"14834\"}]}}";
	

		try {
			SearchResult sr = new SearchResult(s);
			System.out.println(sr.getTotalResults());
			System.out.println(sr.getSearchTerms());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}