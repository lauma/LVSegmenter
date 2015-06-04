package lv.ailab.segmenter.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DomainNameResource extends ServerResource{
	@Get("json")
	public String retrieve() throws Exception {  
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		List<String> alternatives = buildAlternatives(DomainServer.segmenter.segment(query).primaryResult());
		
		return jsonFormat(alternatives);
	}

	private String jsonFormat(List<String> alternatives) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		Iterator<String> i = alternatives.iterator();
		while (i.hasNext()) {
			String alternative = i.next();
			sb.append(alternative);
			if (i.hasNext()) sb.append(", ");			
		}
		sb.append("]");
		return sb.toString();
	}

	private List<String> buildAlternatives(List<String> segments) throws Exception {		
		List<String> result = new ArrayList<String>();
		
		if (segments.size() == 1) {
			// Option 1 - replace the whole name with possible alternatives
			result.addAll(DomainServer.wordembeddings.similarWords(segments.get(0), 10));
		} else {
			// Option 2 - keep all other segments fixed, replace a single word with alternatives
			for (int i=0; i<segments.size(); i++) {
				String prefix = "";
				for (int j=0; j<i; j++) 
					prefix = prefix + " " + segments.get(j);
				
				String suffix = "";
				for (int j=i+1; j<segments.size(); j++)
					suffix = suffix + " " + segments.get(j);

				List<String> replacements = DomainServer.wordembeddings.similarWords(segments.get(i), 10);
				for (String replacement : replacements)
					result.add(prefix.trim() + replacement + suffix.trim());

			}
		}
		
		return result;
	}

}
