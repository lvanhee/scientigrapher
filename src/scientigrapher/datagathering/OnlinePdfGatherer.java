package scientigrapher.datagathering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cachingutils.impl.FileBasedStringSetCache;
import scientigrapher.datagathering.mains.BibToPdfMain;
import scientigrapher.datagathering.mains.LinksToPdfsDatabase;
import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;
import scientigrapher.pdfs.PdfReader;
import textprocessing.TextProcessingUtils;
import webscrapping.RobotBasedPageReader;

public class OnlinePdfGatherer {
	
	
	public static boolean downloadPdfFromSetOfLinks(Set<URL>links, Reference r) {
		if(links.isEmpty())return false;
	
		File outputFile = BibToPdfMain.getPdfFileFor(r);
	
	
		for(URL website:links)
		{
			if(LinksToPdfsDatabase.hasLinkFailedToLeadToAPdf(website))
				continue;
			if(website.toString().equals("https://www.example.edu/paper.pdf"))continue;
			try {
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	
				FileOutputStream fos = new FileOutputStream(outputFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				rbc.close();
				if(OnlinePdfGatherer.isValidPDFFileAlreadyThere(r))
				{
				//	System.out.println("DIRECT DOWNLOAD WORKED");
					return true;
				}
				else BibToPdfMain.getPdfFileFor(r).delete();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("Trying to get the PDF without success");
			} catch (IOException e) {
	
				if(e.toString().contains("Server returned HTTP response code: 403"))
				{
					System.out.println("Forbidden access, can be tried by hand: "+website);
					continue;
				}
				if(e.toString().contains("java.io.IOException: Server returned HTTP response code: 503 for URL"))
				{
					System.out.println("Forbidden access, can be tried by hand: "+website);
					continue;
				}
				
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
				else if(e.toString().startsWith("java.net.UnknownHostException:"))continue;
				else if(e.toString().startsWith("java.io.FileNotFoundException:"))continue;
				else if(e.toString().startsWith("java.io.IOException: Failed to select a proxy"))continue;
				else throw new Error();
				//	System.out.println("Trying to get the PDF without success");
			}
		}
	
		for(URL website:links)
		{
			if(LinksToPdfsDatabase.hasLinkFailedToLeadToAPdf(website))
				continue;
			
			BibToPdfMain.clearChromeDownloadFolder();
			
			if(website.toString().contains("ieeexplore"))continue;
			RobotBasedPageReader.tryDownloadingPageByHand(website, outputFile);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(OnlinePdfGatherer.isValidPDFFileAlreadyThere(r))
			{
				System.out.println("DOWNLOADED BY ROBOT");
				return true;
			}
			else if(ProgramwideParameters.CHROME_DOWNLOAD_FOLDER.listFiles().length>0)
			{
				Set<File> matchingFiles = Arrays.asList(ProgramwideParameters.CHROME_DOWNLOAD_FOLDER.listFiles()).stream()
				.filter(x->x.getName().endsWith(".pdf"))
				.filter(x->isValidFileAlreadyThere(x))
				.filter(x->
				{
					String contents = PdfReader.getStringContentsOutOfFile(x,r.getUniqueId()+"",false).replaceAll("\n", "");
					List<String> bagOfWords = TextProcessingUtils.toListOfWords(contents);
					List<String> bagOfWordsTitle = TextProcessingUtils.toListOfWords(r.getTitle());
					if(Collections.indexOfSubList(bagOfWords, bagOfWordsTitle)>-1)
						return true;
					return false;
					
				})
				.collect(Collectors.toSet());
				
				if(!matchingFiles.isEmpty())
				{
					try {
						Files.copy(matchingFiles.iterator().next().toPath(), outputFile.toPath());
					} catch (IOException e) {
						e.printStackTrace();
						throw new Error();
					}
				//	System.out.println(matchingFiles);
	
					System.out.println("DOWNLOADED BY ROBOT SAVED LOCALLY REPLACED");
					return true;
				}
			}
			BibToPdfMain.getPdfFileFor(r).delete();
			LinksToPdfsDatabase.recordAsFailedToGetToAPdf(website);
		}
	
	
	
	
		return false;
	}

	public static boolean isValidPDFFileAlreadyThere(Reference r) {
		return isValidFileAlreadyThere(BibToPdfMain.getPdfFileFor(r));
	}

	public static boolean downloadPdfFor(Reference r) {
		if(OnlinePdfGatherer.failingLinksToPdfFiles.contains(r.getId()))
			return false;
		if(BibToPdfMain.isFailedToGet(r))return false;
		if(isValidPDFFileAlreadyThere(r))
			return true;
	
		boolean found = BibToPdfMain.attemptDownloadingPdfWithUnpaywall(r);
		if(!found)found = OnlinePdfGatherer.downloadPDFFromScholarLinks(r);
		
		if(!found)
			OnlinePdfGatherer.failingLinksToPdfFiles.add(r.getId());
		
		return found;
	
	}

	public static boolean isValidFileAlreadyThere(File f) {
		if(!f.exists())return false;
		if(f.getName().endsWith("cache"))return false;
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
			if(firstLine.isBlank())
				return false;
			throw new Error();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}


	public static synchronized boolean downloadPDFFromScholarLinks(Reference r) {
		if(!GoogleScholarGatherer.isInGoogleScholar(r))
			return false;
		String googleHtml = GoogleScholarGatherer.getHtmlOfGoogleScholarReferencePage(r);
		boolean found = googleHtml.contains(".pdf") || googleHtml.contains("PDF");
		if(!found)return false;
		return downloadPdfFromSetOfLinks(PdfLinksGatherer.getGoogleScholarLinks(r), r);
		
//		return getPDFFromRawInput(r, googleHtml);
	}

	public static FileBasedStringSetCache<Integer> failingLinksToPdfFiles = 
			FileBasedStringSetCache.loadCache(
					ProgramwideParameters.FAILED_TO_LOAD_PDF_BY_ALL_MEANS.toPath(), 
	Integer::parseInt, x->x+"");
	
	

}
