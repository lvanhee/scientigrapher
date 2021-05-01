package scientigrapher.pdfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.pdfbox.io.RandomAccessRead;


public class MainExtractPDF {

	public static void main(String args[]) throws IOException {
		String fileName = "data/thesis.pdf";

		String outOfFile = PdfReader.getStringContentsOutOfFile(new File(fileName));
		System.out.println(outOfFile);
	}

	private static Map<String, Map<String, Integer>> bigramAssociationCounter(String string) {
		Map<String, Map<String, Integer>> map = new HashMap<>();
		String[] t = string.split(" ");
		String prev = ".";
		for(int i = 0 ; i < t.length ; i++)
		{
			String current = t[i].toLowerCase();
			if(!map.containsKey(prev)) map.put(prev, new HashMap<>());
			if(!map.get(prev).containsKey(current))map.get(prev).put(current, 0);
			map.get(prev).put(current, map.get(prev).get(current)+1);
			prev = current;
		}

		return map;
	}

	private static String removePunctuation(String string) {
		return string        		
				.replaceAll("\n", "")
				.replaceAll("\\)","").replaceAll(",", "");
	}

	private static String getStringContentsOutOfFolder(String folderName) {
		File folder = new File(folderName);

		return Arrays.asList(folder.listFiles()).stream().map(x->{
			try {
				return PdfReader.getStringContentsOutOfFile(x);
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		}).reduce("", (x,y)-> x+"\n\n"+y);

	}

}
