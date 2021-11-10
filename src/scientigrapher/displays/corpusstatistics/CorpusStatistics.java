package scientigrapher.displays.corpusstatistics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;
import scientigrapher.pdfs.PdfReader;
import textprocessing.TextProcessingUtils;

public class CorpusStatistics {
	public static void main(String[] args)
	{
		Set<Reference> allRefs = Reference.referencesFromBibFile(ProgramwideParameters.REFERENCE_BIB_FILE);
		Map<Reference, String> refToPdf = PdfReader.getStringContentsFromValidFilesMappedToReferenceFromFile(ProgramwideParameters.REFERENCE_BIB_FILE); 
		Set<Reference> allRefsWithPDF = refToPdf.keySet();

		int size = allRefs.size();

		Set<Integer> years = new TreeSet<>(allRefs.stream().map(x->x.getYear()).collect(Collectors.toSet()));
		Set<Integer> yearsWithPDF = new TreeSet<>(allRefsWithPDF.stream().map(x->x.getYear()).collect(Collectors.toSet()));


		System.out.println("**************");
		System.out.println("year\tall references\tall references with PDF");

		for(Integer i:years)
			System.out.println(i+"\t"+allRefs.stream().filter(x->x.getYear()==i.intValue()).count()
					+"\t"+allRefsWithPDF.stream().filter(x->x.getYear()==i.intValue()).count());


		System.out.println("\n\n**************");
		System.out.println("year\tnumber of characters\tnumber of words\t"
				+ "number of unique words (per PDF)\t"
				+ "number of unique words (per year)\t"
				+ "number of character per document\t"
				+ "number of character per word\t"
				+ "number of words per sentence\t"
				+ "nomber of character per sentence");

		for(Integer i:years)
		{
			Set<Reference> referencesForThisYear = 
					allRefsWithPDF.stream().filter(x->x.getYear()==i.intValue()).collect(Collectors.toSet());
			
			int nbOfCharacters = referencesForThisYear.stream()
					.map(x->TextProcessingUtils.toListOfWords(
							refToPdf.get(x)).stream().map(y->y.length()).reduce(0,Integer::sum))
					.reduce(0, (x,y)->x+y);
			int nbOfPoints = referencesForThisYear.stream()
					.map(x->TextProcessingUtils.countOccurrences(refToPdf.get(x),'.'))
					.reduce(0, Integer::sum);
			
			int nbOfWords = referencesForThisYear.stream()
					.map(x->TextProcessingUtils.toListOfWords(refToPdf.get(x)).size())
					.reduce(0, Integer::sum);
			
			double nbOfCharsPerPoint = ((double)nbOfCharacters)/nbOfPoints;
			
			double nbOfWordsPerPoint = ((double)nbOfWords)/nbOfPoints;
			double nbOfCharPerWord = ((double)nbOfCharacters)/nbOfWords;
			
			System.out.println(
					i+"\t"+
							nbOfCharacters				
					+"\t"+
					nbOfWords
					+"\t"+
					referencesForThisYear.stream()
					.map(x->TextProcessingUtils.toListOfWords(refToPdf.get(x)).stream().collect(Collectors.toSet()).size())
					.reduce(0, Integer::sum)
					+"\t"+
					referencesForThisYear.stream()
					.map(x->TextProcessingUtils.toListOfWords(refToPdf.get(x)).stream().collect(Collectors.toSet()))
					.reduce(new HashSet<>(), (x,y)->{x.addAll(y); return x;}).size()
					+"\t"+
					((double)nbOfCharacters / referencesForThisYear.size())
					+"\t"+
					nbOfCharPerWord
					+"\t"+
					nbOfWordsPerPoint
					+"\t"+
					nbOfCharsPerPoint
					);
		}

		System.out.println();



	}
}
