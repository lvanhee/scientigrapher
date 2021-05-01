package scientigrapher.pdfs;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfReader {

	
	public static String getStringContentsOutOfFile(File x) throws IOException {
		PDFParser parser = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		PDFTextStripper pdfStripper;

		parser = new PDFParser(new RandomAccessBufferedFileInputStream(x));
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();
		pdDoc = new PDDocument(cosDoc);
		String parsedText = pdfStripper.getText(pdDoc);
		parsedText = parsedText.replaceAll(""+(char)13, " ");
	//	parsedText = parsedText.replaceAll("\n", "");
		while(parsedText.contains("  ")) parsedText = parsedText.replaceAll("  ", " ");

		return parsedText;

	}


}
