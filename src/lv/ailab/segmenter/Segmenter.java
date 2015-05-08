package lv.ailab.segmenter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
        HashMap<String, Lexicon.Entry> foundWords = new HashMap<>();
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
                    Lexicon.Entry found = lexicon.data.get(potWord);
                    if (found == null && validSegment.matcher(potWord).matches())
                        found = new Lexicon.Entry(potWord, "regexp");
                    if (found != null)
                    {
                        dynamicTable.set(end, true);
                        foundWords.put(potWord, found);
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
     * Segments each line in given file and prints out as JSON.
     * @param inFile path to data file
     * @param outFile path to result file
     */
    public void segmentFile(String inFile, String outFile)
    throws IOException
    {
        System.out.println("Segmenting file...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        String readLine = in.readLine();
        out.append("[\n");
        int count = 0;
        while (readLine != null)
        {
            count++;
            out.append(segment(readLine).toJSON());
            if (count % 1000 == 0) System.out.print(count + " processed.\r");
            readLine = in.readLine();
            if (readLine != null) out.append(",\n");
        }
        out.append("\n]");
        System.out.println(count + " processed. Done.");
        in.close();
        out.flush();
        out.close();
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
        public HashMap<String, Lexicon.Entry> foundWords;

        public Results (String original,
                ArrayList<ArrayList<String>> segmentations,
                HashMap<String, Lexicon.Entry> foundWords)
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
                if (res.toString().endsWith(", "))
                    res.delete(res.length()-2, res.length());
                res.append("],");
            }
            if (res.toString().endsWith(","))
                res.delete(res.length()-1, res.length());
            res.append("],\n\t\"FoundWords\":[");
            for (String word : foundWords.keySet())
            {
                res.append("\n\t\t\"");
                res.append(word);
                res.append("\":{\"Lemma\":\"");
                Lexicon.Entry info = foundWords.get(word);
                res.append(info.lemma);
                res.append("\", \"Source\":\"");
                res.append(info.lang);
                res.append("\"},");
            }
            if (res.toString().endsWith(","))
                res.delete(res.length()-1, res.length());
            res.append("]\n}");
            return res.toString();
        }
    }
}
