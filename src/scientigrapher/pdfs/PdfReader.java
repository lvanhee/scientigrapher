package scientigrapher.pdfs;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import scientigrapher.input.pdfs.ReferenceToPdfGetter;
import scientigrapher.input.references.Reference;

public class PdfReader {

	
	public static String getStringContentsOutOfFile(File x) {
		PDFParser parser = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		PDFTextStripper pdfStripper;

		
		try {
			parser = new PDFParser(new RandomAccessBufferedFileInputStream(x));
		
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();
		pdDoc = new PDDocument(cosDoc);
		String parsedText = pdfStripper.getText(pdDoc);
		parsedText = parsedText.replaceAll(""+(char)13, " ");
		while(parsedText.contains("  ")) parsedText = parsedText.replaceAll("  ", " ");
		parsedText = parsedText.replaceAll("- \n", "");
		parsedText = parsedText.replaceAll("-\n", "");

		return parsedText;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

	}
	
	public static Map<Reference, String> getStringContentsPerReference(Set<Reference> refs)
	{
		return refs.stream().collect(Collectors.toMap(Function.identity(), 
				x-> PdfReader.getStringContentsOutOfFile(
		ReferenceToPdfGetter.getPdfFileFor(x))));			
	}

	public static boolean isParsingWorking(File f) {
		PDFParser parser = null;		
		try {
			RandomAccessBufferedFileInputStream input =new RandomAccessBufferedFileInputStream(f); 
			parser = new PDFParser(input);
			parser.parse();
			parser.getDocument().close();
			input.close();
			
			return true;
			
		} catch (IOException e) {
			//e.printStackTrace();
			if(e.toString().equals("java.io.IOException: Page tree root must be a dictionary"))
				return false;
			if(e.toString().equals("java.io.IOException: Missing root object specification in trailer."))
				return false;
			throw new Error();
		}

	}


}
