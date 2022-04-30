package scientigrapher.displays.wordcloud;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import scientigrapher.datagathering.mains.BibToPdfMain;
import scientigrapher.datagathering.mains.LinksToPdfsDatabase;
import scientigrapher.input.ProgramwideParameters;
import scientigrapher.input.references.Reference;
import scientigrapher.input.textprocessing.ScientificWordFilter;
import scientigrapher.pdfs.PdfReader;
import textprocessing.BagOfWords;
import textprocessing.TextProcessingUtils;

public class WordcloudFromReferenceFileMain {
	
	public static void main(String[] args) throws IOException
	{		
		Set<Reference> allReferences = Reference.referencesFromBibFile(ProgramwideParameters.REFERENCE_BIB_FILE);
		
		allReferences = allReferences.parallelStream()
				.filter(x->BibToPdfMain.isPdfAccessible(x))
				.collect(Collectors.toSet());

		
		Map<Reference, String> pdfs = PdfReader.getStringContentsPerReference(allReferences);
				
		String outOfFile = pdfs.values().parallelStream().reduce("", (x,y)->x+"\n"+y);
		
		BagOfWords bw = BagOfWords.newInstance(outOfFile, ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM, true);
		
		Files.writeString(Paths.get("data/raw_text_dump.txt"), outOfFile);

		Files.writeString(Paths.get("data/plaintext_purged_wordcloud_representation.txt"), outOfFile);
		
		String wordCloudCsvRepresentation = WordcloudGenerator.getCsvFileFrom(bw);
		
		Files.writeString(Paths.get("data/wordcloud_representation.csv"), wordCloudCsvRepresentation);
		
		List<String> listOfWords = TextProcessingUtils.toListOfWords(outOfFile);
		listOfWords = listOfWords.stream().filter(x->ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM.test(x)).collect(Collectors.toList());
		
		Map<List<String>, Integer> bigrams = TextProcessingUtils.getNGrams(listOfWords, 2);
		
		String res = bigrams.keySet().stream().sorted((x,y)->-Integer.compare(bigrams.get(x), bigrams.get(y))).map(x->new StringBuilder(x+","+bigrams.get(x))).reduce(
				new StringBuilder(),
				(x,y)->x.append("\n").append(y)).toString();
		
		Files.writeString(Paths.get("data/all_bigrams.txt"), res );
	//	System.out.println(TextProcessingUtils.getNGrams(outOfFile, 2));
		
		
		//System.out.println(outOfFile);
	}

}
