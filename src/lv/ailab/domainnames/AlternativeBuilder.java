package lv.ailab.domainnames;

import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;
import lv.ailab.wordembeddings.WordEmbeddings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public WordEmbeddings wordembeddings;

    /**
     * @param lexiconFiles      array of tuples - first element is file path,
     *                          second element is language stub
     * @param sortByLangChanges flag whether segmenter will use sorting by
     *                          language changes
     * @param embeddingsFile    path to embeddings file
     * @throws IOException
     */
    public  AlternativeBuilder(
            String[][] lexiconFiles, boolean sortByLangChanges,
            String embeddingsFile)
    throws Exception
    {
        lexicon = new Lexicon();
        for (String[] lex : lexiconFiles)
        {
            lexicon.addFromFile(lex[0], lex[1]);
        }
        segmenter = new Segmenter(lexicon);
        segmenter.sortByLanguageChanges = sortByLangChanges;
        wordembeddings = new WordEmbeddings(embeddingsFile);
        wordembeddings.addToLexicon(lexicon);

    }

    /**
     * Segment the given query and then build alternative names with the help of
     * the word embeddings.
     * @param query input query (domain name)
     * @return  created alternatives (alternative domain names)
     * @throws Exception
     */
    public List<String> buildAlternatives (String query) throws Exception
    {
        List<Lexicon.Entry> segments = segmenter.segment(query).primaryResult();
        List<String> result = new ArrayList<>();

        if (segments.size() == 1)
        {
            // Option 1 - replace the whole name with possible alternatives
            result.addAll(wordembeddings.similarWords(segments.get(0).lemma, 10));
        } else
        {
            // Option 2 - keep all other segments fixed, replace a single word with alternatives
            for (int i=0; i<segments.size(); i++)
            {
                String prefix = "";
                for (int j=0; j<i; j++)
                    prefix = prefix + " " + segments.get(j).originalForm;

                String suffix = "";
                for (int j=i+1; j<segments.size(); j++)
                    suffix = suffix + " " + segments.get(j).originalForm;

                List<String> replacements = wordembeddings.similarWords(segments.get(i).lemma, 10);
                for (String replacement : replacements)
                    result.add(prefix.trim() + replacement + suffix.trim());
            }
        }
        return result;
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

}
