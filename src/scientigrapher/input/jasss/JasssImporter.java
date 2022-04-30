package scientigrapher.input.jasss;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import cachingutils.Cache;
import cachingutils.FileBasedStringSetCache;
import cachingutils.SplittedFileBasedCache;
import cachingutils.TextFileBasedCache;
import cachingutils.advanced.localdatabase.AutofillLocalDatabase;
import cachingutils.advanced.localdatabase.LocalDatabaseImpl;
import scientigrapher.displays.wordcloud.WordcloudGenerator;
import scientigrapher.input.TextUtils;
import scientigrapher.input.textprocessing.ScientificWordFilter;
import textprocessing.BagOfWords;
import textprocessing.TextProcessingUtils;
import webscrapping.WebpageReader;

public class JasssImporter {
	
	public static void main(String args[]) throws IOException
	{
		
		Function<String, String> nameToAddress = x->
		{
			String[] split = x.split(",");
			return "https://www.jasss.org/"+split[0]+"/"+split[1]+"/"+split[2]+".html";
		};
		
		SplittedFileBasedCache<String, String> cache = SplittedFileBasedCache.getStringToStringCache(new File("../databases/jasss"));
		
		int volume = 1;
		int number = 1;
		int paperIndex = 1;
		
		boolean lastVolumeFound = true;
		boolean lastNumberFound = true;
		boolean lastPaperFound = true;
		
		Map<String, String> htmlPagePerIndex = new HashMap<>();


		while(lastPaperFound)
		{
			String pageToLookAt = indexesToUrl(volume, number, paperIndex);
			String pageContents = null;
			String key = volume+"-"+number+"-"+paperIndex;
			if(cache.has(key))
				pageContents = cache.get(key);
			else
			{
				pageContents = WebpageReader.getWebclientWebPageContents(pageToLookAt).getString();
				cache.add(key, pageContents);
			}
			if(pageContents.contains("<title>JASSS: Error</title>")||pageContents.equals("NO_PAGE_TO_BE_SERVED"))
			{
				//System.out.println("No more papers for this volume and number");
				paperIndex=1;
				number++;
				if(WebpageReader.getWebclientWebPageContents(indexesToUrl(volume, number, paperIndex)).isFailed())
				{
					//System.out.println("No more numbers for this volume either");
					number = 1;
					volume++;
					if(WebpageReader.getWebclientWebPageContents(indexesToUrl(volume, number, paperIndex)).isFailed())
					{
					//	System.out.println("No more volume either");
						break;
					}
				}
			}
			else
			{
				htmlPagePerIndex.put(key, pageContents);
				paperIndex++;
			}
		}
		
		System.out.println("Loaded: "+htmlPagePerIndex.size()+" papers.");
		
		Map<String, String> plainText = htmlPagePerIndex.keySet().stream().collect(Collectors.toMap(Function.identity(), x->Jsoup.parse(htmlPagePerIndex.get(x)).wholeText()));
		Map<String, List<String>> stringPerText = htmlPagePerIndex.keySet().stream().collect(Collectors.toMap(Function.identity(), x->TextProcessingUtils.toListOfWords(plainText.get(x))));
		Map<String, BagOfWords> bagOfWordsPerEntry = plainText.keySet().stream().collect(Collectors.toMap(Function.identity(), x->BagOfWords.newInstance(x, ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM, true)));
		
		List<String> allTextInASingleList = stringPerText.values().stream().reduce(new ArrayList(),(x,y)->{x.addAll(y); return x;});
		
		
		System.out.println("Loading APA words");
		Set<String> allApaWords = Files.readAllLines(Paths.get("../databases/jasss/apa.txt")).stream().collect(Collectors.toSet());
		
		
		exportNumberOfOccurrencesOfWordsInMappedListsOfWords(allApaWords, stringPerText);
		
		
		
		AtomicInteger totalOccurrencesProcessed = new AtomicInteger();
		Map<String, Integer> occurrencesOfApaWords = allApaWords.parallelStream().sorted().collect(Collectors.toMap(Function.identity(), x->
		{
			int res =  TextProcessingUtils.getNumberOfOccurrencesOf(x,allTextInASingleList);
			System.out.println(totalOccurrencesProcessed.incrementAndGet()+" "+x+" "+res);			
			return res;
		}));
		
		Set<String> apaWordsSortedByNumberOfOccurrence = new TreeSet<>((x,y)->{
			if(occurrencesOfApaWords.get(x)!=occurrencesOfApaWords.get(y)) return -Integer.compare(occurrencesOfApaWords.get(x), occurrencesOfApaWords.get(y));
			return x.compareTo(y);
		});
		apaWordsSortedByNumberOfOccurrence.addAll(occurrencesOfApaWords.keySet());
		
		System.out.println("Exporting APA words");
		Files.writeString(Paths.get("../databases/jasss/apa_to_occurrences.csv"), apaWordsSortedByNumberOfOccurrence.stream().map(x->x+" "+occurrencesOfApaWords.get(x)).reduce("", (x,y)->x+"\n"+y));
		System.out.println("Exported!");

		
		System.out.println(bagOfWordsPerEntry.values().iterator().next());
		
		String globalString = plainText.values().stream().reduce("", (x,y)->x+y);
		
		Files.writeString(Paths.get("../databases/jasss/wordcount.csv"), WordcloudGenerator.getCsvFileFrom(BagOfWords.newInstance(globalString, ScientificWordFilter.INTERESTING_SCIENTIFIC_TERM, true)));
		
		System.out.println(globalString.length());
		
	}

