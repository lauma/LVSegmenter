package lv.ailab.segmenter;

import lv.ailab.segmenter.datastruct.Lexicon;
import lv.ailab.segmenter.datastruct.SegmentationResult;
import lv.ailab.segmenter.datastruct.SegmenterData;
import lv.ailab.segmenter.datastruct.SegmenterDataWithLang;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.net.IDN;

/**
 * Tool for segmenting strings into words.
 * Created on 2015-05-08.
 * @author Lauma
 */
public class Segmenter
{
    /**
     * Trie-style data structure containing valid words.
     */
    public Lexicon lexicon;
    /**
     * Regular expression representing valid words that are not included in
     * lexicon.
     */
    public Pattern validSegment = Pattern.compile("\\d+");

    /**
     * Sorts segmentation results by counting how many pairs of adjacent words
     * from different languages are in segmentation. Enabling this option makes
     * segmentation notably slower!
     */
    public boolean sortByLanguageChanges = false;

    /**
     * Initialize Segmenter from a given lexicon.
     */
    public Segmenter (Lexicon l)
    {
        lexicon = l;
    }

    /**
     * Segment given string.
     * @param s string to segment
     * @return  segmentation results containing all valid segmentations and all
     *          words found in this string
     */
    public SegmentationResult segment(String s)
    {
        s = IDN.toUnicode(s, 0);
        SegmenterData memory;
        if (sortByLanguageChanges) memory = new SegmenterDataWithLang(s);
        else memory = new SegmenterData(s);

        for (int end = 1; end <= s.length(); end++)
        {
            for (int begin = 0; begin < end; begin++)
            {
                if (memory.isBegin(begin))
                {
                    String potWord = s.substring(begin, end);
                    List<Lexicon.Entry> found = lexicon.get(potWord);
                    if ((found == null || found.size() < 1) && validSegment.matcher(potWord).matches())
                        found = new LinkedList<Lexicon.Entry>()
                            {{add(new Lexicon.Entry(potWord, potWord, "regexp"));}};
                    if (found != null)
                    {
                        memory.setBeginValid(end);
                        memory.addWordEntries(potWord, found);
                        memory.makeNextSegmentationVariants(begin, end);
                    }
                }
            }
        }
        return memory.getResult();
    }

    /**
     * Segments each line in given file and prints out as JSON.
     * @param inFile    path to data file
     * @param outFile   path to result file
     */
    public void segmentFile(String inFile, String outFile)
    throws IOException
    {
        System.err.println("Segmenting file...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        String readLine = in.readLine();
        out.append("[\n");
        int count = 0;
        while (readLine != null)
        {
            count++;
            out.append(segment(readLine).toJSON());
            if (count % 1000 == 0) System.err.print(count + " processed.\r");
            readLine = in.readLine();
            if (readLine != null) out.append(",\n");
        }
        out.append("\n]");
        System.err.println(count + " processed. Done.");
        in.close();
        out.flush();
        out.close();
    }

}
