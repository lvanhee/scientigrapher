package scientigrapher.displays.wordcloud;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import scientigrapher.input.pdfs.PdfToLinksDatabase;
import scientigrapher.input.pdfs.ReferenceToPdfGetter;
import scientigrapher.input.references.Reference;
import scientigrapher.pdfs.PdfReader;
import textprocessing.BagOfWords;
import textprocessing.TextProcessingUtils;

public class WordcloudFromReferenceFileMain {
	
	public static void main(String[] args) throws IOException
	{
		String fileName = "data/scopus.bib";
		
		Set<Reference> allReferences = Reference.referencesFromBibFile(fileName);
		
		allReferences = allReferences.parallelStream()
				.filter(x->ReferenceToPdfGetter.isPdfAccessible(x))
				.collect(Collectors.toSet());

		
		Map<Reference, String> pdfs = PdfReader.getStringContentsPerReference(allReferences);
				
		String outOfFile = pdfs.values().parallelStream().reduce("", (x,y)->x+"\n"+y);
		BagOfWords bw = BagOfWords.newInstance(outOfFile, ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM, true);
		
		Files.writeString(Paths.get("data/raw_text_dump.txt"), outOfFile);

		Files.writeString(Paths.get("data/plaintext_purged_wordcloud_representation.txt"), outOfFile);
		
		String wordCloudCsvRepresentation = ScientificWordFilter.getCsvFileFrom(bw);
		
		Files.writeString(Paths.get("data/wordcloud_representation.csv"), wordCloudCsvRepresentation);
		
		
		//System.out.println(outOfFile);
	}

}
