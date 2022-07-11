package scientigrapher.datagathering;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.stdDSA;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import cachingutils.Cache;
import cachingutils.advanced.StringCacheUtils;
import cachingutils.impl.PlainObjectFileBasedCache;
import cachingutils.impl.SplittedFileBasedCache;
import cachingutils.impl.TextFileBasedCache;
import scientigrapher.input.references.Reference;
import scientigrapher.model.InstitutionIdentifier;
import scientigrapher.model.PaperIdentifier;

public class ScopusSearcher {

	private static final Set<String> SCOPUS_KEYS = ConcurrentHashMap.newKeySet();

	static {
		SCOPUS_KEYS.addAll(Arrays.asList("571b28033b62e199613ff00c819c4a13",
				"cbc33ae493d3348f605c01884cb56167",
				"9f577f44be144470bcbd7ecd5176f8e0",
				"5823228557368dde5263a47d76bb8b8a",
				"642ef2bffaf38e0e0a15c2b61060ee6b",
				"82f0358903cf316de433c0cad3d00940",
				"96564e6ebd403ae9df63a81cfa093b06",
				"8a49a9f7c6efaf773097f67334f2b014"));
	}




	private static final Cache<String, Reference> cachePaperIdToReference = SplittedFileBasedCache.newInstance(
			x->new File("../databases/scientigrapher/references/"+x+".txt"),
			x->Reference.toParsableString(x, new HashSet<>()),
			Reference::fromParsableString);


	private static final Cache<String, AuthorDescription> cacheAuthorIdToReference = SplittedFileBasedCache.newInstance(
			x->new File("../databases/scientigrapher/authorid_to_info/"+x+".txt"),
			x->AuthorDescription.toParsableString(x),
			AuthorDescription::fromParsableString);


	private final static int NUMBER_ITEMS_RETURNED_PER_REQUEST=25;

	private static CloseableHttpClient httpclient = //HttpClients.createDefault();
			HttpClients.custom().disableContentCompression().build();

	private static final TextFileBasedCache<Integer, List<String>> allReferencesRelatedToIndex = 
			TextFileBasedCache.newInstance(new File("tmp.txt"),
					x->""+x, 
					x->Integer.parseInt(x), 
					x->StringCacheUtils.listOfStringToString(x), 
					x->StringCacheUtils.stringToListOfString(x), "\t");

	public static List<String> getAllPapersIdFrom(InstitutionIdentifier instId, Optional<Integer> startTime){
		//		//https://api.elsevier.com/content/search/scopus?query=AF-ID(60031040)&field=dc:identifier&count=100&start=0&APIKey=9f577f44be144470bcbd7ecd5176f8e0

		String filter = "AF-ID("+instId.getId()+")";



		//List<String> res = new 

		int start= 1960;
		if(startTime.isPresent())
			start = startTime.get();

		List<Integer> years = new ArrayList<>();
		for(int year = start ; year <= 2023; year ++)
			years.add(year);

		AtomicInteger total = new AtomicInteger();

		List<String> res = years.stream().map(year->
		{
			List<String> resl = new ArrayList<>();
			if(allReferencesRelatedToIndex.has(year))
			{
				return allReferencesRelatedToIndex.get(year);
			}
			String filterForCurrentYear = filter+" and pubyear is "+year;
			JSONObject firstQuery = getBasicRequestResult(filterForCurrentYear);
			JSONObject results = (JSONObject) firstQuery.get("search-results");
			int nbResultsForThisYear = Integer.parseInt((String)results.get("opensearch:totalResults"));
			List<String> resForYear = new ArrayList<>();
			if(nbResultsForThisYear>5000) throw new Error();
			for(int i = 0; i < nbResultsForThisYear ; i = i + NUMBER_ITEMS_RETURNED_PER_REQUEST)
			{

				List<String> newItems = getAllReferencesRelatedToFromIndex(filterForCurrentYear, i);
				System.out.println("Retrieving scopus papers IDs from institution for year: "+year+" "+i+"/"+nbResultsForThisYear+" "+total.addAndGet(newItems.size()));
				resl.addAll(newItems);
				resForYear.addAll(newItems);


			}
			allReferencesRelatedToIndex.add(year, resForYear);
			System.out.println("DONE FOR YEAR"+year);
			return resl;
		}).reduce(new ArrayList<>(), (x,y)->{x.addAll(y); return x;});
		return res;	
	}


