package lv.ailab.segmenter.datasruct;

import java.util.*;

/**
 * Data object used for storing data while performing segmentation.
 * Created on 2015-05-12.
 * @author Lauma
 */
public class SegmenterData
{
    /**
     * String being segmented.
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
    protected List<List<SegmentationVariant>> memorizedVariants;

    /**
     * Collection of all potential words (and information about them) found
     * anywhere in the string to be segmented.
     */
    protected Map<String, List<Lexicon.Entry>> foundWords;

    public SegmenterData (String data)
    {
        this.data = data;
        dynamicTable = new ArrayList<>(data.length() + 1);
        memorizedVariants = new ArrayList<>(data.length() + 1);
        foundWords = new HashMap<>();

        for (int i = 0; i <= data.length() + 1; i++)
        {
            dynamicTable.add(false);
            memorizedVariants.add(new ArrayList<>());
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
     * Set given index as valid ending/begining word border.
     * @param index index for which future isBegin() calls return true
     */
    public void setBeginValid (int index)
    {
        dynamicTable.set(index, true);
    }

    /**
     * Add new word to collected word map.
     * @param word          word itself
     * @param wordEntries   word describing entries
     */
    public void addWordEntries(String word, List<Lexicon.Entry> wordEntries)
    {
        foundWords.put(word, wordEntries);
    }

    /**
     * From segmentation variants at index "from" constructs segmentation new
     * variants in index "to" (without deleting other entries at index "to").
     * @param from begin index of the segment
     * @param to end index of the segment
     */
    public void makeNextSegmentationVariants(int from, int to)
    {
        String word = data.substring(from, to);
        if (memorizedVariants.get(from).isEmpty())
        {
            SegmentationVariant newVariant = new SegmentationVariant();
            newVariant.addNext(word);
            memorizedVariants.get(to).add(newVariant);
        }
        else for (SegmentationVariant variant: memorizedVariants.get(from))
        {
            SegmentationVariant newVariant = variant.makeNext(word);
            memorizedVariants.get(to).add(newVariant);
        }
    }

    /**
     * @return Segmentation results: full length segmentations and found words.
     */
    public SegmentationResult getResult()
    {
        return new SegmentationResult(data, memorizedVariants.get(data.length()), foundWords);
    }
}
