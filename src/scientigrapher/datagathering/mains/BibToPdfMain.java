package scientigrapher.datagathering.mains;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import cachingutils.advanced.StringCacheUtils;
import cachingutils.advanced.failable.AttemptOutcome;
import cachingutils.advanced.failable.SuccessfulOutcome;
import cachingutils.advanced.localdatabase.AutofillLocalDatabase;
import cachingutils.advanced.localdatabase.LocalDatabaseImpl;
import cachingutils.impl.FileBasedStringSetCache;
import cachingutils.impl.TextFileBasedCache;
import scientigrapher.datagathering.GoogleScholarGatherer;
import scientigrapher.datagathering.OnlinePdfGatherer;
import scientigrapher.datagathering.PdfLinksGatherer;
import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BibToPdfMain {

	public static final String POSTS_API_URL = "https://api.unpaywall.org/v2/search";
	private static final File UNPAYWALL_URL_FILE = new File("data/cache/unpaywall/pdf_urls_from_unpaywall.txt");

	public static void main(String[] args) throws IOException, InterruptedException {			
		AutofillLocalDatabase<Reference, Set<URL>> unpaywallUrlDatabase = 
				LocalDatabaseImpl.newInstance(
						TextFileBasedCache.newInstance(
								UNPAYWALL_URL_FILE, 
								(Reference x)->Reference.toParsableString(x,Arrays.asList("|","\t").stream().collect(Collectors.toSet())), 
								(String s)->Reference.fromParsableString(s), 
								(Set<URL> u)->StringCacheUtils.urlSetToString(u),
								(String s)->StringCacheUtils.stringToUrlSet(s), 
								StringCacheUtils.separatorUrlSet()),
						x-> SuccessfulOutcome.newInstance(PdfLinksGatherer.getUnpaywallLinks(x))
						);
		
		List<String> failedOnes = new ArrayList<>();
		
		Set<Reference> allEntries = Reference.referencesFromBibFile(ProgramwideParameters.REFERENCE_BIB_FILE);
		
		int total = allEntries.size();
		
		System.out.println("Processing "+total+" entries");

		allEntries = allEntries.parallelStream().filter(x->!OnlinePdfGatherer.isValidPDFFileAlreadyThere(x)).collect(Collectors.toSet());
		
		AtomicInteger successes = new AtomicInteger();
		AtomicInteger currentDone = new AtomicInteger();

		System.out.println("Loading all links referenced by unpaywall");
		allEntries.stream().sorted((x,y)->Integer.compare(x.getId(), y.getId())).forEach(r->
		{
			System.out.println("Getting unpaywall references for: "+r+unpaywallUrlDatabase.get(r));
			unpaywallUrlDatabase.get(r);});
		
		System.out.println("Trying to load the entries using unpaywall");
		allEntries.stream().sorted((x,y)->Integer.compare(x.getId(), y.getId())).forEach(r->
		attemptDownloadingPdfWithUnpaywall(r));
				
		allEntries = allEntries.stream().filter(x->!OnlinePdfGatherer.isValidPDFFileAlreadyThere(x)).collect(Collectors.toSet());
		
		System.out.println("Attempted downloading entries with Unpaywall. Remaining entries without proper PDF file:"+allEntries.size());
		
		System.out.println("Now loading the metadata information from google scholar");
		
		int totalEntriesRemaining = allEntries.size();
		allEntries.stream()
		.sorted((x,y)->Integer.compare(x.getId(),y.getId()))
		.filter(x->GoogleScholarGatherer.isInGoogleScholar(x))
		.forEach(x->{
		GoogleScholarGatherer
				.getHtmlOfGoogleScholarReferencePage(x);
		System.out.println("Loading metadata from google scholar "+ currentDone.get()+"/"+totalEntriesRemaining+" "+x.getTitle());
		currentDone.incrementAndGet();
		});
		
		currentDone.set(0);
		
		System.out.println("Metadata loaded. Now loading the PDFs");

		allEntries.stream()
		.sorted((x,y)->Integer.compare(x.getId(),y.getId()))
		.forEach(
				r ->{
					System.out.println(r);
					currentDone.incrementAndGet();
					boolean result = OnlinePdfGatherer.downloadPdfFor(r);
					if(result)successes.incrementAndGet();

					System.out.println("Progress:"+ currentDone+"/"+total +
							" success:"+successes+"/"+currentDone);
				});

		System.out.println("Failed due to lack of information:"+failedOnes);
	}

	private static void checkEdgeCases() {
		if(ProgramwideParameters.PDF_FOLDER.exists()||!ProgramwideParameters.PDF_FOLDER.isDirectory()||ProgramwideParameters.PDF_FOLDER.listFiles().length==0)
		{
			throw new Error("No pdf folder available. Run first:"+BibToPdfMain.class);
		}
	}

	
	public static boolean isFailedToGet(Reference r) {
		return 
				getFailedPdfFileFor(r).exists();
	}

	private static File getFailedPdfFileFor(Reference r) {
		File f = getPdfFileFor(r);
		String fileName = f.getName();
		File parent = f.getParentFile();
		File res = new File(parent+"_failed_to_dl/"+fileName);
		return res;
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

	
	private final static FileBasedStringSetCache<Integer> referenceFoundOnUnpaywall = FileBasedStringSetCache
			.loadCache(ProgramwideParameters.UNPAYWALL_FOUND_ENTRIES.toPath(), x->Integer.parseInt(x), (Integer x)->x.toString());
	
	private final static FileBasedStringSetCache<Integer> referenceFailedToBeFoundOnUnpaywall = FileBasedStringSetCache
			.loadCache(ProgramwideParameters.UNPAYWALL_UNFOUND_ENTRIES.toPath(), x->Integer.parseInt(x), (Integer x)->x.toString());
	
	public static boolean attemptDownloadingPdfWithUnpaywall(Reference r) {
		if(referenceFoundOnUnpaywall.contains(r.getId()))return true;
		if(referenceFailedToBeFoundOnUnpaywall.contains(r.getId()))return false;
		
		/*if(r.toString().contains(" emotion: An uncertainty theory of anxiety"))
			System.out.println();*/
		

		//   System.out.println(response.body());
		Set<URL> links = PdfLinksGatherer.getUnpaywallLinks(r);
		
		boolean res = OnlinePdfGatherer.downloadPdfFromSetOfLinks(links,r);
		
		if(res)
			referenceFoundOnUnpaywall.add(r.getId());
		else
			referenceFailedToBeFoundOnUnpaywall.add(r.getId());
		return res;
	}

	public static File getPdfFileFor(Reference r) {
		return Paths
				.get(ProgramwideParameters.PDF_FOLDER.getAbsolutePath()+"/"+r.getId()+".pdf")
				.toFile();
	}

	public static void clearChromeDownloadFolder() {
		if(ProgramwideParameters.CHROME_DOWNLOAD_FOLDER.exists())
			Arrays.asList(ProgramwideParameters.CHROME_DOWNLOAD_FOLDER.listFiles()).stream().forEach(x->x.delete());
		else
			try {
				Files.createDirectories(ProgramwideParameters.CHROME_DOWNLOAD_FOLDER.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
	}

	public static boolean isPdfAccessible(Reference x) {
		return OnlinePdfGatherer.downloadPdfFor(x);
	}
}