	private static List<String> getAllReferencesRelatedToFromIndex(String filter, int startingIndex) {

		//should transform it to also include the query


		JSONObject firstQuery = getBasicRequestResult(filter+"&field=dc:identifier&start="+startingIndex);
		JSONObject results = (JSONObject) firstQuery.get("search-results");
		JSONArray entries = (JSONArray) results.get("entry");

		List<String> res = (List<String>)entries.stream()
				.map(x-> 
				{
					JSONObject x2 =  ((JSONObject)x);
					String val = (String)x2.get("dc:identifier");
					String payload = val.substring(val.indexOf(":")+1);
					return payload;

				})
				.collect(Collectors.toList());

		return res;
	}

	public static JSONObject getBasicRequestResult(String query)
	{
		try {
			String key = SCOPUS_KEYS.iterator().next();
			HttpGet httpget = new HttpGet();
			String query2 = ("https://api.elsevier.com/content/search/scopus?query="+query+"&APIKey="+key).replaceAll(" ", "%20");
			URI toSearch = new URI(query2);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			String responseStr = EntityUtils.toString(response.getEntity());
			response.close();
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject)parser.parse(responseStr);
			return jsonObject;

		} catch (IOException | URISyntaxException | ParseException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	public static Map<String, String> getFieldInfo(String id)
	{
		String fields = "field=title,authors";
		String responseStr = null;
		try {
			HttpGet httpget = new HttpGet();
			String key = SCOPUS_KEYS.iterator().next();
			URI toSearch = new URI("https://api.elsevier.com/content/abstract/scopus_id/"+id+"?"+fields+"&APIKey="+key);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			responseStr = EntityUtils.toString(response.getEntity());
			response.close();

			if(responseStr.contains("RESOURCE_NOT_FOUND"))
			{
				Map<String, String> res = new HashMap<>();
				res.put("found_on_scopus", "false");
				return res;
			}
			if(responseStr.contains("RATE_LIMIT_EXCEEDED"))
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return getFieldInfo(id);
			}

			if(responseStr.contains("TOO_MANY_REQUESTS")||responseStr.contains("QUOTA_EXCEEDED"))
			{
				SCOPUS_KEYS.remove(key);
				return getFieldInfo(id);
			}


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(responseStr));
			Document d = builder.parse(is); 
			Node n = d.getFirstChild();
			NodeList nl = n.getChildNodes();
			Map<String, String> res = new HashMap<>();
			for(int i = 0 ; i < nl.getLength(); i++)
			{
				Node child = nl.item(i);
				if(child.getNodeName().equals("coredata"))
					for(int j = 0; j < child.getChildNodes().getLength(); j++)
					{
						Node childOfChild = child.getChildNodes().item(j);
						if(childOfChild.getNodeName().equals("dc:title"))
							res.put("dc:title", childOfChild.getFirstChild().getNodeValue());
						else throw new Error();
					}
				else if(child.getNodeName().equals("authors"))
				{
					processAuthorsListToIds(child, res);
				}
				else throw new Error();
			}
			return res;

		} catch (IOException | URISyntaxException | ParserConfigurationException| SAXException e) {
			System.err.println("Response string"+responseStr);
			e.printStackTrace();
			throw new Error();
		}
	}

	public static Map<String, String> getAbstractInfo(String id)
	{
		Map<String, String> res = new HashMap<>();
		try {
			HttpGet httpget = new HttpGet();
			String key = SCOPUS_KEYS.iterator().next();
			URI toSearch = new URI("https://api.elsevier.com/content/abstract/scopus_id/"+id+"?abstract&APIKey="+key);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			String responseStr = EntityUtils.toString(response.getEntity());
			response.close();

			if(responseStr.contains("RESOURCE_NOT_FOUND"))
			{
				res.put("found_on_scopus", "false");
				return res;
			}
			
			
			if(!responseStr.contains("ASJC"))
				res.put("asjc_classifications", "NO_CLASSIFICATION_FOUND_ON_SCOPUS");


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(responseStr));
			Document d = builder.parse(is); 
			Node n = d.getFirstChild();

			NodeList r1 = n.getChildNodes();

			for(int i = 0 ; i < r1.getLength(); i++)
			{
				Node child1 = r1.item(i);
				if(child1.getNodeName().equals("coredata"))
				{
					NodeList nl2 = child1.getChildNodes();
					for(int j = 0 ; j < nl2.getLength(); j++)
					{
						Node child2 = nl2.item(j);
						if(child2.getNodeName().equals("link"))
							continue;
						if(child2.getNodeName().equals("dc:creator"))
							continue;
						if(child2.getNodeName().equals("dc:description"))
						{
							NodeList nl3 = child2.getFirstChild().getChildNodes();
							for(int k = 0 ; k < nl3.getLength(); k++)
								if(nl3.item(k).getNodeName().equals("ce:para"))
								{
									Node ceParaNode = nl3.item(k);
									String abstr = "";
									for(int l = 0; l <ceParaNode.getChildNodes().getLength(); l++)
									{
										Node abstrNode = ceParaNode.getChildNodes().item(l);
										if(abstrNode.getNodeName().equals("mml:math"))
										{
											abstr+=abstrNode.getFirstChild().getNodeName();
										}
										else
											if(abstrNode.getNodeName().equals("sup")||abstrNode.getNodeName().equals("inf"))
											{

												if(abstrNode.hasChildNodes())
													abstr+=abstrNode.getFirstChild().getNodeValue();
											}
											else if(abstrNode.getNodeName().equals("#text"))
												abstr+=abstrNode.getNodeValue();
											else throw new Error();
									}
									res.put("abstract", abstr);
								}

							continue;
						}
						if(res.containsKey(child2.getNodeName()))
						{
							if(child2.getNodeName().equals("prism:isbn"))continue;
							throw new Error();
						}
						if(child2.hasChildNodes())
							res.put(child2.getNodeName(), child2.getFirstChild().getNodeValue());
						continue;
					}
				}
				else if(child1.getNodeName().equals("affiliation"))
				{
					if(!res.containsKey("affiliation_ids"))res.put("affiliation_ids","");
					res.put("affiliation_ids", res.get("affiliation_ids")+","+child1.getAttributes().getNamedItem("id").getNodeValue());
					continue;
				}
				else if(child1.getNodeName().equals("authors"))
					processAuthorsListToIds(child1, res);
				else if(child1.getNodeName().equals("dc:creator"))
					continue;
				else if(child1.getNodeName().equals("language"))
				{
					if(child1.hasChildNodes())res.put("language", child1.getAttributes().item(0).getNodeValue());
					continue;
				}
				else if(child1.getNodeName().equals("authkeywords"))
				{
					if(res.containsKey("keywords"))throw new Error();
					String all = "";
					NodeList nl2 = child1.getChildNodes();
					for(int j = 0; j < nl2.getLength() ; j++)
						all += ","+ nl2.item(j).getFirstChild().getNodeValue();
					if(!all.isBlank())
						res.put("keywords", all.substring(1));
				}
				else if(child1.getNodeName().equals("idxterms"))continue;
				else if(child1.getNodeName().equals("subject-areas")) 
				{
					if(res.containsKey("subject-areas"))throw new Error();
					String all = "";
					NodeList nl2 = child1.getChildNodes();
					for(int j = 0; j < nl2.getLength() ; j++)
						all += ","+ nl2.item(j).getFirstChild().getNodeValue();
					if(all.isBlank())continue;
					res.put("subject-areas", all.substring(1));
				}
				else if(child1.getNodeName().equals("item"))
				{
					NodeList nl2 = child1.getChildNodes();
					for(int j = 0; j < nl2.getLength() ; j++)
					{
						Node n2 = nl2.item(j);
						if(n2.getNodeName().equals("xocs:meta")) continue;
						if(n2.getNodeName().equals("ait:process-info")) continue;
						if(n2.getNodeName().equals("bibrecord"))
						{
							NodeList nl3 = n2.getChildNodes();
							for(int k = 0; k < nl3.getLength() ; k++)
							{
								Node n3 = nl3.item(k);
								if(n3.getNodeName().equals("item-info"))continue;
								if(n3.getNodeName().equals("head"))
								{
									NodeList nl4 = n3.getChildNodes();
									for(int l = 0; l < nl4.getLength() ; l++)
									{
										Node n4 = nl4.item(l);
										if(n4.getNodeName().equals("citation-info")) continue;
										if(n4.getNodeName().equals("citation-title")) continue;
										if(n4.getNodeName().equals("author-group")) continue;
										if(n4.getNodeName().equals("correspondence")) continue;
										if(n4.getNodeName().equals("grantlist")) continue;
										if(n4.getNodeName().equals("abstracts")) continue;
										if(n4.getNodeName().equals("source")) continue;
										if(n4.getNodeName().equals("enhancement")) {
											NodeList nl5 = n4.getChildNodes();
											for(int m = 0; m < nl5.getLength() ; m++)
											{
												Node n5 = nl5.item(m);
												if(n5.getNodeName().equals("classificationgroup"))
												{
													NodeList nl6 = n5.getChildNodes();
													for(int o = 0; o < nl6.getLength() ; o++)
													{
														Node n6 = nl6.item(o);
														if(n6.getNodeName().equals("classifications"))
														{
															String value =n6.getAttributes().getNamedItem("type").getNodeValue();
															if(value.equals("GEOCLASS"))continue;
															if(value.equals("ASJC"))
															{
																String classifications = "";
																
																NodeList nl7 = n6.getChildNodes();
																for(int p = 0; p < nl7.getLength() ; p++)
																{
																	Node n7 = nl7.item(p);
																	classifications+=","+n7.getFirstChild().getNodeValue();
																}
																
																res.put("asjc_classifications", classifications.substring(1));
																continue;
																
															}
															if(value.equals("SUBJABBR"))continue;
															if(value.equals("CABSCLASS")) continue;
															if(value.equals("EMCLASS")) continue;
															if(value.equals("CPXCLASS")) continue;
															if(value.equals("FLXCLASS")) continue;
															if(value.equals("SUBJECT")) continue;
															if(value.equals("ENCOMPASSCLASS")) continue;
															throw new Error();
														}
														throw new Error();
													}
													continue;
												}
												if(n5.getNodeName().equals("chemicalgroup"))continue;
												if(n5.getNodeName().equals("sequencebanks"))continue;
												if(n5.getNodeName().equals("tradenamegroup"))continue;
												if(n5.getNodeName().equals("descriptorgroup"))continue;
												if(n5.getNodeName().equals("manufacturergroup"))continue;
												throw new Error();
											}
											continue;
										}
										if(n4.getNodeName().equals("related-item")) continue;
										throw new Error();
									}
									continue;
								}
								if(n3.getNodeName().equals("tail"))
								{
									for(int l = 0; l < n3.getChildNodes().getLength(); l++)
									{
										Node n4 = n3.getChildNodes().item(l);
										if(n4.getNodeName().equals("bibliography"))
										{
											if(res.containsKey("cited_reference_ids"))
												throw new Error();
											for(int m=0; m<n4.getChildNodes().getLength();m++)
											{
												Node n5=n4.getChildNodes().item(m);
												if(n5.getNodeName().equals("reference"))
												{
													for(int o=0; o<n5.getChildNodes().getLength();o++)
													{
														Node n6=n5.getChildNodes().item(o);
														if(n6.getNodeName().equals("ref-info"))
														{
															for(int p=0; p<n6.getChildNodes().getLength();p++)
															{
																Node n7=n6.getChildNodes().item(p);
																if(n7.getNodeName().equals("ref-title"))continue;
																if(n7.getNodeName().equals("refd-itemidlist"))
																{
																	for(int q=0; q<n7.getChildNodes().getLength();q++)
																	{
																		Node n8=n7.getChildNodes().item(q);
																		String type = n8.getAttributes().getNamedItem("idtype").getNodeValue();
																		if(type.equals("SGR"))
																		{
																			String refId = n8.getFirstChild().getNodeValue();
																			if(res.containsKey("cited_reference_ids"))
																				res.put("cited_reference_ids", refId);
																			else 
																				res.put("cited_reference_ids", ","+refId);
																		}
																		else throw new Error();
																	}
																	continue;
																}
																if(n7.getNodeName().equals("ref-authors"))continue;
																if(n7.getNodeName().equals("ref-publicationyear"))continue;
																if(n7.getNodeName().equals("ref-text"))continue;
																if(n7.getNodeName().equals("ref-sourcetitle"))continue;
																if(n7.getNodeName().equals("ref-volisspag"))continue;
																if(n7.getNodeName().equals("ref-website"))continue;
																throw new Error();
															}
															continue;
														}
														if(n6.getNodeName().equals("ref-fulltext"))continue;
														throw new Error();
													}
													continue;
												}
												throw new Error();
											}
											continue;
										}
										continue;
									}
									continue;
								}
								throw new Error();
							}
							continue;
						}
						throw new Error();
					}

					continue;
				}
				else throw new Error();
			}
			if(res.containsValue(null))
				throw new Error();
			return res;

		} catch (IOException | URISyntaxException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	private static void processAuthorsListToIds(Node child, Map<String, String> res) {
		if(res.containsKey("author_ids"))throw new Error();
		res.put("author_ids", "");
		for(int j = 0; j < child.getChildNodes().getLength(); j++)
		{
			Node authorDescription = child.getChildNodes().item(j);
			res.put("author_ids", res.get("author_ids")+","+authorDescription.getAttributes().getNamedItem("auid").getNodeValue());
			/*if(!res.containsKey("authors")) res.put("authors", "");
			if(authorDescription.getNodeName().equals("author"))
			{
				childOfChild
				res.put("title", child.getNodeValue());
			}
			else throw new Error();*/
		}
		if(res.get("author_ids").isEmpty()) res.put("author_ids", "NO_AUTHORS_PROVIDED");
		else
			res.put("author_ids",res.get("author_ids").substring(1));
	}



	public static Reference getReferenceFromPaperId(String l) {
		if(cachePaperIdToReference.has(l))
			return cachePaperIdToReference.get(l);
		
		System.out.println("Looking up on Scopus for:"+l);
		Map<String, String> basicResult = getFieldInfo(l);
		if(basicResult.containsKey("found_on_scopus")&&
				basicResult.get("found_on_scopus").equals("false"))
		{
			basicResult.put("scopus_id", l+"");
			Reference res = Reference.newInstance(basicResult);
			cachePaperIdToReference.add(l, res);
			return res;
		}

		Map<String,String> abstractInfo = getAbstractInfo(l);

		List<String> allReferencesIds = getAllReferencesFromPaperId(l);


		for(String k:basicResult.keySet())
			if(abstractInfo.containsKey(k)&&!abstractInfo.get(k).equals(basicResult.get(k)))
				throw new Error();
			else abstractInfo.put(k, basicResult.get(k));

		abstractInfo.put("scopus_id", l+"");
		
		if(allReferencesIds.isEmpty())
			abstractInfo.put(Reference.CITED_REF_ID_KEY, "NO_CITATIONS_FOUND");
		else abstractInfo.put(Reference.CITED_REF_ID_KEY, allReferencesIds.stream().reduce("", (x,y)->x+","+y).substring(1));

		Reference res = Reference.newInstance(abstractInfo);
		cachePaperIdToReference.add(l, res);
		return res;
	}


	private static List<String> getAllReferencesFromPaperId(String l) {
		String responseStr = null;
		HttpGet httpget = new HttpGet();
		URI toSearch;
		try {
			String currentKey = getCurrentKey();
			toSearch = new URI("https://api.elsevier.com/content/abstract/scopus_id/"+l+"?"
					+ "view=REF&APIKey="+currentKey);

			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			responseStr = EntityUtils.toString(response.getEntity());
			response.close();

			if(responseStr.contains("RESOURCE_NOT_FOUND"))
			{
				System.err.println("Resource not found");
				throw new Error();
			}
			if(responseStr.contains("TOO_MANY_REQUESTS"))
			{
				System.err.println("Too many requests");
				throw new Error();
			}

			if(responseStr.contains("QUOTA_EXCEEDED")||responseStr.contains("RATE_LIMIT_EXCEEDED"))
			{
				SCOPUS_KEYS.remove(currentKey);
				return getAllReferencesFromPaperId(l);
			}


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(responseStr));
			Document d = builder.parse(is); 
			if(d.getChildNodes().getLength()==1&&d.getFirstChild().getChildNodes().getLength()==0)
				return new ArrayList<>();
			Node n = d.getFirstChild().getFirstChild();
			int totalToProcess = 
					Integer.parseInt(n.getAttributes().getNamedItem("total-references").getNodeValue());
			List<String> allReferences = new ArrayList<>();
			
			for(int ref=0; ref <= totalToProcess; ref+=40)
			{
				int toAsk = 40;
				if(totalToProcess-ref<40)
					toAsk = totalToProcess-ref;
				allReferences.addAll(getAllReferencesFromPaperId(l,ref,toAsk));
			}
			/*if(allReferences.size()!=totalToProcess-1)
				throw new Error();*/
			return allReferences;
		} catch (URISyntaxException | ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new Error();
	}
	
	private static List<String> getAllReferencesFromPaperId(String l,int currentRefNumber, int numberToAsk) {
		String responseStr = null;
		HttpGet httpget = new HttpGet();
		String key = SCOPUS_KEYS.iterator().next();
		URI toSearch;
		try {
			toSearch = new URI("https://api.elsevier.com/content/abstract/scopus_id/"+l+"?"
					+ "startref="+currentRefNumber+"&refcount="+numberToAsk
					+ "&view=REF&APIKey="+getCurrentKey());

			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			responseStr = EntityUtils.toString(response.getEntity());
			response.close();

			if(responseStr.contains("RESOURCE_NOT_FOUND"))
			{
				System.err.println("Resource not found");
				throw new Error();
			}
			if(responseStr.contains("TOO_MANY_REQUESTS"))
			{
				System.err.println("Too many requests");
				throw new Error();
			}

			if(responseStr.contains("QUOTA_EXCEEDED"))
			{
				System.err.println("Quota exceeded");
				throw new Error();
			}


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(responseStr));
			Document d = builder.parse(is); 
			Node n = d.getFirstChild().getFirstChild();
			NodeList nl = n.getChildNodes();
			List<String> res = new ArrayList<>();
			for(int i = 0 ; i < nl.getLength(); i++)
			{
				Node n1 = nl.item(i);
				if(n1.getNodeName().equals("link"))continue;
				if(n1.getNodeName().equals("reference"))
				{					
					boolean found = false;
					for(int j = 0 ; j < n1.getChildNodes().getLength(); j++)
					{
						Node n2 = n1.getChildNodes().item(j);
						if(n2.getNodeName().equals("scopus-id"))
						{
							res.add(n2.getFirstChild().getNodeValue());
							found = true;
						}
					}
					if(!found)throw new Error();
					continue;
				}
					
				throw new Error();
			}
			return res;
		} catch (URISyntaxException | ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new Error();
	}


	private static String getCurrentKey() {
		return SCOPUS_KEYS.iterator().next();
	}


	public static List<String> getAllScopusIdReferencesFromAuthor(String authorId) {
		final int maxSizeQuery = 200;
		try {
			HttpGet httpget = new HttpGet();
			String key = SCOPUS_KEYS.iterator().next();
			String query2 = ("https://api.elsevier.com/content/search/scopus?query=AU-ID("+authorId+")&field=dc:identifier&count="+maxSizeQuery+"&APIKey="+key).replaceAll(" ", "%20");
			URI toSearch = new URI(query2);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			String responseStr = EntityUtils.toString(response.getEntity());
			response.close();
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject)parser.parse(responseStr);

			JSONObject results = (JSONObject) jsonObject.get("search-results");
			JSONArray allPapers = (JSONArray) results.get("entry");
			int nbPapers = Integer.parseInt((String) results.get("opensearch:totalResults"));
			
			
			List<String> res = new ArrayList<>();
			for(int i = 0; i < nbPapers; i=i+200)
			{
				
				//.get("dc:identifier")).map(x->(String)x)
				List<String> local = getAllScopusIdReferencesFromAuthor(authorId,i);
				res.addAll(local);
			}
			return res;

		} catch (IOException | URISyntaxException | ParseException e) {
			e.printStackTrace();
			throw new Error();
		}
	}
	
	public static List<String> getAllScopusIdReferencesFromAuthor(String authorId, int startQuery) {
		final int maxSizeQuery = 200;
		try {
			HttpGet httpget = new HttpGet();
			String key = SCOPUS_KEYS.iterator().next();
			String query2 = ("https://api.elsevier.com/content/search/scopus?query"
					+ "=AU-ID("+authorId+")"
					+ "&field=dc:identifier"
					+ "&start="+startQuery
					+ "&count="+maxSizeQuery+"&APIKey="+key).replaceAll(" ", "%20");
			URI toSearch = new URI(query2);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			String responseStr = EntityUtils.toString(response.getEntity());
			response.close();
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject)parser.parse(responseStr);

			JSONObject results = (JSONObject) jsonObject.get("search-results");
			JSONArray allPapers = (JSONArray) results.get("entry");

			return (List<String>) allPapers.stream()
			.map(x->{
				String x2 = (String)((JSONObject)x).get("dc:identifier");
				return x2.substring(x2.indexOf(":")+1);
			})
			.collect(Collectors.toList());
			


		} catch (IOException | URISyntaxException | ParseException e) {
			e.printStackTrace();
			throw new Error();
		}
	}


	public static Set<Reference> translateIntoReference(Set<String> allScopusIdReferencesFrom) {
		return allScopusIdReferencesFrom.stream().map(x->getReferenceFromPaperId(x)).collect(Collectors.toSet());
	}

	public static Map<String, Integer> getTopicsOfResearcher(String researcherId){
		//https://api.elsevier.com/content/author?author_id=37076777600&APIKey=82f0358903cf316de433c0cad3d00940
		//can recover topic codes and specific issues code
		throw new Error();
	}


	public static AuthorDescription getAuthorDescriptionFromId(String authorId) {
		if(cacheAuthorIdToReference.has(authorId))
			return cacheAuthorIdToReference.get(authorId);

		try {
			HttpGet httpget = new HttpGet();
			String key = SCOPUS_KEYS.iterator().next();
			String query2 = ("https://api.elsevier.com/content/author?author_id="+authorId+"&&APIKey="+key).replaceAll(" ", "%20");
			URI toSearch = new URI(query2);
			httpget.setURI(toSearch);
			CloseableHttpResponse response = httpclient.execute(httpget);
			String responseStr = EntityUtils.toString(response.getEntity());
			response.close();

			if(responseStr.contains("RATE_LIMIT_EXCEEDED")) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return getAuthorDescriptionFromId(authorId);
			}

			if(responseStr.contains("QUOTA_EXCEEDED")||responseStr.contains("TOO_MANY_REQUESTS"))
			{
				SCOPUS_KEYS.remove(key);
				return getAuthorDescriptionFromId(authorId);
			}


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(responseStr));
			Document d = builder.parse(is); 
			Node n = d.getFirstChild();

			Map<String, String> m = new HashMap<>();
			NodeList r1 = n.getChildNodes();

			int variantNumber = 0;

			m.put("scopus-id", authorId);

			if(responseStr.contains("RESOURCE_NOT_FOUND"))
			{
				m.put("resource-not-found", "true");
				return AuthorDescription.newInstance(m);
			}

			for(int i = 0 ; i < r1.getLength(); i++)
			{
				Node n1 = r1.item(i);
				if(n1.getNodeName().equals("coredata"))
				{
					NodeList nl2 = n1.getChildNodes();
					for(int j = 0 ; j < nl2.getLength(); j++)
					{
						Node n2 = nl2.item(j);
						if(n2.getChildNodes().getLength()==1 && n2.getChildNodes().item(0).getNodeName().equals("#text"))
						{
							m.put(n2.getNodeName(), n2.getChildNodes().item(0).getNodeValue());
							continue;
						}
						if(n2.getNodeName().equals("link"))
							continue;
						throw new Error();
					}
					continue;
				}
				else if(n1.getNodeName().equals("affiliation-current"))
				{
					String currentAffiliationId= n1.getAttributes().getNamedItem("id").getNodeValue();
					m.put("current_affiliations_id", currentAffiliationId);
					continue;
				}
				else if(n1.getNodeName().equals("subject-areas"))
					continue;
				else if(n1.getNodeName().equals("author-profile"))
				{
					NodeList nl2 = n1.getChildNodes();
					for(int j = 0 ; j < nl2.getLength(); j++)
					{
						Node n2 = nl2.item(j);
						if(n2.getNodeName().equals("status"))continue;
						if(n2.getNodeName().equals("date-created"))continue;
						if(n2.getNodeName().equals("preferred-name"))
						{
							NodeList nl3 = n2.getChildNodes();
							for(int k = 0 ; k < nl3.getLength(); k++)
							{
								Node n3 = nl3.item(k);
								if(n3.getNodeName().equals("#text")&& n3.getNodeValue().isBlank())
									continue;
								if(n3.getChildNodes().getLength()==1 && n3.getChildNodes().item(0).getNodeName().equals("#text"))
								{
									if(m.containsKey(n3.getNodeName()))throw new Error();
									m.put(n3.getNodeName(), n3.getChildNodes().item(0).getNodeValue());
									continue;
								}
								if(!n3.hasChildNodes())continue;
								throw new Error();
							}
							continue;
						}
						if(n2.getNodeName().equals("name-variant"))
						{
							NodeList nl3 = n2.getChildNodes();
							for(int k = 0 ; k < nl3.getLength(); k++)
							{
								Node n3 = nl3.item(k);
								if(n3.getNodeName().equals("#text")&& n3.getNodeValue().isBlank())
									continue;
								if(n3.getChildNodes().getLength()==1 && n3.getChildNodes().item(0).getNodeName().equals("#text"))
								{
									if(m.containsKey("variant"+variantNumber+"_"+n3.getNodeName()))throw new Error();
									m.put("variant"+variantNumber+"_"+n3.getNodeName(), n3.getChildNodes().item(0).getNodeValue());
									continue;
								}
								if(!n3.hasChildNodes())continue;
								throw new Error();
							}
							variantNumber++;
							continue;
						}
						if(n2.getNodeName().equals("classificationgroup"))
						{
							NodeList nl3 = n2.getChildNodes();
							for(int k = 0 ; k < nl3.getLength(); k++)
							{
								Node n3 = nl3.item(k);
								if(n3.getNodeName().equals("#text")&&n3.getNodeValue().isBlank()) continue;
								if(n3.getNodeName().equals("classifications"))
								{
									NodeList nl4 = n3.getChildNodes();
									for(int l = 0 ; l < nl4.getLength(); l++)
									{
										Node n4 = nl4.item(l);
										if(n4.getNodeName().equals("#text")&&n4.getNodeValue().isBlank()) continue;
										if(n4.getNodeName().equals("classification")&& n4.getChildNodes().getLength()==1 && n4.getChildNodes().item(0).getNodeName().equals("#text"))
										{
											String count = n4.getAttributes().getNamedItem("frequency").getNodeValue();
											String id = "category_publication:"+n4.getChildNodes().item(0).getNodeValue();
											if(m.containsKey(id))
												throw new Error();
											m.put(id,count);
											continue;
										}
										throw new Error();
									}
									continue;
								}
								throw new Error();
							}
							continue;
						}
						if(n2.getNodeName().equals("publication-range"))continue;
						if(n2.getNodeName().equals("journal-history"))continue;
						if(n2.getNodeName().equals("affiliation-current"))
						{
							String allAffiliations = "";
							Set<String> allAff = new HashSet<>();

							for(int k = 0 ; k < n2.getChildNodes().getLength(); k++)
							{
								allAff.add(n2.getChildNodes().item(k).getAttributes().getNamedItem("affiliation-id").getNodeValue());
								if(n2.getChildNodes().item(k).getAttributes().getNamedItem("parent")!=null)
									allAff.add(n2.getChildNodes().item(k).getAttributes().getNamedItem("parent").getNodeValue());
								if(m.containsKey("current_affiliations_id"))
									allAff.addAll(Arrays.asList(m.get("current_affiliations_id").split(",")));
							}

							m.put("current_affiliations_id", allAff.stream().reduce("", (x,y)->x+","+y).substring(1));
							continue;
						}
						if(n2.getNodeName().equals("affiliation-history"))
						{
							NodeList nl3 = n2.getChildNodes();
							Set<String> allAff = new HashSet<>();
							for(int k = 0 ; k < nl3.getLength(); k++)
							{
								Node n3 = nl3.item(k);
								if(n3.getAttributes().getNamedItem("affiliation-id")!=null)
									allAff.add(n3.getAttributes().getNamedItem("affiliation-id").getNodeValue());
								if(n3.getAttributes().getNamedItem("id")!=null)
									allAff.add(n3.getAttributes().getNamedItem("id").getNodeValue());
								if(n3.getAttributes().getNamedItem("parent")!=null)
									allAff.add(n3.getAttributes().getNamedItem("parent").getNodeValue());
							}

							m.put("former_affiliations_id", allAff.stream().reduce("", (x,y)->x+","+y).substring(1));
							continue;
						}
						if(n2.getNodeName().equals("alias"))continue;
						if(n2.getNodeName().equals("orcid")) {
							m.put("orcid", n2.getFirstChild().getNodeValue());
							continue;
						}
						if(n2.getNodeName().equals("manual-curation"))
							continue;
						throw new Error();
					}
					continue;
				}
				if(n1.getNodeName().equals("affiliation-history"))
				{
					NodeList nl2 = n1.getChildNodes();
					Set<String> allAff = new HashSet<>();
					for(int k = 0 ; k < nl2.getLength(); k++)
					{
						Node n2 = nl2.item(k);
						if(n2.getAttributes().getNamedItem("affiliation-id")!=null)
							allAff.add(n2.getAttributes().getNamedItem("affiliation-id").getNodeValue());
						if(n2.getAttributes().getNamedItem("id")!=null)
							allAff.add(n2.getAttributes().getNamedItem("id").getNodeValue());
						if(n2.getAttributes().getNamedItem("parent")!=null)
							allAff.add(n2.getAttributes().getNamedItem("parent").getNodeValue());
					}

					m.put("former_affiliations_id", allAff.stream().reduce("", (x,y)->x+","+y).substring(1));
					continue;
				}
				else if(n1.getNodeName().equals("alias"))continue;
				else throw new Error();
			}





			AuthorDescription res = AuthorDescription.newInstance(m);
			cacheAuthorIdToReference.add(authorId, res);
			return res;
		} catch (IOException | URISyntaxException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			throw new Error();
		}

	}


	public static List<Reference> getAllPapersFromAuthor(AuthorDescription ad) {
		return getAllScopusIdReferencesFromAuthor(ad.getScopusId())
				.stream()
				.map(x->ScopusSearcher.getReferenceFromPaperId(x))
				.collect(Collectors.toList());		
	}
}
