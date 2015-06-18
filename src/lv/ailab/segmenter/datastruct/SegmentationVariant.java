package lv.ailab.segmenter.datastruct;

import java.util.LinkedList;

/**
 * Single segmentation variant. Basically it is a wrapped list of strings. At
 * least for now.
 * Created on 2015-05-11.
 * @author Lauma
 */
public class SegmentationVariant //implements Cloneable
{
    /**
     * Ordered listing of segments this segmentation variant consists of.
     */
    protected LinkedList<String> segments = new LinkedList<>();

    /**
     * Add next segment.
     * @param segment   segment to add
     */
    public void addNext(String segment)
    {
        segments.add(segment);
    }

    /**
     * Create new segmentation variant by cloning this variant and adding one
     * more segment.
     * @param nextSegment   segment to add for the newly created segmentation
     *                      variant
     * @return  new segmentation variant
     */
    public SegmentationVariant makeNext(String nextSegment)
    {
        SegmentationVariant res = new SegmentationVariant();
        res.segments = (LinkedList<String>)this.segments.clone();
        return res;
    }

    /**
     * Forms JSON list from all segments in this segmentation variant.
     */
    public String toJSONSegmentList()
    {
        StringBuilder res = new StringBuilder("[");
        for (String word : segments)
        {
            res.append("\"");
            res.append(word);
            res.append("\", ");
        }
        if (res.toString().endsWith(", "))
            res.delete(res.length()-2, res.length());
        res.append("]");
        return res.toString();
    }
}
