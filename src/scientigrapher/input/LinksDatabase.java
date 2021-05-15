package scientigrapher.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class LinksDatabase {
	private static final File DATABASE_RECORD = Paths.get("data/cache/failed_urls.obj").toFile(); 

	private static final Set<String> failedURLs = new HashSet<>();
	static {
		try {
			 
            FileInputStream fileIn = new FileInputStream(DATABASE_RECORD);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
 
            Object obj = objectIn.readObject();
            objectIn.close();
            failedURLs.addAll((Set)obj);
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		
	}

	public static boolean hasLinkFailedToLeadToAPdf(URL website) {
		/*System.out.println(website);
		System.out.println(failedURLs);
		System.out.println(failedURLs.contains(website.toString()));*/
		return failedURLs.contains(website.toString());
	}

	public static void recordAsFailedToGetToAPdf(URL website) {
		failedURLs.add(website.toString());
		
		try {
			 
            FileOutputStream fileOut = new FileOutputStream(DATABASE_RECORD);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(failedURLs);
            objectOut.close(); 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		
	/*	System.out.println("Added:"+website);
		System.out.println("Failed links:"+failedURLs);
		System.exit(0);*/
	}

}
