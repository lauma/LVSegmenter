package lv.ailab.segmenter.datastruct;

import lv.ailab.segmenter.LangConst;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Sort segmentations. Best should be first.
     */
    public void sortSegmentations()
    {
        segmentations.sort((o1, o2) -> {
            int comp = ((SegmentationVariantWithLang) o1).getBestLangSeqStats().compareTo(
                    ((SegmentationVariantWithLang) o2).getBestLangSeqStats());
            if (comp == 0)
                comp = new Integer(((SegmentationVariantWithLang) o1).segments.size())
                        .compareTo(((SegmentationVariantWithLang) o2).segments.size());
            return comp;
        });
    }

    /**
     * Convinience method - get list of corresponding lexicon entries for the
     * one of the best segmentations (according to language changes, segment
     * count etc.). Or get original string, if no segmentation.
     * @return  List of Lexicon Entries - one for each element of the first
     *          segmentation.
     */
    public List<Lexicon.Entry> primaryResult()
    {
        List<Lexicon.Entry> res = new LinkedList<>();
        sortSegmentations();
        if (segmentations.isEmpty())
            res.add(new Lexicon.Entry(this.original, this.original, LangConst.NOLANG));
        else
        {
            SegmentationVariantWithLang primRes = (SegmentationVariantWithLang) segmentations.get(0);
            LanguageSequence primResLang = primRes.getAllBestLangSeq().get(0);
            for (int i = 0; i < primRes.segments.size(); i++)
            {
                String segment = primRes.segments.get(i);
                String lang = primResLang.langs.get(i);
                Optional<Lexicon.Entry> matched = foundWords.get(segment).stream()
                        .filter(entry -> entry.lang.equals(lang)).findFirst();
                if (matched.isPresent())
                    res.add(matched.get());
                else
                    throw new IllegalStateException(
                            "Segmentation fragment and language matching error. Probably fatal code flaw!");
            }
        }
        return res;
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
        sortSegmentations();
        for (SegmentationVariant variant : segmentations)
        {
            //int changes = ((SegmentationVariantWithLang)variant).getMinimumLangCount();
            LanguageSequence.Stats bestStats = ((SegmentationVariantWithLang)variant).getBestLangSeqStats();
            List<LanguageSequence> bestLangSeqs = ((SegmentationVariantWithLang)variant).getAllBestLangSeq();
            res.append("\n\t\t{\n\t\t\t\"Segmentation\":");
            res.append(variant.toJSONSegmentList());
            res.append("\n\t\t\t\"LanguageChanges\":");
            res.append(bestStats.differences);
            res.append("\n\t\t\t\"RegexpSegments\":");
            res.append(bestStats.regExpCount);
            res.append("\n\t\t\t\"SeparatorSegments\":");
            res.append(bestStats.separatorCount);
            res.append("\n\t\t\t\"OOVSegments\":");
            res.append(bestStats.nolangCount);
            res.append("\n\t\t\t\"Languages\":[");
            for (LanguageSequence seq : bestLangSeqs)
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
