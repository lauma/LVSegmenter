package lv.ailab.segmenter.datastruct;

import lv.ailab.segmenter.LangConst;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container object representing Segmenter's results.
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
    public List<? extends SegmentationVariant> segmentations;
    /**
     * All valid "words" (accepted as word by lexicon or regexp) that are
     * found given string.
     */
    public Map<String, List<Lexicon.Entry>> foundWords;

    /**
     * This indicates if somewhere in the segmentation process beam size
     * restriction have been applied.
     */
    public boolean approximatedResult;

    public SegmentationResult(String original,
            List<SegmentationVariant> segmentations,
            Map<String, List<Lexicon.Entry>> foundWords,
            boolean approximatedResult)
    {
        this.original = original;
        this.segmentations = segmentations;
        this.foundWords = foundWords;
        this.approximatedResult = approximatedResult;
    }

    /**
     * Sort segmentations by length.
     */
    public void sortSegmentations()
    {
        segmentations.sort(Comparator.<SegmentationVariant>naturalOrder());
                //((o1, o2) -> new Integer(o1.segments.size()).compareTo(o2.segments.size())));
    }

    /**
     * Convinience method - get list of corresponding lexicon enstries for the
     * shortest segmentation. Or get original string, if no segmentation.
     * @return  List of Lexicon Entries - one for each element of the first
     *          segmentation.
     */
    public List<Lexicon.Entry> primaryResult()
    {
		List<Lexicon.Entry> res = new LinkedList<>();
        sortSegmentations();
    	if (segmentations.isEmpty())
            res.add(new Lexicon.Entry(this.original, this.original, LangConst.NOLANG));
        else res.addAll(segmentations.get(0).segments.stream()
                .map(segment -> foundWords.get(segment).get(0))
                .collect(Collectors.toList()));
		return res;
    }
    
    /**
     * Convinience method - get space separated string representation of the
     * primaryResult().
     */
    public String primaryResultString()
    {
    	List<String> res = primaryResult().stream().map(entry -> entry.lemma)
                .collect(Collectors.toList());
        return String.join(" ", res);
    }

    /**
     * @return Slightly formatted JSON representation
     */
    public String toJSON()
    {
        StringBuilder res = new StringBuilder();
        res.append("{\n\t\"String\":\"");
        res.append(original);
        res.append("\",\n\t\"VariantCountReductionUsed\":\"");
        res.append(approximatedResult);
        res.append("\",\n\t\"SegmentationVariants\":[");
        sortSegmentations();
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
                res.append("\", \"Form\":\"");
                res.append(info.originalForm);
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
