package scientigrapher.datagathering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cachingutils.parsing.ParsingUtils;

public class AuthorDescription {
	private static final String ITEM_SEPARATOR = "|||";
	private static final String KEY_TO_VALUE_SEPARATOR = "=";
	
	private static final String SCOPUS_KEY="scopus-id";
	private static final String CURRENT_AFFILIATION_KEY="current_affiliations_id";
	private final Map<String, String> input;

	
	
	public AuthorDescription(Map<String, String> stringMap) {
		if(!stringMap.containsKey(SCOPUS_KEY)) throw new Error();
		this.input = stringMap;
	}
	
	public int hashCode() {return input.hashCode();}
	public boolean equals(Object o) {
		return ((AuthorDescription)o).input.equals(input);
	}


	public static AuthorDescription fromParsableString(String s)
	{
		String updatedSplitter = ITEM_SEPARATOR.replaceAll("\\|", "\\\\|");
		return newInstance(ParsingUtils.toStringMap(s,updatedSplitter,"="));
	}


	public static AuthorDescription newInstance(Map<String, String> stringMap) {
		return new AuthorDescription(stringMap);
	}

	public static String toParsableString(AuthorDescription ad)
	{
		return ParsingUtils.mapToParseableString(ad.input, ITEM_SEPARATOR, KEY_TO_VALUE_SEPARATOR);
	}
	
	public String toString()
	{
		return getScopusId()+" "+getReadableName()+" "+input;
	}

	public String getReadableName() {
		return input.get("given-name")+" "+input.get("surname");
	}

	public String getScopusId() {
		return input.get(SCOPUS_KEY);
	}

	public Set<String> getCurrentAffiliationCodes() {
		if(!input.containsKey(CURRENT_AFFILIATION_KEY))
			return new HashSet<>();
		return Arrays.asList(input.get(CURRENT_AFFILIATION_KEY).split(",")).stream().collect(Collectors.toSet());
	}

	public Map<String, Integer> getPublicationCategories() {
		return input.keySet().stream().filter(x->x.startsWith("category_publication")).collect(Collectors.toMap(x->x.substring(x.indexOf(":")+1), x->Integer.parseInt(input.get(x))));
	}

}