	private static void exportNumberOfOccurrencesOfWordsInMappedListsOfWords(Set<String> allApa, Map<String, List<String>>wordListPerKey) throws IOException {
		FileBasedStringSetCache<String> entriesWithZeroMatches = FileBasedStringSetCache.loadCache(new File("../databases/jasss/cache/cache_apa_without_occurrence_in_jasss.txt").toPath(), Function.identity(), Function.identity());
				TextFileBasedCache<List<String>, Integer> cacheApaToOccurrence = 
				TextFileBasedCache.newInstance(new File("../databases/jasss/cache/cache_apa_jasss_intercross.txt"), 
						x->x.get(0)+";"+x.get(1),
						x->Arrays.asList(x.split(";")),
						x->""+x, Integer::parseInt, "\t");
		
		StringBuilder res = new StringBuilder();
		
		AtomicInteger totalWordsTested = new AtomicInteger();
		AtomicInteger totalCrossingAdded = new AtomicInteger();
		Map<String, Map<String,Integer>> totalPerPaperPerApa = allApa.parallelStream().collect(Collectors.toMap(Function.identity(), apa->
		{
			if(entriesWithZeroMatches.contains(apa))return new HashMap<>();
			Map<String, Integer> allNumberOfOccurrenceOfWordsInThePaperForThisEntry =
					wordListPerKey.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), 
							x->
					{
						/*List<String> k = Arrays.asList(apa,x);
						if(!cacheApaToOccurrence.has(k))
						{
							//cacheApaToOccurrence.add(k, TextProcessingUtils.getNumberOfOccurrencesOf(apa,wordListPerKey.get(x)));
							System.out.println("Computed:"+totalWordsTested+" "+totalCrossingAdded.incrementAndGet()+" "+apa+" "+x);
						}
						return cacheApaToOccurrence.get(k);*/
						return  TextProcessingUtils.getNumberOfOccurrencesOf(apa,wordListPerKey.get(x));
					}
					));
			
			Map<String, Integer> filteredOccurrences = allNumberOfOccurrenceOfWordsInThePaperForThisEntry.keySet().stream()
					.filter(x->allNumberOfOccurrenceOfWordsInThePaperForThisEntry.get(x)>0)
					.collect(Collectors.toMap(Function.identity(), y->allNumberOfOccurrenceOfWordsInThePaperForThisEntry.get(y)));
			
			if(filteredOccurrences.isEmpty())
				entriesWithZeroMatches.add(apa);
			
			System.out.println(totalWordsTested.incrementAndGet());

			return filteredOccurrences;
		}));
	

		
		Set<String> sortedApaSet = new TreeSet<>((x,y)->{
			int nbOccurrencesX=totalPerPaperPerApa.get(x).values().stream().reduce(0, (a,b)->a+b);
			int nbOccurrencesY=totalPerPaperPerApa.get(y).values().stream().reduce(0, (a,b)->a+b);
			
			if(nbOccurrencesX== nbOccurrencesY)
				return x.compareTo(y);
			
			return -Integer.compare(nbOccurrencesX,nbOccurrencesY);
			});
		sortedApaSet.addAll(allApa);
		
		for(String apa:sortedApaSet)
		{
			Map<String, Integer> valuesForThisApa = totalPerPaperPerApa.get(apa);
			Set<String> sortedPaperSet = new TreeSet<>(
					(x,y)-> {
						if(valuesForThisApa.get(x)==valuesForThisApa.get(y)) 
							return x.toString().compareTo(y.toString());
						else return -Integer.compare(valuesForThisApa.get(x), valuesForThisApa.get(y));
					}
							);
			
			sortedPaperSet.addAll(valuesForThisApa.keySet().stream().filter(x->valuesForThisApa.get(x)>0).collect(Collectors.toSet()));
			res.append(apa+" "+ totalPerPaperPerApa.get(apa).values().stream().reduce(0, (a,b)->a+b)
			+sortedPaperSet.stream().map(x->"https://www.jasss.org/"+x.replaceAll("-", "/")+".html"+" ->"+valuesForThisApa.get(x)).reduce("", (x,y)-> x+"\t"+y)+"\n");
		}
		
		
		
		Files.writeString(Paths.get("../databases/jasss/results/result_apa_words_to_jasss_papers.txt"),res.toString());
	}

	private static String indexesToUrl(int volume, int number, int paperIndex) {
		return "https://www.jasss.org/"+volume+"/"+number+"/"+paperIndex+".html";
	}

}
