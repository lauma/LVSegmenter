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

public class SegmentResource extends ServerResource{
	@Get("json")
	public String retrieve() throws Exception {  
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return DomainServer.segmenter.segment(query).toJSON();
	}

}
