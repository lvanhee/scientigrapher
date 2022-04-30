package scientigrapher.displays.conceptPerYear;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import scientigrapher.input.references.Reference;
import scientigrapher.input.textprocessing.ScientificWordFilter;
import scientigrapher.pdfs.PdfReader;
import textprocessing.BagOfWords;

public class ConceptPerYearMain {
	
	public static void main(String[] args)
	{
		String fileName = "data/scopus.bib";
		Map<Reference,String> m = PdfReader.getStringContentsFromValidFilesMappedToReferenceFromFile(new File(fileName));
		
		Set<Integer> years = new TreeSet<>(m.keySet().stream().map(x->x.getYear()).collect(Collectors.toSet()));
		
		String allText = m.values().stream().reduce("", (x,y)->x+"\n"+y);
		
		BagOfWords totalBagOfWord = BagOfWords
				.newInstance(allText,
						ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM,true);
		
		List<String> topWords = totalBagOfWord.getWordsSortedByDecreasingNumberOfOccurrences();
		

		
		
		topWords = topWords.subList(0, 10);
		
		for(String s:topWords)
			System.out.println(s+" "+totalBagOfWord.getNbOccurrences(s));

		Map<Integer, BagOfWords> bowPerYear = new HashMap<>();
		
		System.out.println("************** TOP KEYWORDS EVERY YEAR");
		for(Integer i:years)
		{
			bowPerYear.put(i,
					BagOfWords.newInstance(m.keySet().stream().filter(x->x.getYear()==i.intValue())
					.map(x->m.get(x))
					.reduce("", (x,y)->x+"\n"+y),
					ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM, true));
			
			List<String> top = bowPerYear.get(i).getWordsSortedByDecreasingNumberOfOccurrences();
			top = top.subList(0, 10);
			System.out.print(i+"\t");
			for(int t = 0 ; t < 10 ; t ++)
				System.out.print(top.get(t)+"\t"+bowPerYear.get(i).getNbOccurrences(top.get(t))+"\t");
			System.out.println();
		}
		
		
		
		System.out.println("**************");
		System.out.print("year\t");
		for(String s:topWords)
			System.out.print(s+"\t");
		System.out.println();
		
		for(Integer i:bowPerYear.keySet())
		{
			System.out.print(i+"\t");
			for(String s:topWords)
			{
				System.out.print(bowPerYear.get(i).getNbOccurrences(s)+"\t");
			}
			System.out.println();
		}
		
		
		
		System.out.println("**************");
		
		Set<String> mostFrequentOnTop = new HashSet<>();
		for(int i = 0 ; i < 10 &&mostFrequentOnTop.size()<11; i++)
		{
			final int tmp=i;
			mostFrequentOnTop.addAll(bowPerYear.values().stream()
			.map(x->x.getWordsSortedByDecreasingNumberOfOccurrences().get(tmp))
			.collect(Collectors.toSet()));
		}
		
		System.out.print("year\t");
		for(String s:mostFrequentOnTop)
			System.out.print(s+"\t");
		System.out.println();
		
		for(Integer i:bowPerYear.keySet())
		{
			System.out.print(i+"\t");
			for(String s:mostFrequentOnTop)
			{
				System.out.print(bowPerYear.get(i).getNbOccurrences(s)+"\t");
			}
			System.out.println();
		}
				
	}

}
