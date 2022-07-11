package scientigrapher.input.restapi;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class Utilities {

	static String unescapeCharacters(String s) {
		
		
		return StringUtils.replaceEach(StringEscapeUtils.unescapeHtml(s),
				        new String[]{"&amp;", "&lt;", "&gt;", "&quot;", "&#x27;", "&#x2F;"},
				        new String[]{"&", "<", ">", "\"", "'", "/"});
	}
}