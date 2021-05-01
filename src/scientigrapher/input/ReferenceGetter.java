package scientigrapher.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ReferenceGetter {
	

    public static final String POSTS_API_URL = "https://api.unpaywall.org/v2/search";

    public static void main(String[] args) throws IOException, InterruptedException {
    	
    	String entries = Files.readString(Paths.get("data/scopus.bib"));
    	
    	List<String> failedOnes = new ArrayList<>();
    	
    	int total = entries.split("@").length;
    	AtomicInteger successes = new AtomicInteger();
    	AtomicInteger currentDone = new AtomicInteger();
    	
    	Arrays.asList(entries.split("@")).parallelStream().forEach(
    	entry ->{
    		if(!entry.contains("doi")&&!entry.contains("title")) {
    			failedOnes.add(entry); return;}

    		currentDone.incrementAndGet();
    		String title = entry.substring(entry.indexOf("title=")+7);
    		title = title.substring(0,title.indexOf("}"));
    		String doi = null;
    		if(entry.contains("doi")) {
    			doi = entry.substring(entry.indexOf("doi=")+5);
    			doi = doi.substring(0,doi.indexOf("}"));
    		}
    		boolean result = downloadPdfFor(title, doi);
    		if(result)successes.incrementAndGet();
    		
    		System.out.println("Progress:"+ currentDone+"/"+total +
    				" success:"+successes+"/"+currentDone);
    	});
    	/*String title = "Role-assignment in open agent societies";
    	String DOI = "10.1007/s10676-020-09572-w";
    	
    	
    	downloadPdfFor(title, DOI);*/
    	
    	System.out.println("Failed:"+failedOnes);
    	
    	
    }

	public static boolean downloadPdfFor(String title, String DOI) {
    	HttpClient client = HttpClient.newHttpClient();
    	//   HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/search/?query="+title+"&is_oa=true&email=unpaywall_00@example.com"))
    	HttpRequest request = null;
    	if(DOI!=null)
    		request = HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/"+DOI+"?email=unpaywall_00@example.com"))    	
                .build();
    	else request = 
    			HttpRequest.newBuilder(URI.create("https://api.unpaywall.org/v2/search/?query="+title.replaceAll(" ", "%20")+"&is_oa=true&email=unpaywall_00@example.com")).build();
        HttpResponse<String> response=null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("DOI not found");
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new Error("DOI not found");
		}
        
    
     //   System.out.println(response.body());
        Set<String> links = Arrays.asList(response.body().toString().split("\n")).stream().filter(x->x.contains("url_for_pdf")).map(x->x.substring(x.indexOf(":")+2)
        		.replaceAll("\"", "").replaceAll(",", ""))
        		.collect(Collectors.toSet()); 
        

        System.out.println(links);
        // parse JSON
  /*      ObjectMapper mapper = new ObjectMapper();
        List<Post> posts = mapper.readValue(response.body(), new TypeReference<List<Post>>() {});

        // posts.forEach(post -> {
        //     System.out.println(post.toString());
        // });
        posts.forEach(System.out::println);*/

        System.out.println("Downloading reference for "+title+" "+DOI);
        boolean found = false;
        for(String s:links)
        {
        	URL website;
        	try {
        		website = new URL(s);
        		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        		
        		String fileName = null;
        		if(DOI!=null)fileName =DOI;
        		else fileName="NODOI"+title;
        		FileOutputStream fos = new FileOutputStream("data/references/"+fileName.replaceAll("/", "!").replaceAll(":","")+".pdf");
        		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        		found = true; break;
        	} catch (MalformedURLException e) {
        		e.printStackTrace();
        		System.out.println("Trying to get the PDF without success");
        	} catch (IOException e) {
        		/*if(!e.getCause().toString().contains("Server returned HTTP response code: 400"))
        			e.printStackTrace();*/
        		System.out.println("Trying to get the PDF without success");
        	}
        }
        
        if(!found)System.err.println("Reference not found for:"+title+" "+DOI);
        return found;
	}
}
