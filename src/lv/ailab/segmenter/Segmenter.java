package lv.ailab.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Created on 2015-05-08.
 *
 * @author Lauma
 */
public class Segmenter
{
    /**
     * Trie-style data structure containing valid words.
     */
    Lexicon lexicon;
    /**
     * Regular expression representing valid words that are not included in
     * lexicon.
     */
    Pattern validSegment = Pattern.compile("\\d+");

    public Segmenter (Lexicon l)
    throws IOException
    {
        lexicon = l;
    }

    public Results segment(String s)
    {
        // i-th position contains information, if there is a segmentation
        // variant found, ending in this position.
        ArrayList<Boolean> dynamicTable = new ArrayList<>(s.length());
        // i-th position contains segmentation variants for s.substring(0, i)
        ArrayList<ArrayList<ArrayList<String>>> memorizedWords = new ArrayList<>(s.length());
        // in case overall segmentation is not successful, any reasonable
        // substring might be usefull
        HashSet<String> foundWords = new HashSet<>();
        for (int i = 0; i < s.length() + 1; i++)
        {
            dynamicTable.add(false);
            memorizedWords.add(new ArrayList<>());
        }
        dynamicTable.set(0, true);

        for (int end = 1; end <= s.length(); end++)
        {
            for (int begin = 0; begin < end; begin++)
            {
                if (dynamicTable.get(begin))
                {
                    String potWord = s.substring(begin, end);
                    if (lexicon.data.containsKey(potWord) || validSegment.matcher(potWord).matches())
                    {
                        dynamicTable.set(end, true);
                        foundWords.add(potWord);
                        if (memorizedWords.get(begin).isEmpty())
                        {
                            ArrayList<String> newVariant = new ArrayList<>();
                            newVariant.add(potWord);
                            memorizedWords.get(end).add(newVariant);
                        }
                        else for (ArrayList<String> variant: memorizedWords.get(begin))
                        {
                            ArrayList<String> newVariant = (ArrayList<String>) variant.clone();
                            newVariant.add(potWord);
                            memorizedWords.get(end).add(newVariant);
                        }
                    }
                }
            }
        }
        return new Results(s, memorizedWords.get(s.length()), foundWords);
    }

    /**
     * Object for representing Segmenter's results.
     */
    public static class Results
    {
        /**
         * Original string.
         */
        String original;
        /**
         * Fond segmentation variants for given string.
         */
        public ArrayList<ArrayList<String>> segmentations;
        /**
         * All valid "words" (accepted as word by lexicon or regexp) that are
         * found given string.
         */
        public HashSet<String> foundWords;

        public Results (String original,
                ArrayList<ArrayList<String>> segmentations,
                HashSet<String> foundWords)
        {
            this.original = original;
            this.segmentations = segmentations;
            this.foundWords = foundWords;
        }

        public String toJSON()
        {
            StringBuilder res = new StringBuilder();
            res.append("{\n\t\"String\":\"");
            res.append(original);
            res.append("\",\n\t\"SegmentationVariants\":[");
            for (ArrayList<String> variant : segmentations)
            {
                res.append("\n\t\t[");
                for (String word : variant)
                {
                    res.append("\"");
                    res.append(word);
                    res.append("\", ");
                }
                res.delete(res.length()-2, res.length());
                res.append("],");
            }
            res.delete(res.length()-1, res.length());
            res.append("],\n\t\"FoundWords\":[");
            for (String word : foundWords)
            {
                res.append("\"");
                res.append(word);
                res.append("\", ");
            }
            res.delete(res.length()-2, res.length());
            res.append("]\n}");
            return res.toString();
        }
    }
}
