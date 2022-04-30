package scientigrapher.output;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import scientigrapher.input.references.Reference;
import scientigrapher.pdfs.PdfReader;

public class PapersFeaturingAConcept {
	
	public static void main(String[] args) {
		
		Set<Reference> allReferences = Reference.referencesFromBibFile(new File(args[0]));
		
		Map<Reference,String> allInput = PdfReader.getStringContentsFromValidFilesMappedToReferenceFromFile(new File(args[0]));
		String concept = args[1];
		
		Set<Reference> filteredReferences = 
				allInput.keySet().stream().filter(x->
				x.getTitle().contains(concept)||
				countOccurences(allInput.get(x), concept)>5)
				.collect(Collectors.toSet());
		
		Set<Reference> sortedFilteredReferences = new TreeSet<>((x,y)->Integer.compare(x.getId(),y.getId()));
		sortedFilteredReferences.addAll(filteredReferences);
		sortedFilteredReferences.addAll(allReferences.stream()
				.filter(x->x.getTitle().contains(concept))
				.collect(Collectors.toSet()));
		
		for(Reference r: sortedFilteredReferences)
			System.out.println(r);

	}

	private static int countOccurences(
			String largeString, String smallString) {
		int total = 0;
		String currentString = largeString;
		while(currentString.contains(smallString))
		{
			total++;
			currentString = currentString.substring(currentString.indexOf(smallString)+1);
		}
		return total;
	}


}
