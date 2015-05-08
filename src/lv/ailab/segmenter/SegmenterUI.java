package lv.ailab.segmenter;


import java.io.IOException;

public class SegmenterUI
{
    public static String WORDLIST_FILE = "wordlist-filtered.txt";
    //public Segmenter segmenter;

   // public SegmenterUI()
    //throws IOException
    //{
    //    segmenter = new Segmenter (Lexicon.loadFromFile(WORDLIST_FILE));
    //}

    public static void main(String[] args)
    throws IOException
    {
        if(args.length == 1)
        {
            Segmenter s = new Segmenter (Lexicon.loadFromFile(WORDLIST_FILE));
            System.out.println(s.segment(args[0]).toJSON());
        } else if (args.length == 4 && args[0].equals("-segment"))
        {
            Segmenter s = new Segmenter (Lexicon.loadFromFile(args[1]));
            s.segmentFile(args[2], args[3]);
        } else if (args.length == 4 && args[0].equals("-filter"))
        {
            Filter.loadFromFile(args[1]).filterList(args[2], args[3]);
        } else
            printInfo();
    }

    /**
     * Print information about this package.
     */
    protected static void printInfo()
    {
        System.out.println("To segment a single string against default worlist, pass it as parameter.");
        System.out.println("To segment each line in a file, use following parameters:");
        System.out.println("\t-segment wordlist input_file output_file");
        System.out.println("To filter a wordlist, use following parameters:");
        System.out.println("\t-filter filter_file input_file output_file");
        System.out.println();
    }
}
