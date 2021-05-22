package scientigrapher.input.pdfs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import scientigrapher.input.references.Reference;
import scientigrapher.pdfs.PdfReader;

import java.awt.Robot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdfparser.PDFParser;

public class ReferenceToPdfGetter {


	public static final String POSTS_API_URL = "https://api.unpaywall.org/v2/search";

	public static final File PDF_FOLDER = Paths.get("data/references").toFile();

	public static void main(String[] args) throws IOException, InterruptedException {

		purgeTheReferenceFolderOutOfNonPdf();

		List<String> failedOnes = new ArrayList<>();

		
		Set<Reference> allEntries = Reference.referencesFromBibFile("data/scopus.bib");

		int total = allEntries.size();

		allEntries = allEntries.parallelStream().filter(x->!isValidPDFFileAlreadyThere(x)).collect(Collectors.toSet());

		AtomicInteger successes = new AtomicInteger();
		AtomicInteger currentDone = new AtomicInteger();

		allEntries.parallelStream().forEach(r->
		downloadPdfWithUnpaywall(r));
		
		allEntries = allEntries.stream().filter(x->!isValidPDFFileAlreadyThere(x)).collect(Collectors.toSet());
		
		allEntries.stream()
		.forEach(x->ReferenceToPdfGetter
				.getGoogleScholarHtmlTextWithCaching(x));

		allEntries.stream()
		.forEach(
				r ->{
					System.out.println(r);
					currentDone.incrementAndGet();
					boolean result = downloadPdfFor(r);
					if(result)successes.incrementAndGet();

					System.out.println("Progress:"+ currentDone+"/"+total +
							" success:"+successes+"/"+currentDone);
				});

		System.out.println("Failed due to lack of information:"+failedOnes);

		purgeTheReferenceFolderOutOfNonPdf();

	}

	private static void purgeTheReferenceFolderOutOfNonPdf() {
		Arrays.asList(PDF_FOLDER.listFiles()).stream()
		.forEach(f->
		{
			//System.out.println("Checking for purging:"+f);
			if(!isValidFileAlreadyThere(f))
			{
				System.out.println("Deleting:"+f);
				f.delete();
			}
		});
	}

	public static boolean downloadPdfFor(Reference r) {
		if(isValidPDFFileAlreadyThere(r))
			return true;

		boolean found = downloadPdfWithUnpaywall(r);
		if(found)return found;
		return downloadPDFWithScholar(r);

	}

	private static boolean isValidPDFFileAlreadyThere(Reference r) {
		return isValidFileAlreadyThere(getPdfFileFor(r));
	}

