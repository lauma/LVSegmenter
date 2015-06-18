package lv.ailab.segmenter;

import lv.ailab.segmenter.datastruct.Lexicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Sample interface, demonstrating usage for Segmenter and lexicon filter.
 */
public class SegmenterUI
{
    public static String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
    public static String WORDLIST_FILE_EN = "wordsEn-sil-filtered.txt";
    public static boolean FAST_SEGMENTATION = false;

    /**
     * Run Segmenter or Filter tools. To get usage info, provide no parameters.
     */
    public static void main(String[] args)
    throws IOException
    {
        // Segmenter invocation with a single word.
        if (args.length == 1)
        {
            Lexicon l = new Lexicon();
            l.addFromFile(WORDLIST_FILE_LV, "lv");
            l.addFromFile(WORDLIST_FILE_EN, "en");
            Segmenter s = FAST_SEGMENTATION ?
                    Segmenter.fastSegmenter(l) : Segmenter.fullFunctionalitySegmenter(l);
            long beginTime = System.nanoTime();
            String res = s.segment(args[0]).toJSON();
            long endTime = System.nanoTime();
            System.out.println(res);
            System.err
                    .printf("Segmented in %.2f seconds.\n", (double) (endTime - beginTime) / 1000000000.0);
        }
        // Segmenter invocation for piping.
        else if(args.length >= 2 &&  args[0].equals("-segmentpipe"))
        {
            Lexicon l = new Lexicon();
            for (int i = 1; i < args.length; i++)
                l.addFromFile(args[i].substring(args[i].indexOf('=') + 1), args[i].substring(0, args[i].indexOf('=')));
            Segmenter s = FAST_SEGMENTATION ?
                    Segmenter.fastSegmenter(l) : Segmenter.fullFunctionalitySegmenter(l);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String line = in.readLine();
            while (line!= null && !line.equals(""))
            {
                String res = s.segment(line).toJSON();
                System.out.println(res);
                line = in.readLine();
            }
            System.err.println("Segmenter pipe ended.");
            in.close();
        }
        // Segmenting each line in a file.
        else if (args.length >= 4 && args[0].equals("-segment"))
        {
            Lexicon l = new Lexicon();
            for (int i = 3; i < args.length; i++)
                l.addFromFile(args[i].substring(args[i].indexOf('=') + 1), args[i].substring(0, args[i].indexOf('=')));
            Segmenter s = FAST_SEGMENTATION ?
                    Segmenter.fastSegmenter(l) : Segmenter.fullFunctionalitySegmenter(l);
            long beginTime = System.nanoTime();
            s.segmentFile(args[1], args[2]);
            long endTime = System.nanoTime();
            System.err.printf("Segmented in %.2f in seconds.\n", (double) (endTime - beginTime) / 1000000000.0);
        }
        // Filtering wordlist.
        else if (args.length == 4 && args[0].equals("-filter"))
        {
            Filter f = new Filter();
            f.loadFromFile(args[3]);
            f.filterList(args[1], args[2]);
        }
        // Wrong parameters passed or no parameters at all.
        else
            printInfo();
    }

    /**
     * Print information about this interface.
     */
    protected static void printInfo()
    {
        System.err.println("To segment a single string against default worlist pass it as parameter.");
        System.err.println("To lunch segmenter for pipeline (empty inputline halts) use following\nparameters:");
        System.err.println("\t-segmentpipe lang1=wordlist1 lang2=wordlist2 ...");
        System.err.println("To segment each line in a file use following parameters:");
        System.err.println("\t-segment input_file output_file lang1=wordlist1 lang2=wordlist2 ...");
        System.err.println("To filter a wordlist use following parameters:");
        System.err.println("\t-filter input_file output_file filter_file");
        System.err.println();
    }
}
