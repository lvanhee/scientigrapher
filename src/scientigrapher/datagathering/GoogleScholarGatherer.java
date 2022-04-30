package scientigrapher.datagathering;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cachingutils.SplittedFileBasedCache;
import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;
import webscrapping.RobotBasedPageReader;

public class GoogleScholarGatherer {

	public static final SplittedFileBasedCache<Reference, String> googleReferenceManagerCache = 
			SplittedFileBasedCache.newInstance(r->new File(ProgramwideParameters.GOOGLE_SCHOLAR_ENTRY_FOLDER.getAbsolutePath()+"/"+r.getId()+".txt"), Function.identity(), Function.identity());

	
	public static String getHtmlOfGoogleScholarReferencePage(Reference r) {
		if(GoogleScholarGatherer.googleReferenceManagerCache.has(r)) return GoogleScholarGatherer.googleReferenceManagerCache.get(r);


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


		fullText = RobotBasedPageReader.getFullPageAsHtml(pageToAsk,1);

		if(r.getDoi()!=null && fullText.contains("did not match any articles."))
		{
			String requestSideTitle = r.getTitle().replaceAll(" ", "+").replaceAll(",", "");
			pageToAsk = "https://scholar.google.com/scholar?q=\""+requestSideTitle+"\"";
			fullText = RobotBasedPageReader.getFullPageAsHtml(pageToAsk,1);
		}

		if(fullText.contains("we can't verify that you're not a robot when JavaScript is turned off")
				||fullText.contains("Prouvez-\r\n" + 
						"nous\r\n" + 
						"que vous n'êtes pas une machine\r\n" + 
						"")
				|| fullText.contains("Our systems have detected unusual traffic from your computer network")
				||fullText.contains("<title>Sorry...</title>"))
			throw new Error("Busted that we are a robot!");

		fullText = fullText.replaceAll("<br>\n", "<br>");

		if(!fullText.startsWith("\r\n" + 
				"<!doctype html>"))
			throw new Error("Failed loading google scholar page");
		GoogleScholarGatherer.googleReferenceManagerCache.add(r, fullText);

		return fullText;
	}

	public static int getNumberOfCitationsFor(Reference r)
	{
		String pageToSearch = getHtmlOfGoogleScholarReferencePage(r);
		List<String> lines = Arrays.asList(pageToSearch.split("\n")).stream().collect(Collectors.toList());
		while(!lines.get(0).contains("Cite</span>"))
			lines = lines.subList(1, lines.size());
		String currentLine = lines.get(0);
		String title = r.getTitle();
		if(title.startsWith("Viewpoint:")) title = title.substring(10);
		title = title.toLowerCase().replaceAll("[^a-zA-Z]", "");
		String pressedCurrentLine = currentLine.toLowerCase().replaceAll("[^a-zA-Z]", "");
		if(!pressedCurrentLine.contains(title))
		{
			System.out.println(pressedCurrentLine);
			throw new Error();
		}
		//	int indexFirstCite = pageToSearch.indexOf("Cite</span>");

		if(!pageToSearch.contains("Cited by"))return 0;
		String citedBy = pageToSearch.substring(pageToSearch.indexOf("Cited by"));
		citedBy = citedBy.substring(9,citedBy.indexOf("<"));

		return Integer.parseInt(citedBy);

	}

	public static boolean isInGoogleScholar(Reference x) {
		return GoogleScholarGatherer.googleReferenceManagerCache.has(x);
	}

}
