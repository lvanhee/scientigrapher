package scientigrapher.datagathering.mains;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import cachingutils.Cache;
import cachingutils.FileBasedStringSetCache;
import cachingutils.TextFileBasedCache;
import scientigrapher.input.ProgramwideParameters;

public class LinksToPdfsDatabase {

	private static final Cache<URL, Boolean> urlSuccess =
			TextFileBasedCache.newInstance
			(ProgramwideParameters.WORKING_PDF_URL_DATABASE, 
					x->x.toString(), 
					x->{
						try {
							return new URL(x);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							throw new Error();
						}
					}, 
					x->x.toString(), 
					Boolean::parseBoolean,
					//null,
					"\t");
		

	public static boolean hasLinkFailedToLeadToAPdf(URL website) {
		return urlSuccess.has(website) && !urlSuccess.get(website);
	}

	public static synchronized void recordAsFailedToGetToAPdf(URL website) {
		urlSuccess.add(website, false);
	}

}
