package scientigrapher.datagathering.mains;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cachingutils.advanced.failable.SuccessfulOutcome;
import cachingutils.advanced.localdatabase.AutofillLocalDatabase;
import cachingutils.advanced.localdatabase.LocalDatabaseImpl;
import cachingutils.impl.SplittedFileBasedCache;
import scientigrapher.datagathering.AuthorDescription;
import scientigrapher.datagathering.ScopusSearcher;
import scientigrapher.input.references.Reference;
import scientigrapher.model.InstitutionIdentifier;
import scientigrapher.model.metrics.AsjcInterdisciplinaryScore;
import webscrapping.WebpageReader;
import webscrapping.WebpageReader.PageContentsResult;

public class UmuProfiler {
	
	private static Path PATH_DATABASE = Paths.get("../databases/taiga_db/all_papers_institution.txt");
					
	
	public static void main(String[] args) throws IOException {
		/*URL staffUrl = new URL("https://www.umu.se/en/staff/");
		PageContentsResult staffWebpage = WebpageReader.getWebclientWebPageContents(staffUrl);
		String suitedPartOfStaff = staffWebpage.getString().substring(staffWebpage.getString().indexOf("<a class=\"orglink\""));
		suitedPartOfStaff = suitedPartOfStaff.substring(0,suitedPartOfStaff.indexOf("</section>"));
		
		Set<URL> allUrlsInStaffPage = WebpageReader.getAllUrlsFrom(suitedPartOfStaff, staffUrl);
		
		Set<String> allPeople = new HashSet<>();
		for(URL u: allUrlsInStaffPage)
		{
			String departmentWebPage = WebpageReader.getWebclientWebPageContents(u).getString();
			List<String> allNamesCurrentDepartment = new ArrayList<>();
			allNamesCurrentDepartment.addAll(Arrays.asList(departmentWebPage.split("Click to send an email to ")));
			allNamesCurrentDepartment.remove(0);
			allNamesCurrentDepartment = allNamesCurrentDepartment.stream().map(x->x.substring(0,x.indexOf("\""))).collect(Collectors.toList());
			allPeople.addAll(allNamesCurrentDepartment);
		}
		
		System.out.println(allPeople);*/
		
		long scopusAffiliationId = 60031040;
		
		List<String> allPapersFromAffiliation = null;
		if(PATH_DATABASE.toFile().exists())
		{
			String input = Files.readString(PATH_DATABASE);
			allPapersFromAffiliation = Arrays.asList(input.split(",")).stream().map(x->x.trim()).collect(Collectors.toList());
		}
		else {
			allPapersFromAffiliation = ScopusSearcher.getAllPapersIdFrom(InstitutionIdentifier.newInstance(scopusAffiliationId),Optional.of(2020));
			String toWrite = allPapersFromAffiliation.toString().substring(1);
			toWrite = toWrite.substring(0,toWrite.length()-1);
			Files.writeString(PATH_DATABASE,toWrite);
		}
		
		System.out.println(allPapersFromAffiliation.size());
		System.out.println(allPapersFromAffiliation.stream().collect(Collectors.toSet()).size());
		
		AtomicInteger total = new AtomicInteger();
		
		allPapersFromAffiliation.parallelStream().forEach(l->
		{
			//if(l<500000000||l==343853205) {skipped++; continue;}
			Reference r = ScopusSearcher.getReferenceFromPaperId(l);
			System.out.println(total.incrementAndGet()+" "+r+" "+AsjcInterdisciplinaryScore.getIntermediateScores(r.getAsjcCodes()));
		});
		
		Set<String> allAuthors = allPapersFromAffiliation.parallelStream().map(x->ScopusSearcher.getReferenceFromPaperId(x).getAuthorScopusIdList())
		//.map(x->x.getAuthorScopusIdList())
		.reduce(ConcurrentHashMap.newKeySet(), (x,y)->{x.addAll(y); return x;});
		
		AtomicInteger totalAuthorsProcessed = new AtomicInteger();
		AtomicInteger totalAffiliations = new AtomicInteger();
		allAuthors.stream().forEach(x->{
			AuthorDescription ad = ScopusSearcher.getAuthorDescriptionFromId(x);
			int totalAffiliated = totalAffiliations.get();
			if(ad.getCurrentAffiliationCodes().contains("60031040"))
				totalAffiliations.incrementAndGet();
			System.out.println(totalAffiliated+" "+totalAuthorsProcessed.incrementAndGet()+"/"+allAuthors.size()+" "+ScopusSearcher.getAuthorDescriptionFromId(x));}
					);
		Set<AuthorDescription> allAuthorsExtended = allAuthors.stream().map(x->ScopusSearcher.getAuthorDescriptionFromId(x)).collect(Collectors.toSet());

		System.out.println("Number of authors who ever wrote a paper tied to umu: "+ allAuthors.size());
		

		

		//System.out.println(allPapersFromAffiliation);
	}
}
