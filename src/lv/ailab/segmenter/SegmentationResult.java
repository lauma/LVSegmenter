package lv.ailab.segmenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Object for representing Segmenter's results.
 */
public class SegmentationResult
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
    public HashMap<String, List<Lexicon.Entry>> foundWords;

    public SegmentationResult(String original,
            ArrayList<ArrayList<String>> segmentations,
            HashMap<String, List<Lexicon.Entry>> foundWords)
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
            res.append("\":[");
            for (Lexicon.Entry info : foundWords.get(word))
            {
                res.append("{\"Lemma\":\"");
                res.append(info.lemma);
                res.append("\", \"Source\":\"");
                res.append(info.lang);
                res.append("\"}, ");
            }
            if (res.toString().endsWith(", "))
                res.delete(res.length()-2, res.length());
            res.append("],");
        }
        if (res.toString().endsWith(","))
            res.delete(res.length()-1, res.length());
        res.append("]\n}");
        return res.toString();
    }
}
