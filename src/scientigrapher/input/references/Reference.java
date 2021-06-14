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
	private final Integer year;

	public Reference(String title2, String doi2, int id, Integer year) {
		this.doi = doi2;
		this.title = title2;
		this.id = id;
		this.year = year;
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

	public static Reference newInstance(String title2, String doi2, int id, Integer year) {
		return new Reference(title2,doi2, id, year);
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
		Integer year=null;
		if(entry.replaceAll(" ", "").contains("year={"))
		{
			String sub = entry.replaceAll(" ", "");
			sub = sub.substring(sub.indexOf("year={")+6);
			sub = sub.substring(0,sub.indexOf("}"));
			year = Integer.parseInt(sub);
		}
		else 
			throw new Error();
		
		return Reference.newInstance(title,doi,val,year);
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

	public int getYear() {
		return year;
	}

}
