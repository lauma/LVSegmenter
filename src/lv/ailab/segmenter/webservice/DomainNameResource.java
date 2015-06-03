package lv.ailab.segmenter.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DomainNameResource extends ServerResource{
	@Get("json")
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return DomainServer.segmenter.segment(query).toJSON();
	}

}