	private static boolean isValidFileAlreadyThere(File f) {
		if(!f.exists())return false;
		if(f.length() < 100) return false;
		//if(f.exists() && f.length() > 100000)return true;
		
		
		

		try {
			Path p = f.toPath();
			List<String>allLines = Files.readAllLines(p, StandardCharsets.ISO_8859_1);

			int i = 0;
			String firstLine = allLines.get(i);
			while(firstLine.isEmpty())
			{
				firstLine = allLines.get(i);
				i++;
			}

			if(firstLine.toLowerCase().startsWith("<!doctype html"))
				return false;
			if(firstLine.startsWith("<html"))
				return false;
			if(firstLine.startsWith("<head"))
				return false;
			if(firstLine.startsWith("<?xml"))
				return false;
			if(firstLine.startsWith("<!--"))
				return false;
			if(firstLine.startsWith("%PDF"))
				return PdfReader.isParsingWorking(f);
			throw new Error();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	private static synchronized boolean downloadPDFWithScholar(Reference r) {

		


		return scholarRawInputToPDF(r, getGoogleScholarHtmlTextWithCaching(r));

	}


	private static String getGoogleScholarHtmlTextWithCaching(Reference r) {
		File cacheFile = getCachedGoogleScholarFile(r);

		if(cacheFile.exists())
		{

			try {
				return Files.readAllLines(
						cacheFile.toPath(),
						StandardCharsets.ISO_8859_1)
						.stream()
						.reduce((x,y)->x+y).get();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}

		}


		String fullText = null;
		String pageToAsk = null;
		if(r.getDoi()!=null)
		{
			String requestSideDoi = r.getDoi().replaceAll("/", "%2F");
			pageToAsk = "https://scholar.google.com/scholar?q="+requestSideDoi;
		}
		else 
		{
			String requestSideTitle = r.getTitle().replaceAll(" ", "+").replaceAll(",", "");
			pageToAsk = "https://scholar.google.com/scholar?q=\""+requestSideTitle+"\"";
		}

		RobotManager.clickOnChrome();
		RobotManager.writeAddressAndLoadPage(pageToAsk);
		fullText = RobotManager.getFullPage();
		RobotManager.closePage();


		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(cacheFile));
			writer.write(fullText);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(fullText.contains("we can't verify that you're not a robot when JavaScript is turned off")
				|| fullText.contains("Our systems have detected unusual traffic from your computer network"))
			throw new Error("Busted that we are a robot!");
		return fullText;
	}

	private static boolean scholarRawInputToPDF(Reference r, String fullText) {
		boolean found = fullText.contains(".pdf") || fullText.contains("PDF");
		if(!found)return false;

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
					try {
						return new URI(current).toURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
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

							if(!isAllowedProvider(current)) return null;
							try {
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

		return downloadPdfFromSetOfLinks(links, r);
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
		
		
		
		throw new Error();
	}

	private static String traditionalWebPageContents(String webpage) {
		webpage = webpage.replaceAll(" ", "%20");
		try {
			// Create URL object
			URL url = new URL(webpage);
			URLConnection conn = url.openConnection();
			InputStreamReader isr = 
					new InputStreamReader(url.openStream()
							);

			BufferedReader readr = 
					new BufferedReader(isr);

			String line = null;
			String res = "";
			while ((line = readr.readLine()) != null) 
			{
				res+=line+"\n";
			}


			readr.close();
			return res;
		}

		// Exceptions
		catch (MalformedURLException mue) {
			System.out.println("Malformed URL Exception raised");
		}
		catch (IOException ie) {
			ie.printStackTrace();
			System.out.println("IOException raised");
		}
		if(webpage.contains("bab.la"))return null;
		throw new Error();
	}

	private static WebClient webClient;
	private synchronized static String getPageContentsWebClient(String webpage) {
		try {
			if(webClient==null)
			{
				webClient = new WebClient();

				webClient.getJavaScriptEngine().shutdown();
				java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
			}

			final HtmlPage page = webClient.getPage(webpage);

			final String pageAsXml = page.asXml();

			//  final String pageAsText = page.asText();
			return pageAsXml;
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
			throw new Error();
		}
	}

	private static boolean downloadPdfWithUnpaywall(Reference r) {
		HttpClient client = HttpClient.newHttpClient();
		//   HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/search/?query="+title+"&is_oa=true&email=unpaywall_00@example.com"))
		HttpRequest request = null;
		URI target = null;
		if(r.getDoi()!=null)
			request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/"+r.getDoi()+"?email=unpaywall_00@example.com"))    	
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


		//   System.out.println(response.body());
		Set<URL> links = Arrays.asList(response.body().toString().split("\n")).stream()
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


		return downloadPdfFromSetOfLinks(links,r);
	}

	public static File getPdfFileFor(Reference r) {
		return Paths
				.get("data/references/"+r.getId()+".pdf")
				.toFile();
	}

	private static File getCachedGoogleScholarFile(Reference r) {

		return Paths.get("data/cache/google_scholar/"+
		r.getId()+".txt"
		).toFile();
	}



	private static boolean downloadPdfFromSetOfLinks(Set<URL>links, Reference r) {
		if(links.isEmpty())return false;

		File outputFile = getPdfFileFor(r);


		boolean found = false;



		for(URL website:links)
		{
			if(PdfToLinksDatabase.hasLinkFailedToLeadToAPdf(website))
				continue;
			if(website.toString().equals("https://www.example.edu/paper.pdf"))continue;
			try {
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());

				FileOutputStream fos = new FileOutputStream(outputFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				found = true;
				fos.close();
				rbc.close();
				if(isValidPDFFileAlreadyThere(r))
				{
					System.out.println("DIRECT DOWNLOAD WORKED");
					return true;
				}
				else getPdfFileFor(r).delete();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("Trying to get the PDF without success");
			} catch (IOException e) {

				if(e.toString().contains("Server returned HTTP response code: 403"))
					System.out.println("Forbidden access, can be tried by hand: "+website);
				else if(e.toString().contains("received handshake warning"))
					System.out.println("received handshake warning, can be tried by hand: "+website);
				else if(e.toString().contains("Server returned HTTP response code: 400"))
					System.out.println("Error 400, probably some character issue. Try getting it by hand: "+website);
				else if(e.toString().contains("javax.net.ssl.SSLException: Connection reset"))
					System.out.println("SSL issue; try getting by hand: "+website);
				else if(e.toString().contains("javax.net.ssl.SSLHandshakeException:"))
					System.out.println("SSL issue; try getting by hand: "+website);
				else if(e.toString().contains("java.net.ConnectException: Connection timed out: connect"))
					continue;
				else if(e.toString().contains("java.io.FileNotFoundException: "+website))
					continue;
				else throw new Error();
				//	System.out.println("Trying to get the PDF without success");
			}
		}

		for(URL website:links)
		{
			if(PdfToLinksDatabase.hasLinkFailedToLeadToAPdf(website))
				continue;
			if(website.toString().contains("ieeexplore"))continue;
			tryGettingLinkByHand(website, outputFile);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(isValidPDFFileAlreadyThere(r))
				return true;
			getPdfFileFor(r).delete();
			PdfToLinksDatabase.recordAsFailedToGetToAPdf(website);
		}




		return false;
	}

	private static synchronized void tryGettingLinkByHand(URL link, File outputFile) {
		if(outputFile.exists())
			outputFile.delete();

		RobotManager.clickOnChrome();
		RobotManager.writeAddressAndLoadPage(link.toString());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		RobotManager.savePage(outputFile.getParentFile().getAbsolutePath(),outputFile.getName());

		System.out.print("");
	}

	public static boolean isPdfAccessible(Reference x) {
		return downloadPdfFor(x);
	}
}
