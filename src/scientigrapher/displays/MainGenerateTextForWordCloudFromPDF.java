package scientigrapher.displays;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import scientigrapher.StringProcessing;
import scientigrapher.pdfs.PdfReader;

public class MainGenerateTextForWordCloudFromPDF {
	
	public static void main(String[] args) throws IOException
	{
		String fileName = "data/thesis.pdf";

		String outOfFile = PdfReader.getStringContentsOutOfFile(new File(fileName));
		
		outOfFile = outOfFile.replaceAll("\n", " ");
		outOfFile = StringProcessing.purgeAllPunctuation(outOfFile);
		outOfFile = purgeSymbolsThatAreIrrelevantForWordClouds(outOfFile);
		
		System.out.println(outOfFile);
	}

	private static String purgeSymbolsThatAreIrrelevantForWordClouds(String outOfFile) {
		
		for(String s: Arrays.asList(
				"section","Table","part","Report","Study", "thank","within","less","good","either","might",
				"using", "still","also","overall","presented","others","often","ways","back","long","will","org",
				"without","way","much","whole",
				"make","can",
				"One", "one","two","three",
				"based","many", "ACM","use", "well",
				"used",
				"0","1","2","3","4","5","6","7","8","9",
				"Journal","thus","one", "new","made","org"))
			outOfFile = outOfFile.replaceAll(" "+s+" ", "");
		
		for(String s: Arrays.asList("0","1","2","3","4","5","6","7","8","9"))
			outOfFile = outOfFile.replaceAll(s, "");
		
		return outOfFile;
	}

}
