package scientigrapher.pdfs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.pdfbox.io.RandomAccessRead;


public class MainExtractPDF {

	public static void main(String args[]) throws IOException {
		File inputFile = new File("C:\\Users\\loisv\\Desktop\\data_troll");
		String outputFileName = "all_troll_text.txt";
		
		
		
		if(inputFile.isDirectory())
		{
			Map<File, String> allContents = getStringContentsOutOfFolder(inputFile);
			
			String concat = "id,paper_text"+allContents.keySet().stream().map(x->x.getName()+","+allContents.get(x).replaceAll(",", "").replaceAll("\n", "")).reduce("", (x,y)->x+"\n"+y);
			String translatedConcat = new String(concat.getBytes(), StandardCharsets.UTF_8);
			
			Files.writeString(Paths.get(outputFileName), translatedConcat, StandardCharsets.UTF_8);

		}
		else
		{
			String outOfFile = PdfReader.getStringContentsOutOfFile(inputFile, inputFile.getName(),false);
			System.out.println(outOfFile);
		}
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

	private static Map<File,String> getStringContentsOutOfFolder(File folder) {

		return Arrays.asList(folder.listFiles())
				.stream()
				.collect(Collectors.toMap(Function.identity(), 
						x->
				PdfReader.getStringContentsOutOfFile(x,x.getName(),false)));

	}

}
