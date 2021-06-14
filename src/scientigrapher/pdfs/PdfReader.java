package scientigrapher.pdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import scientigrapher.input.pdfs.ReferenceToPdfGetter;
import scientigrapher.input.references.Reference;

public class PdfReader {

	private static File getCacheFileFrom(File f)
	{
		String fileName = f.getName();
		File parent = f.getParentFile();
		File res = new File(parent+"_cached_string/"+fileName);
		return res;
	}
	private static File getFailedProcessingFile(File x)
	{
		return new File(x.getAbsolutePath()+"_failed");
	}
	public static String getStringContentsOutOfFile(File x) {
		File cacheFile = getCacheFileFrom(x);
		if(cacheFile.exists())
			return (String)importFromFile(cacheFile);
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
		
		exportToFile(cacheFile,parsedText);

		return parsedText;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

	}
	
	private static void exportToFile(File cacheFile, String parsedText) {
		 try {
	            FileOutputStream fileOut = new FileOutputStream(cacheFile);
	            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
	            objectOut.writeObject(parsedText);
	            objectOut.close();
	            fileOut.close();
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	}
	
	private static Object importFromFile(File cacheFile) {
		 try {
			 FileInputStream fi = new FileInputStream(cacheFile);
			 ObjectInputStream oi = new ObjectInputStream(fi);
			 Object res = oi.readObject();
			 oi.close();
			 fi.close();
			 return res;	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		 throw new Error();
	}

	public static Map<Reference, String> getStringContentsPerReference(Set<Reference> refs)
	{
		return refs.stream().collect(Collectors.toMap(Function.identity(), 
				x-> PdfReader.getStringContentsOutOfFile(
		ReferenceToPdfGetter.getPdfFileFor(x))));			
	}

	public static boolean isParsingWorking(File f) {
		if(getCacheFileFrom(f).exists())return true;
		if(getFailedProcessingFile(f).exists())return false;
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
			if(
					e.toString().equals("java.io.IOException: Page tree root must be a dictionary")
					||
			e.toString().equals("java.io.IOException: Missing root object specification in trailer."))
			{
				try {
					getFailedProcessingFile(f).createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new Error();
				}
				return false;
			}
			throw new Error();
		}

	}

	public static Map<Reference, String> getStringContentsFromValidFilesMappedToReferenceFromFile(String fileName) {
		
		Set<Reference> allReferences = Reference.referencesFromBibFile(fileName);
		
		allReferences = allReferences.parallelStream()
				.filter(x->ReferenceToPdfGetter.isPdfAccessible(x))
				.collect(Collectors.toSet());
		
		return getStringContentsPerReference(allReferences);
	}


}
