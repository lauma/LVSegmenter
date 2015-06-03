package lv.ailab.segmenter.webservice;

import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;

import org.restlet.*;
import org.restlet.data.*;

public class DomainServer {
	static Lexicon lexicon;
	static Segmenter segmenter;	
	static private int port = 8182;

	public static void main(String[] args) throws Exception {
	    String WORDLIST_FILE_LV = "wordlist-lv.txt";
	    String WORDLIST_FILE_EN = "wordsEn-sil.txt";
	    boolean SORT_BY_LANG_CHANGES = true;

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-port")) {
				if (i+1 < args.length && !args[i+1].startsWith("-")) {
					try {
						port = Integer.parseInt(args[i+1]);
						i++;
					} catch (Exception e) {
						System.err.printf("Error when parsing command line parameter '%s %s'\n",args[i], args[i+1]);
						System.err.println(e.getMessage());
						System.exit(64); //EX_USAGE flag according to sysexit.h 'standard'
					}
				}
			}
			
			if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-?")) {
				System.out.println("Webservice for domain name alternative generator");
				System.out.println("\nCommand line options:");
				System.out.println("\t-port 1234 : sets the web server port to some other number than the default 8182");
				System.out.println("\nWebservice access:");
				System.out.println("http://localhost:8182/analyze/[word] : morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
				System.out.flush();
				System.exit(0);
			}
		} // for .. arguments
        
		lexicon = new Lexicon();
		lexicon.addFromFile(WORDLIST_FILE_LV, "lv");
		lexicon.addFromFile(WORDLIST_FILE_EN, "en");
        segmenter = new Segmenter (lexicon);
        segmenter.sortByLanguageChanges = SORT_BY_LANG_CHANGES;
        
        // Create a new Restlet component and add a HTTP server connector to it 
	    Component component = new Component();  
	    component.getServers().add(Protocol.HTTP, port);  
	    
	    // Then attach it to the local host 
	    component.getDefaultHost().attach("/{domainname}", DomainNameResource.class);
	    
	    component.start();
	}

}
