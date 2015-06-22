package lv.ailab.domainnames;

import lv.ailab.segmenter.LangConst;
import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;
import lv.ailab.segmenter.datastruct.Lexicon.Entry;
import lv.ailab.wordembeddings.WordEmbeddings;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alternative domain name creator.
 * Created on 2015-06-09.
 *
 * @author Lauma, Pēteris
 */
public class AlternativeBuilder
{
    public Lexicon lexicon;
    public Segmenter segmenter;
    public WordEmbeddings wordembeddings_lv; // TODO - generalize
    public WordEmbeddings wordembeddings_en;
    
    
    private WordEmbeddings wordembeddings(String language) throws Exception{
    	if (language.equalsIgnoreCase("lv")) return wordembeddings_lv;
    	if (language.equalsIgnoreCase("en")) return wordembeddings_en;
    	throw new Exception(String.format("Wordembeddings - bad language %s", language));
    }

    /**
     * @param lexiconFiles      array of tuples - first element is file path,
     *                          second element is language stub
     * @param sortByLangChanges flag whether segmenter will use sorting by
     *                          language changes
     * @param embeddingsFileLV  path to 1st embeddings file
     * @param embeddingsFileEN  path to 2nd embeddings file
     * @throws IOException
     */
    public  AlternativeBuilder(
            String[][] lexiconFiles, boolean sortByLangChanges, boolean allowNolang,
            String embeddingsFileLV, String embeddingsFileEN )
    throws Exception
    {
        lexicon = new Lexicon();
        for (String[] lex : lexiconFiles)
        {
            lexicon.addFromFile(lex[0], lex[1]);
        }
        segmenter = new Segmenter(lexicon);
        segmenter.sortByLanguageChanges = sortByLangChanges;
        segmenter.allowNolangSegments = allowNolang;
        wordembeddings_lv = new WordEmbeddings(embeddingsFileLV);
        wordembeddings_lv.addToLexicon(lexicon, "lv");
        wordembeddings_en = new WordEmbeddings(embeddingsFileEN);
        wordembeddings_en.addToLexicon(lexicon, "en");
    }

    /**
     * @param sortByLangChanges flag whether segmenter will use sorting by
     *                          language changes
     * @param embeddingsFileLV  path to 1st embeddings file
     * @param embeddingsFileEN  path to 2nd embeddings file
     * @param lexiconFiles      list where each element is in form
     *                          "lang=lexicon_file"
     * @throws IOException
     */
    public  AlternativeBuilder(
            boolean sortByLangChanges, boolean allowNolang,
            String embeddingsFileLV, String embeddingsFileEN, String ... lexiconFiles)
            throws Exception
    {
        lexicon = new Lexicon();
        for (String lex : lexiconFiles)
        {
            lexicon.addFromFile(lex.substring(lex.indexOf('=') + 1), lex.substring(0, lex.indexOf('=')));
        }
        segmenter = new Segmenter(lexicon);
        segmenter.sortByLanguageChanges = sortByLangChanges;
        segmenter.allowNolangSegments = allowNolang;
        wordembeddings_lv = new WordEmbeddings(embeddingsFileLV);
        wordembeddings_lv.addToLexicon(lexicon, "lv");
        wordembeddings_en = new WordEmbeddings(embeddingsFileEN);
        wordembeddings_en.addToLexicon(lexicon, "en");
    }

    /**
     * Segment the given query and then build alternative names with the help of
     * the word embeddings.
     * @param query input query (domain name)
     * @return  created alternatives (alternative domain names)
     * @throws Exception
     */
    public List<String> buildAlternatives (String query) throws Exception {
    	return buildAlternatives(query, null);
    }
    
