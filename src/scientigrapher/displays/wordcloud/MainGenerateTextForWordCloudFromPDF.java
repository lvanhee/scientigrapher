package scientigrapher.displays.wordcloud;

import java.io.File;
import java.io.IOException;

import scientigrapher.pdfs.PdfReader;
import textprocessing.TextProcessingUtils;

public class MainGenerateTextForWordCloudFromPDF {
	
	public static void main(String[] args) throws IOException
	{
		String fileName = "data/thesis.pdf";

		String outOfFile = PdfReader.getStringContentsOutOfFile(new File(fileName));
		
		outOfFile = outOfFile.replaceAll("\n", " ");
		outOfFile = TextProcessingUtils.purgeAllPunctuation(outOfFile);
		outOfFile = ScientificWordFilter.purgeTermsThatAreNotSpecificToScience(outOfFile);
		
		System.out.println(outOfFile);
	}

}
