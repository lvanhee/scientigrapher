package scientigrapher.datagathering;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cachingutils.Cache;
import cachingutils.TextFileBasedCache;
import cachingutils.advanced.StringCacheUtils;
import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;

public class PdfLinksGatherer {

	public static Set<URL> getUnpaywallLinks(Reference r) {
		
		HttpClient client = HttpClient.newHttpClient();
		//   HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/search/?query="+title+"&is_oa=true&email=unpaywall_00@example.com"))
		HttpRequest request = null;
		URI target = null;
		if(r.getDoi()!=null)
			request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/"+r.getDoi()+"?email=a.b@gmail.com"))    	
			.build();
		else request = 
				HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/search/?query="+r.getTitle().replaceAll("\"", "").replaceAll(" ", "%20")+"&is_oa=true&email=unpaywall_00@example.com")).build();
		HttpResponse<String> response=null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("DOI not found");
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new Error("DOI not found");
		}
	
		return Arrays.asList(response.body().toString().split("\n")).stream()
		.filter(x->x.contains("url_for_pdf"))
		.filter(x->x!=null && !x.equals("null") &&x.length()>5 && !x.contains("\"url_for_pdf\": null"))
		.map(x->
		{
			final String original = x;
			if(!original.contains("url_for_pdf"))
				throw new Error();
			String y  = x.substring(x.indexOf("url_for_pdf")+14);
			if(y.startsWith("\""))y = y.substring(1);
			if(y.contains("\","))
				y = y.substring(0,y.indexOf("\","));
			while(y.endsWith(" ")|| y.endsWith(",")||y.endsWith("\""))
				y = y.substring(0,y.length()-1);
	
			try {
				return new URI(y).toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			throw new Error();				
		}
		//.replaceAll("\"", "").replaceAll(",", "").replaceAll(" ", "")
				)
		.collect(Collectors.toSet()); 
	}

	public static Set<URL> getGoogleScholarLinks(Reference r) {
		String fullText = GoogleScholarGatherer.getHtmlOfGoogleScholarReferencePage(r);
		Set<URL> links = Arrays.asList(fullText.split("\n")).stream()
				.filter(x->x.contains(".pdf"))
				.map(x-> {
					String current = x.substring(x.indexOf("http"), x.indexOf(".pdf")+4)
							.replaceAll("<b>", "")
							.replaceAll("</b>", "");
					while(current.substring(1).contains("http"))
					{
						int index = current.substring(1).indexOf("http");
						current = current.substring(index+1);
					}

					current = current.replaceAll("\\[", "");
					current = current.replaceAll("\\]", "");

					if(current.contains(" ")) return null;
					current = current.replaceAll("#", "%23");
					try {
						return new URI(current).toURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
						return null;
					}
					throw new Error();
				})
				.filter(x->x!=null)
				.collect(Collectors.toSet());


		Set<URL> links2 = Arrays.asList(fullText.split("PDF")).stream()
				.filter(x->x.contains("https"))
				.map(
						x->
						{
							String current = x;
							while(current.substring(1).contains("https"))
							{
								int index = current.substring(1).indexOf("https");
								current = current.substring(index+1);
							}
							if(current.contains("\""))
								current = current.substring(0,current.indexOf("\""));	

							current = current.replaceAll("\\[", "");
							current = current.replaceAll("\\]", "");
							current = current.replaceAll("<b>", "");
							current = current.replaceAll("</b>", "");
							current = current.replaceAll("<br>", "");
							current = current.replaceAll("\n", "");
							if(current.contains(" "))
								current = current.substring(0,current.indexOf(" "));
							
							current = current.replaceAll("#", "%23");

							if(!isAllowedProvider(current)) return null;
							try {
								current = transformNameToGetToPDF(current);
								return new URI(current).toURL();
							} catch (MalformedURLException | URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							throw new Error();
						}
						)
				.filter(x->x!=null)
				.collect(Collectors.toSet());


		links.addAll(links2);
		return links;
	}
	
	private static String transformNameToGetToPDF(String current) {
		if(current.contains("https://www.ncbi.nlm.nih.gov"))
			return current+"/pdf";
		return current;
	}

	private static boolean isAllowedProvider(String current) {
		if(current.startsWith("https://www.example.edu/paper.pdf"))return false;
		if(current.contains("ieeexplore"))return false;

		if(current.contains(".pdf"))return true;
		if(current.startsWith("https://dl.acm.org/doi/pdf/"))return true;
		if(current.startsWith("https://www.liebertpub.com/doi/pdf/"))return true;
		if(current.startsWith("https://www.tandfonline.com/doi/pdf/"))return true;
		if(current.contains("/doi/pdf/"))return true;
		if(current.startsWith("https://www.mdpi.com/")&&current.endsWith("/pdf"))return true;
		if(current.startsWith("https://iopscience.iop.org/article/")&&current.endsWith("/pdf"))return true;
		if(current.contains("https://arxiv.org/pdf/"))return true;
		if(current.contains("https://citeseerx.ist.psu.edu/viewdoc/download")&&current.endsWith("type=pdf"))return true;
		if(current.contains("https://www.computer.org/csdl/api/v1/periodical/mags/"))return true;
		if(current.contains("https://onlinelibrary.wiley.com/doi/pdfdirect/"))return true;
		if(current.contains("https://journals.vgtu.lt/"))return true;
		if(current.contains("https://doi.org/"))return true;
		if(current.contains("https://hal."))return true;
		if(current.contains("https://revistas.javeriana.edu.co/index.php/revPsycho/"))return true;
		if(current.contains("https://revistia.org/index.php/"))return true;
		if(current.contains("https://par.nsf.gov/"))return true;
		

		if(current.contains("https://www.ncbi.nlm.nih.gov/"))return true;
		//though reachable pdf from the link

		if(current.startsWith("https://dl.acm.org/doi/abs/"))return false;
		if(current.startsWith("https://scholar.google"))return false;
		if(current.startsWith("https://ieeexplore.ieee.org/abstract/"))return false;
		if(current.startsWith("https://www.tandfonline.com/doi/abs/"))return false;

		if(current.startsWith("https://link.springer.com/chapter/"))return false;
		if(current.startsWith("https://www.igi-global.com/chapter/"))return false;
		if(current.startsWith("https://books.google.com/books"))return false;
		if(current.startsWith("https://www.sciencedirect.com/science/article/"))return false;
		if(current.endsWith("html"))return false;
		if(current.contains("/doi/abs/"))return false;
		if(current.startsWith("https://www.ingentaconnect.com/content/cog/"))return false;
		if(current.startsWith("https://www.computer.org/csdl/proceedings-article/"))return false;
		if(current.contains("https://www.emerald.com/insight/content/doi/"))return false;
		if(current.contains("https://aisel.aisnet.org/jise/"))return false;
		if(current.contains("https://www.learntechlib.org/p/"))return false;
		if(current.contains("https://aisel.aisnet.org/"))return false;
		if(current.contains("https://www.ceeol.com/search/article-detail"))return false;
		if(current.contains("https://digital-library.theiet.org/content/journals/"))return false;
		if(current.startsWith("https://iopscience.iop.org/article/")&&current.endsWith("/meta"))return false;
		if(current.contains("https://link.springer.com/article/"))return false;
		if(current.contains("https://dl.acm.org/doi/fullHtml/"))return false;
		if(current.contains("https://www.jstor.org/stable/"))return false;
		if(current.contains("https://search.proquest.com/openview"))return false;
		if(current.contains("https://www.pdcnet.org/teachphil/content/"))return false;
		if(current.contains("https://id.google.com/verify/"))return false;
		if(current.contains("https://www.actapress.com/"))return false;
		if(current.contains("https://www.biodiversity-science.net/EN/article/downloadArticleFile.do"))
			return false;
		if(current.contains("https://stanford.library.sydney.edu.au"))
			return false;
		if(current.contains("https://ebooks.iospress.nl"))
			return false;
		if(current.contains("https://drive.google.com/file/"))return false;
		if(current.contains("https://ojs.aaai.org/index.php/AAAI/article/download/"))return false;
		if(current.contains("https://elibrary.ru/"))return false;

		if(current.contains("https://content.iospress.com/articles/"))return false;
		if(current.contains("https://repository.tudelft.nl/islandora/object/"))return false;
		if(current.contains("https://psycnet.apa.org"))return false;
		
		return true;
		
	//	throw new Error();
	}

	
	private final static Cache<Reference, URL> workingPdfLinkForReference =
			TextFileBasedCache.newInstance(
					ProgramwideParameters.REFERENCE_TO_WORKING_PDF_URL_DATABASE,
					//null,
					(Reference x)->Reference.toParsableString(x,Arrays.asList("|","\t").stream().collect(Collectors.toSet())),
					//null,
					(String s)->Reference.fromParsableString(s), 
					(URL u)->u.toString(),
					//null,
					(String s)->{try {
						return new URL(s);
					} catch (MalformedURLException e) {
						e.printStackTrace();
						throw new Error();
					}}, 
					StringCacheUtils.separatorUrlSet());
			
	public static URL getOneWorkingLinkToAPdfFor(Reference reference) {
		if(workingPdfLinkForReference.has(reference))
			return workingPdfLinkForReference.get(reference);
		List<URL> validLinks = new LinkedList<>();
		
		validLinks.addAll(getUnpaywallLinks(reference));
		validLinks.addAll(getGoogleScholarLinks(reference));
		
		for(URL u:validLinks)
		{
			if(OnlinePdfGatherer.downloadPdfFromSetOfLinks(Arrays.asList(u).stream().collect(Collectors.toSet()), reference))
			{
				workingPdfLinkForReference.add(reference, u);
				return u;
			}
		}
		
		return null;
	}

	public static URL getANonWorkingLinkToAPdfFor(Reference reference) {
		Set<URL> linksToPdfs = getUnpaywallLinks(reference);
		if(!linksToPdfs.isEmpty()) return linksToPdfs.iterator().next();
		linksToPdfs = getGoogleScholarLinks(reference);
		if(!linksToPdfs.isEmpty()) return linksToPdfs.iterator().next();
		if(reference.hasUrl())
			return reference.getUrl();
		return null;
	}

}
