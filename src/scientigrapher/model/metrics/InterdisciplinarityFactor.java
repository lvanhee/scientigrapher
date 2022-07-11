package scientigrapher.model.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import scientigrapher.datagathering.AuthorDescription;
import scientigrapher.datagathering.ScopusSearcher;
import scientigrapher.input.references.Reference;

public class InterdisciplinarityFactor {
	
	private final Map<String, Integer> allAsjcSubjectAreas;
	
	public InterdisciplinarityFactor(Map<String, Integer> allCategories) {
		this.allAsjcSubjectAreas = allCategories;
	}

	public static void main(String[] args)
	{

		AuthorDescription ad = ScopusSearcher.getAuthorDescriptionFromId("57204261558");

		//AuthorDescription ad2 = ScopusSearcher.getAuthorDescriptionFromId("6701745605");
		//InterdisciplinarityFactor id = InterdisciplinarityFactor.newInstance(ad);
		//InterdisciplinarityFactor id2 = InterdisciplinarityFactor.newInstance(ad2);

		//Map<String, Map<String,Integer>> allExtendedAreas = id.getAllExtendedSubjectAreas();
		//	Map<String, Map<String,Integer>> allExtendedAreas2 = id2.getAllExtendedSubjectAreas();

		//Reference headPaper = ScopusSearcher.getPaperInformationFromPaperId("85132952628");
		List<String> allReferences = //headPaper.getReferencesIdsCitedByThePaper();
				ScopusSearcher.getAllScopusIdReferencesFromAuthor(ad.getScopusId());
		
		
		
		/*for(String s:allReferences)
		{
			System.out.println();
		}*/
		
		AtomicInteger remaining = new AtomicInteger(allReferences.size());
		
		List<Reference> allCitedPapers = allReferences.stream()
				.map(x->
				{
					Reference res = ScopusSearcher.getReferenceFromPaperId(x);
					List<String> allRefIds = res.getReferencesIdsCitedByThePaper();
					System.out.println("\n"+remaining.decrementAndGet()+" "+ AsjcInterdisciplinaryScore.newInstance(res).getAllIntermediateSubjectAreaCodes()+" "+allRefIds.size());
					List<Reference> allRefs = res.getReferencesCitedByThePaper();
					for(Reference r:allRefs)
						System.out.print(AsjcInterdisciplinaryScore.newInstance(r).getAllIntermediateSubjectAreaCodes());
					return allRefs;
				})
				.reduce(new ArrayList<>(),(x,y)->{x.addAll(y); return x;});
		
		Map<Reference, Long> counts =
			    allCitedPapers.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		
	//	for(Reference r:allCitedPapers)
			


		//List<Reference> refs = headPaper.getReferencesCitedByThePaper();
		

		AsjcInterdisciplinaryScore authorScore = AsjcInterdisciplinaryScore.newInstance(ScopusSearcher.getAllPapersFromAuthor(ad));
		AsjcInterdisciplinaryScore refsScore = AsjcInterdisciplinaryScore.newInstance(allCitedPapers);
		
		System.out.print("\n\t"+ad.getReadableName()+"\tCited in references\tReferences");
		System.out.println(AsjcInterdisciplinaryScore.toExcel(refsScore,authorScore));


		/*System.out.println(headPaper.getAsjcCodes());
		System.out.println(headPaper.getReferencesIdsCitedByThePaper());*/
		
		
	}
	
	

	

	private static String getSortedPresentation(Map<String, Map<String, Integer>> allExtendedAreas) {
		Set<String> sorted = new TreeSet<>((x,y)->{
			int nbX = allExtendedAreas.get(x).values().stream().reduce(0,(z,t)->z+t);
			int nbY = allExtendedAreas.get(y).values().stream().reduce(0,(z,t)->z+t);
			if(nbX==nbY)return x.compareTo(y);
			else return Integer.compare(nbX, nbY);
		}); 
		sorted.addAll(allExtendedAreas.keySet());
		String res = "";
		for(String s:sorted)
		{
			res+=s+" "+allExtendedAreas.get(s).values().stream().reduce(0,(z,t)->z+t)+"\n";


			for(String val:allExtendedAreas.get(s).keySet().stream().sorted((x,y)->
			{
				if(allExtendedAreas.get(s).get(x).equals(allExtendedAreas.get(s).get(y)))
					return x.compareTo(y);
				return Integer.compare(allExtendedAreas.get(s).get(x), allExtendedAreas.get(s).get(y));
			}
					).collect(Collectors.toList()))
				res+="\t"+getExtendedSubjectAreaCode(val)+":"+allExtendedAreas.get(s).get(val)+"\n";
		}
		return res;
	}

	private Map<String, Map<String, Integer>> getAllExtendedSubjectAreas() {
		Map<String, Map<String, Integer>> res = new HashMap<>();

		for(String s:allAsjcSubjectAreas.keySet())
		{
			String globalCode = getGlobalSubjectAreaCode(s);
			if(!res.containsKey(globalCode))
				res.put(globalCode, new HashMap<>());
			res.get(globalCode).put(s, allAsjcSubjectAreas.get(s));
		}
		return res;
	}
	


	

	
	private static InterdisciplinarityFactor newInstance(AuthorDescription ad) {
		Map<String, Integer> allCategories = ad.getPublicationCategories();
		return new InterdisciplinarityFactor(allCategories);
	}
}
