package lv.ailab.segmenter;


import java.io.IOException;

public class SegmenterUI
{
    public static String WORDLIST_FILE = "wordlist-filtered.txt";
    public Lexicon l;

    public SegmenterUI()
    throws IOException
    {
        l = Lexicon.loadFromFile(WORDLIST_FILE);

    }

    public static void main(String[] args)
    throws IOException
    {
        SegmenterUI ui = new SegmenterUI();

    }
}
