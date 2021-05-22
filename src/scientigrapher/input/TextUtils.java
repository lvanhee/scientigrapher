package scientigrapher.input;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextUtils {

	public static Map<List<String>, Integer> getNGrams(String parsedText, int nb) {
		Map<List<String>, Integer> allNgrams = new HashMap<>();
		
		List<String> lastFew = new LinkedList<>();
		for(String s: parsedText.split(" "))
		{
			lastFew.add(s);
			if(lastFew.size()==nb)
			{
				List<String> copy = lastFew.stream().collect(Collectors.toList());
				if(!allNgrams.containsKey(copy))
					allNgrams.put(copy, 0);
				allNgrams.put(copy, allNgrams.get(copy)+1);
				lastFew.remove(0);				
			}
		}
		return allNgrams;
	}
	
}
