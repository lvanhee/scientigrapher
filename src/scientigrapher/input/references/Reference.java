package scientigrapher.input.references;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Reference {

	private final String doi;
	private final String title;
	private final int id;

	public Reference(String title2, String doi2, int id) {
		this.doi = doi2;
		this.title = title2;
		this.id = id;
	}

	public boolean hasDoi() {
		return doi != null;
	}

	public String getDoi() {
		return doi;
	}

	public String getTitle() {
		return title;
	}

	public static Reference newInstance(String title2, String doi2, int id) {
		return new Reference(title2,doi2, id);
	}

	public static Reference parse(String entry, int val)
	{
		String title = entry.substring(entry.indexOf("title=")+7);
		title = title.substring(0,title.indexOf("}"));
		String doi = null;
		if(entry.contains("doi")) {
			doi = entry.substring(entry.indexOf("doi=")+5);
			doi = doi.substring(0,doi.indexOf("}"));
		}
		return Reference.newInstance(title,doi,val);
	}

	public int getId() {
		return id;
	}

	public String toString()
	{
		return id+" "+doi+" "+title;
	}

	public static Set<Reference> referencesFromBibFile(String fileName){
		String entries;
		try {
			entries = Files.readString(Paths.get(fileName));

			AtomicInteger currentRefNumber = new AtomicInteger();
			return 
					Arrays.asList(entries.split("\n@"))
					.stream()
					.filter(x->
					!x.isEmpty()//&&
					//x.contains("@")
							)
					.map(
							x-> (Reference)
							Reference.parse(x, 
									currentRefNumber.incrementAndGet())
							)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

}
