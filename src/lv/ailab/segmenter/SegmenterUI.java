package lv.ailab.segmenter;


import java.io.IOException;

public class SegmenterUI
{
    public static String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
    public static String WORDLIST_FILE_EN = "google-10000-filtered-english.txt";
    //public Segmenter segmenter;

   // public SegmenterUI()
    //throws IOException
    //{
    //    segmenter = new Segmenter (Lexicon.loadFromFile(WORDLIST_FILE_LV));
    //}

    public static void main(String[] args)
    throws IOException
    {
        if(args.length == 1)
        {
            Lexicon l = new Lexicon();
            l.addFromFile(WORDLIST_FILE_LV, "lv");
            l.addFromFile(WORDLIST_FILE_EN, "en");
            Segmenter s = new Segmenter (l);
            long beginTime = System.nanoTime();
            String res = s.segment(args[0]).toJSON();
            long endTime = System.nanoTime();
            System.out.println(res);
            System.out.printf("Segmented in %.2f seconds.\n", (double) (endTime - beginTime) / 1000000000.0);
        } else if (args.length >= 4 && args[0].equals("-segment"))
        {
            Lexicon l = new Lexicon();
            for (int i = 3; i < args.length; i++)
                l.addFromFile(args[i].substring(args[i].indexOf('=') + 1), args[i].substring(0, args[i].indexOf('=')));
            Segmenter s = new Segmenter(l);
            long beginTime = System.nanoTime();
            s.segmentFile(args[1], args[2]);
            long endTime = System.nanoTime();
            System.out.printf("Segmented in %.2f in seconds.\n", (double)(endTime - beginTime)/1000000000.0);
        } else if (args.length == 4 && args[0].equals("-filter"))
        {
            Filter.loadFromFile(args[3]).filterList(args[1], args[2]);
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
        System.out.println("\t-segment input_file output_file lang1=wordlist1 lang2=wordlist2 ...");
        System.out.println("To filter a wordlist, use following parameters:");
        System.out.println("\t-filter input_file output_file filter_file");
        System.out.println();
    }
}
