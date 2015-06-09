package lv.ailab.domainnames.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lv.ailab.domainnames.AlternativeBuilder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class SegmentResource extends ServerResource{
	@Get("json")
	public String retrieve() throws Exception {  
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return DomainServer.alternatives.segmenter.segment(query).toJSON();
	}

}
