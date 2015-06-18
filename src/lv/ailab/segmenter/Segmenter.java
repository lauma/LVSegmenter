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
     * Regular expression class enlisting all symbols that should be considered
     * as seperators, not parts of the segments. Null - no special treatment for
     * any charcter.
     */
    public Pattern separatorClass = Pattern.compile("[\\-.:/?=&\\\\]");

    /**
     * Sorts segmentation results by counting how many pairs of adjacent words
     * from different languages are in segmentation. Enabling this option makes
     * segmentation notably slower!
     */
    public boolean sortByLanguageChanges = false;

    /**
     * Allows segments not conforming any segment verification method (OOV,
     * regexp, separators).
     */
    public boolean allowNolangSegments = false;

    /**
     * Creates Segmenter with all time consuming optional features enabled.
     * @param l lexicont to initialize Segmenter with
     */
    public static Segmenter fullFunctionalitySegmenter (Lexicon l)
    {
        Segmenter res = new Segmenter(l);
        res.allowNolangSegments = true;
        res.sortByLanguageChanges = true;
        return res;
    }

    /**
     * Creates segmenter with all time consuming optional features disabled.
     * @param l lexicont to initialize Segmenter with
     */
    public static Segmenter fastSegmenter (Lexicon l)
    {
        Segmenter res = new Segmenter(l);
        res.allowNolangSegments = false;
        res.sortByLanguageChanges = false;
        return res;
    }

    /**
     * Initialize Segmenter from a given lexicon and default flags.
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
            //System.out.println(end);
            for (int begin = 0; begin < end; begin++)
            {
                String potWord = s.substring(begin, end);
                List<Lexicon.Entry> found = lexicon.get(potWord);
                if ((found == null || found.size() < 1) && validSegment
                        .matcher(potWord).matches())
                    found = new LinkedList<Lexicon.Entry>()
                    {{add(new Lexicon.Entry(potWord, potWord, LangConst.REGEXP));}};
                if ((found == null || found.size() < 1) && separatorClass
                        .matcher(potWord).matches())
                    found = new LinkedList<Lexicon.Entry>()
                    {{add(new Lexicon.Entry(potWord, potWord, LangConst.SEPARATOR));}};
                if (found != null)
                {
                    // If found word is a continuation for an existing
                    // segmentation variant or it is possible to use before it
                    // nolang "middleman" segment.
                    if (memory.isValidBegin(begin) ||
                            allowNolangSegments && makeNolangSegment(begin, memory))
                    {
                        memory.setBeginValid(end);
                        memory.addWordEntries(potWord, begin, found);
                        memory.makeNextSegmentationVariants(begin, end);
                    }
                    // If found word can't be a continuation for an existing
                    // segmentation variant.
                    else
                        memory.addWordEntries(potWord, begin, found);
                }
            }

        }
        //System.err.println("miip");
        // To allow segmentation end with nolang word.
        if (allowNolangSegments) makeNolangSegment(s.length(), memory);
        //System.err.println("miip");
        return memory.getResult();
    }

    /**
     * This function trays to create nolang segments ending at the specified
     * position. If segmenter is not allowed to make nolang segments or if
     * there no place for such segment, false is returned.
     * @param noLangSegmEnd ending index for the new segment to create
     * @param memory dynamic programming data for current segmentation task
     * @return true, if new segment was made, false otherwise
     */
    protected boolean makeNolangSegment(int noLangSegmEnd, SegmenterData memory)
    {
        if (!allowNolangSegments || memory.isValidBegin(noLangSegmEnd)) return false;
        boolean res = false;
        // Here we want to find all recent segmentation endings
        // without including full words as substrings of nonlang
        // segments.
        int noLangSegmBegin;
        for (noLangSegmBegin = noLangSegmEnd - 1; noLangSegmBegin >= 0; noLangSegmBegin--)
        {
            // This means we are trying to include whole word as
            // a part of nolang segment. This should not be
            // done, so we quit the loop.
            if (memory.isWordBegin(noLangSegmBegin) + noLangSegmBegin <= noLangSegmEnd &&
                    memory.isWordBegin(noLangSegmBegin) != 0)
                break;
            // If some segmentation ends here or this is the
            // beginning of the string, we should consider this
            // as potential for nolang segment.
            if (memory.isValidBegin(noLangSegmBegin) || noLangSegmBegin == 0)
            {
                // Update memory for the nolang segment.
                String nolangSegment = memory.data.substring(noLangSegmBegin, noLangSegmEnd);
                memory.addNolangSegment(nolangSegment);
                memory.makeNextSegmentationVariants(noLangSegmBegin, noLangSegmEnd);
                res = true;
            }
        }
        return res;
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
            if (count % 100 == 0) System.err.print(count + " processed.\r");
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
