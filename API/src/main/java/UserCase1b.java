import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;// in play 2.3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class UserCase1b {
	public static ArrayList<String> requestURLs = new ArrayList<String>();
	public static ArrayList<Float> fieldValues = new ArrayList<Float>();
	public static String query = "TestForm";
	public static String queryType = "form";
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
		
		//Create a query for all documents made from a specified form template
		String output1 = Query.makeQuery(uriString());
		
		//Process results
		ObjectMapper mapper = new ObjectMapper();
		JsonNode results = mapper.readTree(output1);
		for(JsonNode document : results.path("documents")) {
			//extract links for documents made from desired form
			String requestURL = document.path("_links").path(0).path("link").asText();
			String output2 = Query.makeQuery(requestURL);
			
			//iterate over all documents extracting data from desired field
			ObjectMapper mapper2 = new ObjectMapper();
			JsonNode results2 = mapper2.readTree(output2);
			JsonNode fieldArray = results2.path("fields");
			String fieldValue = fieldArray.path(1).path("content").asText();
			fieldValues.add(Float.parseFloat(fieldValue));
			System.out.println(fieldValue);
			
			try {
			    Thread.sleep(100);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
		}
	}
	
	//This method builds the URI String from your Query
	public static String uriString() throws URISyntaxException {
		
		String uriString = "";
		
		AdvancedQuery advQuery = new AdvancedQuery(new Query (query, queryType));
	    
	    URIBuilder builder = new URIBuilder()
	    		.setScheme("https")
	            .setHost(Query.setProperties("hostURL"))
	            .setParameter("advancedQuery", advQuery.advancedQuery2JSON());
	    
	    URI uri = builder.build();
	    uriString = uri.toString();
	    
//	    System.out.println(uriString);
	    return uriString;
	    
	}
}
	


