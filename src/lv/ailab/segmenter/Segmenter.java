package lv.ailab.segmenter;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public Segmenter (Lexicon l)
    throws IOException
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
        SegmenterData memory = new SegmenterData(s);

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
                            {{add(new Lexicon.Entry(potWord, "regexp"));}};
                    if (found != null)
                        memory.addNextSegment(begin, end, found);
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
        System.out.println("Segmenting file...");
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
            if (count % 1000 == 0) System.out.print(count + " processed.\r");
            readLine = in.readLine();
            if (readLine != null) out.append(",\n");
        }
        out.append("\n]");
        System.out.println(count + " processed. Done.");
        in.close();
        out.flush();
        out.close();
    }

}
