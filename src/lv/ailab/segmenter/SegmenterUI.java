package lv.ailab.segmenter;


import java.io.IOException;

public class SegmenterUI
{
    public static String WORDLIST_FILE = "wordlist.txt";
    public Lexicon l;

    public SegmenterUI()
    throws IOException
    {
        l = new Lexicon();
        System.out.print("Loading wordlist...");
        l.loadFromFile(WORDLIST_FILE);
        System.out.println(" Done.");
    }

    public static void main(String[] args)
    throws IOException
    {
        SegmenterUI ui = new SegmenterUI();

    }
}
