package lv.ailab.segmenter.datastruct;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    /*public void sortSegByLangs()
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
            List<SegmentationVariant> segmentations,
            Map<String, List<Lexicon.Entry>> foundWords)
    {
        this.original = original;
        this.segmentations = segmentations;
        this.foundWords = foundWords;
    }
    
    public List<String> primaryResult() {
		List<String> res = new LinkedList<String>();
    	if (segmentations.isEmpty()) {
    		res.add(this.original);
    	} else {
        	for (String segment : segmentations.get(0).segments) {
        		res.add(foundWords.get(segment).get(0).lemma);
        	}    		
    	}
		return res;
    }

    /**
     * @return Slightly formatted JSON representation
     */
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
