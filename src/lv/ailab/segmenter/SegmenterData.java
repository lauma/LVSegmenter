package lv.ailab.segmenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data object used for storing data while performing segmentation.
 * Created on 2015-05-12.
 * @author Lauma
 */
public class SegmenterData
{
    /**
     * String being segmented
     */
    protected final String data;
    /**
     * i-th position contains information, if there is a segmentation
     * variant found, ending in this position.
     */
    protected List<Boolean> dynamicTable;

    /**
     * i-th position contains segmentation variants for
     * string_to_segment.substring(0, i)
     */
    protected List<List<SegmentationVariant>> memorizedWords;

    /**
     * Collection of all potential words (and information about them) found
     * anywhere in the string to be segmented.
     */
    protected Map<String, List<Lexicon.Entry>> foundWords;

    public SegmenterData (String data)
    {
        this.data = data;
        dynamicTable = new ArrayList<>(data.length());
        memorizedWords = new ArrayList<>(data.length());
        foundWords = new HashMap<>();

        for (int i = 0; i < data.length() + 1; i++)
        {
            dynamicTable.add(false);
            memorizedWords.add(new ArrayList<>());
        }
        dynamicTable.set(0, true);
    }

    /**
     * Checks if given index is valid begining of the new word.
     * @param index index to check (starting with 0)
     * @return does the substring(0, index) has a valid segmentation.
     */
    public boolean isBegin (int index)
    {
        return dynamicTable.get(index);
    }

    /**
     * If the segmenter has found next segment, this function updates data
     * structure accordingly.
     * @param begin begin index of the segment
     * @param end end index of the segment
     * @param wordEntries entries describing word found in this segment
     */
    public void addNextSegment(int begin, int end, List<Lexicon.Entry> wordEntries)
    {
        String word = data.substring(begin, end);
        dynamicTable.set(end, true);
        foundWords.put(word, wordEntries);
        if (memorizedWords.get(begin).isEmpty())
        {
            SegmentationVariant newVariant = new SegmentationVariant();
            newVariant.addNext(word);
            memorizedWords.get(end).add(newVariant);
        }
        else for (SegmentationVariant variant: memorizedWords.get(begin))
        {
            SegmentationVariant newVariant = variant.makeNext(word);
            memorizedWords.get(end).add(newVariant);
        }
    }

    /**
     * @return Segmentation results: full length segmentations and found words.
     */
    public SegmentationResult getResult()
    {
        return new SegmentationResult(data, memorizedWords.get(data.length()), foundWords);
    }
}
