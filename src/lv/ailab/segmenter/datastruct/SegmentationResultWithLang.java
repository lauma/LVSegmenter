package lv.ailab.segmenter.datastruct;

import java.util.List;
import java.util.Map;

/**
 * Segmentation results augumented with segment language data.
 * Created on 2015-05-12.
 *
 * @author Lauma
 */
public class SegmentationResultWithLang extends SegmentationResult
{
    public SegmentationResultWithLang(String original,
            List<SegmentationVariantWithLang> segmentations,
            Map<String, List<Lexicon.Entry>> foundWords)
    {
        super(original, null, foundWords);
        super.segmentations = segmentations;
    }

    /**
     * @return  slightly formatted JSON representation with segmentation
     *          variants sorted by language changes
     */
    public String toJSON()
    {
        StringBuilder res = new StringBuilder();
        res.append("{\n\t\"String\":\"");
        res.append(original);
        res.append("\",\n\t\"SegmentationVariants\":[");
        segmentations.sort((o1, o2) -> {
            int comp = new Integer(((SegmentationVariantWithLang) o1)
                    .getMinimumLangCount())
                    .compareTo(((SegmentationVariantWithLang) o2).getMinimumLangCount());
            if (comp == 0)
                comp = new Integer(((SegmentationVariantWithLang) o1).segments.size())
                        .compareTo(((SegmentationVariantWithLang) o2).segments.size());
            return comp;
        });
        for (SegmentationVariant variant : segmentations)
        {
            int changes = ((SegmentationVariantWithLang)variant).getMinimumLangCount();
            res.append("\n\t\t{\n\t\t\t\"Segmentation\":");
            res.append(variant.toJSONSegmentList());
            res.append("\n\t\t\t\"LanguageChanges\":");
            res.append(changes);
            res.append("\n\t\t\t\"Languages\":[");
            for (LanguageSequence seq : ((SegmentationVariantWithLang)variant).getLangSequencesByChangeCount(changes))
            {
                res.append("\n\t\t\t\t");
                res.append(seq.toJSONLangList());
                res.append(",");
            }
            if (res.toString().endsWith(","))
                res.delete(res.length()-1, res.length());
            res.append("]},");
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
