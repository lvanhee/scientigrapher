package scientigrapher.displays.wordcloud;

import java.io.File;
import java.io.IOException;

import scientigrapher.StringProcessing;
import scientigrapher.pdfs.PdfReader;

public class MainGenerateTextForWordCloudFromPDF {
	
	public static void main(String[] args) throws IOException
	{
		String fileName = "data/thesis.pdf";

		String outOfFile = PdfReader.getStringContentsOutOfFile(new File(fileName));
		
		outOfFile = outOfFile.replaceAll("\n", " ");
		outOfFile = StringProcessing.purgeAllPunctuation(outOfFile);
		outOfFile = WordcloudFilter.purgeTermsThatAreIrrelevantForWordClouds(outOfFile);
		
		System.out.println(outOfFile);
	}

}
