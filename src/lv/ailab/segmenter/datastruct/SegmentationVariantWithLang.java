package lv.ailab.segmenter.datastruct;

import lv.ailab.segmenter.LangConst;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Segmentation variant augmented with possible languages for each segment.
 * Natural comparison order might not be consistent with equals() method.
 * Created on 2015-05-12.
 *
 * @author Lauma
 */
public class SegmentationVariantWithLang extends SegmentationVariant
{
    /**
     * Languages for respective segments.
     */
    protected LinkedList<LanguageSequence> langs = new LinkedList<LanguageSequence>()
            {{add(new LanguageSequence());}};

    /**
     * Memorized characteristics for currently best language sequence. Do not
     * use this pointer to access Stats object dirctly, use
     * getBestLangSeqStats() instead.
     */
    protected LanguageSequence.Stats bestLangSeqStats = null;

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
        bestLangSeqStats = null;
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
     * Get Stats object for the "best" language sequences according to
     * LanguageSequence.CountComparator.
     */
    public LanguageSequence.Stats getBestLangSeqStats()
    {
        if (bestLangSeqStats == null)
            bestLangSeqStats = langs.stream().min(LanguageSequence.CountComparator.get()).get().stats;
        return bestLangSeqStats;
    }

    /**
     * Get all language sequences that are as good as the "best" one.
     */
    public List<LanguageSequence> getAllBestLangSeq()
    {
        LanguageSequence.Stats best = getBestLangSeqStats();
        return langs.stream().filter(variant -> variant.stats.equals(best))
                .collect(Collectors.toList());
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * Comparison is done first by Stats object, then by segment count.
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o)
    {
        SegmentationVariantWithLang osv = (SegmentationVariantWithLang) o;
        int comp = getBestLangSeqStats().compareTo(osv.getBestLangSeqStats());
        if (comp != 0) return comp;
        if (segments.size() < osv.segments.size()) return -1;
        if (segments.size() > osv.segments.size()) return 1;
        return 0;
    }
}
