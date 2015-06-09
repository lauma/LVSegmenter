package lv.ailab.domainnames.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import lv.ailab.domainnames.AlternativeBuilder;
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
		
		List<String> alternatives = DomainServer.alternatives.buildAlternatives(query);

		return AlternativeBuilder.resultToJson(alternatives);
	}

}
