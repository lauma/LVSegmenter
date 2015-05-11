package lv.ailab.segmenter;

import java.util.*;

/**
 * Object for representing Segmenter's results.
 */
public class SegmentationResult
{
    /**
     * Original string.
     */
    public String original;
    /**
     * Fond segmentation variants for given string.
     */
    public ArrayList<SegmentationVariant> segmentations;
    /**
     * All valid "words" (accepted as word by lexicon or regexp) that are
     * found given string.
     */
    public HashMap<String, List<Lexicon.Entry>> foundWords;

/*    public void sortSegByLangs()
    {
        segmentations.sort(new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2)
            {
                HashSet<String>
                return 0;
            }
        });
    }*/

    public SegmentationResult(String original,
            ArrayList<SegmentationVariant> segmentations,
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
        for (SegmentationVariant variant : segmentations)
        {
            res.append("\n\t\t");
            res.append(variant.toJSONSegmentList());
            res.append(",");
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
