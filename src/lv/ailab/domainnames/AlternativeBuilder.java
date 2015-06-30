package lv.ailab.domainnames;

import lv.ailab.segmenter.LangConst;
import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;
import lv.ailab.segmenter.datastruct.Lexicon.Entry;
import lv.ailab.wordembeddings.Synonyms;
import lv.ailab.wordembeddings.WordEmbeddings;
import lv.lumii.expressions.Expression;
import lv.lumii.expressions.Expression.Category;
import lv.lumii.expressions.ExpressionWord;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

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
    private static transient AbstractSequenceClassifier<CoreLabel> morphoClassifier = null;
    protected static transient Analyzer analyzer = null;
    private Synonyms synonyms;
        
    private WordEmbeddings wordembeddings(String language) throws Exception{
    	if (language.equalsIgnoreCase("lv")) return wordembeddings_lv;
    	if (language.equalsIgnoreCase("en")) return wordembeddings_en;
    	throw new Exception(String.format("Wordembeddings - bad language %s", language));
    }

    // code that ensures that we have a morpholexicon and tagger available, and also can import a pre-loaded tagger to avoid duplication
	public static void initClassifier(String model) throws Exception {
		morphoClassifier = CMMClassifier.getClassifier(new File(model));		
		analyzer = LVMorphologyReaderAndWriter.getAnalyzer(); // Assumption - that the morphology model actually loads the LVMorphologyReaderAndWriter data, so it should be filled.
	}
	
	public static void setClassifier(AbstractSequenceClassifier<CoreLabel> preloadedClassifier) {
		morphoClassifier = preloadedClassifier;
		analyzer = LVMorphologyReaderAndWriter.getAnalyzer(); // Assumption - that the morphology model actually loads the LVMorphologyReaderAndWriter data, so it should be filled.
	}
	
	public static void initClassifier() throws Exception {
		setClassifier(Expression.morphoClassifier);
	}
	
	public static Analyzer getAnalyzer() throws Exception {
		if (analyzer == null) initClassifier();
		return analyzer;
	}
	
	public static AbstractSequenceClassifier<CoreLabel> getClassifier() throws Exception {
		if (morphoClassifier == null) initClassifier();
		return morphoClassifier;
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
            String embeddingsFileLV, String embeddingsFileEN, String synonymsFile )
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
		synonyms = new Synonyms(synonymsFile);
		synonyms.addToLexicon(lexicon, "lv");
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
        
        // form a sentence and tag it
        String sentence = segments.stream().map(a -> a.originalForm).collect(Collectors.joining(" "));
        Expression expression = new Expression(sentence, "other", true, false);
        
        if (segments.size() == 1)
        {
            // Option 1 - replace the whole name with possible alternatives
        	Lexicon.Entry segment = segments.get(0);
        	// Non-language segments are kept fixed
        	if (segment.lang.equalsIgnoreCase("lv") || segment.lang.equalsIgnoreCase("en"))
        		result.addAll(wordembeddings(segment.lang).similarWords(segment.lemma, 10));
        	if (segment.lang.equalsIgnoreCase("lv"))
        		result.addAll(synonyms.similarWords(segment.lemma));
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
                
                String lemma = segment.lemma;
                if (segment.lang.equalsIgnoreCase("lv")) lemma = lemma.toLowerCase();

                List<String> replacements = wordembeddings(segment.lang).similarWords(lemma, 10);
                if (segment.lang.equalsIgnoreCase("lv")) replacements.addAll(synonyms.similarWords(lemma));
                for (String replacement : replacements) {
                    String alternative = alternativeForm(replacement, segment, expression.expWords.get(i));
                    if (alternative == null) continue;
                    
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

    public static String alternativeForm(String replacement, Entry segment, ExpressionWord expressionWord) throws Exception {
		if (!segment.lang.equalsIgnoreCase("lv")) return replacement;
		
		Wordform wf = getAnalyzer().analyzeLemma(replacement).getBestWordform();
		if (wf == null) {
			System.err.printf("Nesanāca pamatforma vārdam %s\n", replacement);
			wf = getAnalyzer().analyze(replacement).getBestWordform();
		}
		if (wf == null) {
			System.err.printf("Nu %s laikam vispār nav vārds\n", replacement);
			return null;
		}
		
		if (expressionWord.correctWordform.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) &&
			!wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun)) {
			System.err.printf("%s nav lietvārds tā kā %s - izlaižam!\n", replacement, segment.originalForm);
			return null;
		}		
		
		String result = Expression.inflectWord(wf, expressionWord.correctWordform.getValue(AttributeNames.i_Case), expressionWord.correctWordform, Category.other, true);
																													// FIXME - te padod šo te vārdu f-jai, kas gaida frāzes pēdējo vārdu; teorētiski neko šai gadījumā nemaina bet varbūt ir slikti
//		System.out.printf("Lokām %s kā %s / %s - sanāca %s\n", replacement, segment.originalForm, expressionWord.correctWordform.getTag(), result);
//		System.out.flush();
		
		return result.trim(); // FIXME - inflectWord pieliek lieku atstarpi beigās, būtu drīzāk tur jālabo
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
