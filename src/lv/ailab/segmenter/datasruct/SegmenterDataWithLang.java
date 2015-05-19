package lv.ailab.segmenter.datasruct;

import java.util.stream.Collectors;

/**
 * Segmenter data augmented with possible languages for each segmentation
 * variant.
 * Created on 2015-05-12.
 *
 * @author Lauma
 */
public class SegmenterDataWithLang extends SegmenterData
{
    public SegmenterDataWithLang (String data)
    {
        super(data);
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
            SegmentationVariantWithLang newVariant = new SegmentationVariantWithLang();
            newVariant.addNext(word, foundWords.get(word)
                    .stream().map(entry -> entry.lang).collect(Collectors.toSet()));
            memorizedVariants.get(to).add(newVariant);
        }
        else for (SegmentationVariant variant: memorizedVariants.get(from))
        {
            SegmentationVariantWithLang newVariant =
                    ((SegmentationVariantWithLang)variant).makeNext(
                            word, foundWords.get(word)
                                    .stream().map(entry -> entry.lang).collect(Collectors.toSet()));
            memorizedVariants.get(to).add(newVariant);
        }
    }

    /**
     * @return Segmentation results: full length segmentations and found words.
     */
    public SegmentationResultWithLang getResult()
    {
        return new SegmentationResultWithLang(
                data,
                memorizedVariants.get(data.length()).stream().map(
                        variant -> (SegmentationVariantWithLang)variant).collect(Collectors.toList()),
                foundWords);
    }
}
