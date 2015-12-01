package lv.ailab.wordembeddings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lv.ailab.segmenter.datastruct.Lexicon;

public class Synonyms {
	private Map<String, List<String>> synonyms = new HashMap<String, List<String>>();
	
	public Synonyms(String filename) throws Exception {
		System.err.println(String.format("Loading synonyms from %s", filename));
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        String line = in.readLine();
        while (line != null) {
            String[] parts = line.split("-",2);            
            if (parts.length != 2) {
            	System.err.printf("Gļukaini dati sinonīmos rindā\n%s\n", line);
            	line = in.readLine();
            	continue;
            }
            String word = parts[0].trim();
            List<String> synset = new LinkedList<String>();
            for (String semicolon_part : parts[1].split(";")) {
            	for (String comma_part : semicolon_part.split(",")) {
            		if (!comma_part.trim().contains(" "))
            			synset.add(comma_part.trim());
                }
            }
            
            synonyms.put(word, synset);
            line = in.readLine();
        }
        in.close();
	}
	
	public List<String> similarWords(String word) throws Exception {
		List<String> result = synonyms.get(word);
		if (result == null) result = new LinkedList<String>();
		return result;
	}
	
	public void addToLexicon(Lexicon lex, String language){
		for (Entry<String, List<String>> entry : synonyms.entrySet()) {
			lex.addWord(entry.getKey(), entry.getKey(), language);
			for (String synonym : entry.getValue())
				lex.addWord(synonym, synonym, language);
		}
	}

}
