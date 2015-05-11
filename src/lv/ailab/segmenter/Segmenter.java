package lv.ailab.segmenter;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created on 2015-05-08.
 *
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

    public SegmentationResult segment(String s)
    {
        // i-th position contains information, if there is a segmentation
        // variant found, ending in this position.
        ArrayList<Boolean> dynamicTable = new ArrayList<>(s.length());
        // i-th position contains segmentation variants for s.substring(0, i)
        ArrayList<ArrayList<SegmentationVariant>> memorizedWords = new ArrayList<>(s.length());
        // in case overall segmentation is not successful, any reasonable
        // substring might be usefull
        HashMap<String, List<Lexicon.Entry>> foundWords = new HashMap<>();
        for (int i = 0; i < s.length() + 1; i++)
        {
            dynamicTable.add(false);
            memorizedWords.add(new ArrayList<>());
        }
        dynamicTable.set(0, true);

        for (int end = 1; end <= s.length(); end++)
        {
            for (int begin = 0; begin < end; begin++)
            {
                if (dynamicTable.get(begin))
                {
                    String potWord = s.substring(begin, end);
                    List<Lexicon.Entry> found = lexicon.get(potWord);
                    if ((found == null || found.size() < 1) && validSegment.matcher(potWord).matches())
                        found = new LinkedList<Lexicon.Entry>()
                            {{add(new Lexicon.Entry(potWord, "regexp"));}};
                    if (found != null)
                    {
                        Set<String> langs = null;
                        dynamicTable.set(end, true);
                        foundWords.put(potWord, found);
                        if (memorizedWords.get(begin).isEmpty())
                        {
                            SegmentationVariant newVariant = new SegmentationVariant();
                            newVariant.addNext(potWord);
                            memorizedWords.get(end).add(newVariant);
                        }
                        else for (SegmentationVariant variant: memorizedWords.get(begin))
                        {
                            SegmentationVariant newVariant = variant.makeNext(potWord);
                            memorizedWords.get(end).add(newVariant);
                        }
                    }
                }
            }
        }
        return new SegmentationResult(s, memorizedWords.get(s.length()), foundWords);
    }

    /**
     * Segments each line in given file and prints out as JSON.
     * @param inFile path to data file
     * @param outFile path to result file
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
