package lv.ailab.segmenter.datasruct;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Segmentation variant augmented with possible languages for each segment.
 * Created on 2015-05-12.
 *
 * @author Lauma
 */
public class SegmentationVariantWithLang extends SegmentationVariant
{
    /**
     * Languages for respective segments.
     */
    //protected LinkedList<HashSet<String>> langs = new LinkedList<>();
    protected LinkedList<LanguageSequence> langs = new LinkedList<LanguageSequence>()
            {{add(new LanguageSequence(new LinkedList<String>(), 0));}};

    /**
     * Add next segment with given language(-s) and updates possible language
     * combinations.
     * @param nextSegment   segment to add
     * @param segmentLangs  languages for this segment
     */
    public void addNext(String nextSegment, Set<String> segmentLangs)
    {
        super.addNext(nextSegment);
        LinkedList<LanguageSequence> nextResults = new LinkedList<>();
        for (LanguageSequence variant : langs)
            for (String lang : segmentLangs)
                nextResults.add(variant.makeNext(lang));
        langs = nextResults;
    }

    /**
     * Adding segment with no languages is not supported for this class.
     * @param segment   segment to add
     * @throws UnsupportedOperationException    always
     */
    public void addNext(String segment)
    {
        throw new UnsupportedOperationException(
                "To add a new segment, language map must be provided.");
    }

    /**
     * Create new segmentation variant by cloning this variant and adding one
     * more segment.
     * @param nextSegment   segment to add for the newly created segmentation
     *                      variant
     * @param langs         languages for new segment
     * @return  new segmentation variant
     */
    public SegmentationVariantWithLang makeNext(String nextSegment, Set<String> langs)
    {
        SegmentationVariantWithLang res = new SegmentationVariantWithLang();
        res.segments = (LinkedList<String>)this.segments.clone();
        res.langs = (LinkedList<LanguageSequence>) this.langs.clone();
        res.addNext(nextSegment, langs);
        return res;
    }

    /**
     * Creating new segmentation variant by cloning this variant and adding one
     * more segment with no language is not supported for this class.
     * @param nextSegment   segment to add for the newly created segmentation
     *                      variant
     * @throws UnsupportedOperationException    always
     */
    public SegmentationVariantWithLang makeNext(String nextSegment)
    {
        throw new UnsupportedOperationException(
                "To add a new segment, language map must be provided.");
    }

    /**
     * @return  minimal count of language changes (following languages have
     *          different languages)
     */
    public int getMinimumLangCount()
    {
        return langs.stream().map(variant -> variant.differences).min(Comparator
                .naturalOrder()).get();
    }

    /**
     * @param changeCount   how many language changes language sequence must
     *                      contain to be included in result
     * @return  language sequences with exactly given language change counts
     */
    public List<LanguageSequence> getLangSequencesByChangeCount(int changeCount)
    {
        return langs.stream().filter(variant -> variant.differences == changeCount)
               .collect(Collectors.toList());
    }

}
