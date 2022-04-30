package scientigrapher.pdfs;

import java.io.File;
import java.io.IOException;

import scientigrapher.input.textprocessing.ScientificWordFilter;
import textprocessing.TextProcessingUtils;

public class PdfToStringMain {
	
	public static void main(String[] args) throws IOException
	{
		String fileName = "data/thesis.pdf";

		String outOfFile = PdfReader.getStringContentsOutOfFile(new File(fileName),"", false);
		
		outOfFile = outOfFile.replaceAll("\n", " ");
		outOfFile = TextProcessingUtils.purgeAllPunctuation(outOfFile);
		outOfFile = ScientificWordFilter.purgeTermsThatAreNotSpecificToScience(outOfFile);
		
		System.out.println(outOfFile);
	}

}