    /**
     * Segment the given query and then build alternative names with the help of
     * the word embeddings.
     * @param query input query (domain name)
     * @param limit cap on how many alternatives are allowed
     * @return  created alternatives (alternative domain names)
     * @throws Exception
     */
    public List<String> buildAlternatives (String query, Integer limit) throws Exception
    {
        List<String> result = new ArrayList<>();
        if (query.endsWith(".id")) {
        	// Workaround for .id.lv subdomains
    		List<String> alternatives = buildAlternatives(query.substring(0, query.length() - 3), limit);
    		for (String alternative : alternatives)
    			result.add(alternative + ".id");
    		return result;
    	}
    	
        List<Lexicon.Entry> segments = segmenter.segment(query).primaryResult();
        // Filter out separators.
        segments = segments.stream().filter(a -> !LangConst.SEPARATOR.equals(a.lang)).collect(Collectors.toList());

        if (segments.size() == 1)
        {
            // Option 1 - replace the whole name with possible alternatives
        	Lexicon.Entry segment = segments.get(0);
        	// Non-language segments are kept fixed
        	if (segment.lang.equalsIgnoreCase("lv") || segment.lang.equalsIgnoreCase("en"))
        		result.addAll(wordembeddings(segment.lang).similarWords(segment.lemma, 10));
        } else
        {
            // Option 2 - keep all other segments fixed, replace a single word with alternatives
            for (int i=0; i<segments.size(); i++)
            {
            	Lexicon.Entry segment = segments.get(i);
            	if (!segment.lang.equalsIgnoreCase("lv") && !segment.lang.equalsIgnoreCase("en"))
            		continue;// Non-language segments are kept fixed
            	
                String prefix = segments.stream().limit(i).map(a -> a.originalForm).collect(Collectors.joining("-"));
                String suffix = segments.stream().skip(i+1).map(a -> a.originalForm).collect(Collectors.joining("-"));

                List<String> replacements = wordembeddings(segment.lang).similarWords(segment.lemma, 10);
                for (String replacement : replacements) {
                    String alternative = alternativeForm(replacement, segment);
                    if (!prefix.trim().isEmpty())
                        alternative = prefix + "-" + alternative;
                    if (!suffix.trim().isEmpty())
                        alternative = alternative + "-" + suffix;
                    
                    result.add(alternative);
                }                
            }
        }
        
        if (limit != null && limit > 0) {
        	result = result.stream().limit(limit).collect(Collectors.toList());
        }
        return result;
    }

    public static String alternativeForm(String replacement, Entry segment) {
		if (!segment.lang.equalsIgnoreCase("lv")) return replacement;
		
		System.out.println(replacement);
		System.out.println(segment.toString());
		
		return null;
	}

	/**
     * Utility method for formatting the list of alternatives as a CSV line (no
     * line endings added).
     */
    public static String resultToCsv(List<String> alternatives)
    {
        return "\"" + String.join("\",\"", alternatives) + "\"";
    }

    /**
     * Utility method for formatting the list of alternatives as a JSON array.
     */
    public static String resultToJson(List<String> alternatives)
    {
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

    public static void main(String[] args)
    throws Exception
    {
        if (args == null || args.length < 4)
        {
            System.err.println("To generate alternative names for every line in a file, provide following");
            System.err.println("arguments:");
            System.err.println("\tfile_to_process ouput_file word_embeddings_file_lv word_embeddings_file_en lang1=wordlist1 lang2=wordlist2");
            return;
        }
        AlternativeBuilder ab = new AlternativeBuilder(
                true, true, args[2], args[3], Arrays.copyOfRange(args, 4, args.length));

        System.err.println("Processing file...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
        out.append("\"Domēns\",\"Varianti\"\n");
        String readLine = in.readLine();
        int count = 0;
        while (readLine != null)
        {
            count++;
            out.append("\"" + readLine + "\",");
            out.append(resultToCsv(ab.buildAlternatives(readLine)));
            if (count % 100 == 0) System.err.print(count + " processed.\r");
            readLine = in.readLine();
            if (readLine != null) out.newLine();
        }
        System.err.println(count + " processed. Done.");
        in.close();
        out.flush();
        out.close();


    }

}
