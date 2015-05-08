package lv.ailab.segmenter;


import java.io.IOException;

public class SegmenterUI
{
    public static String WORDLIST_FILE = "wordlist-filtered.txt";
    public Segmenter segmenter;

    public SegmenterUI()
    throws IOException
    {
        segmenter = new Segmenter (Lexicon.loadFromFile(WORDLIST_FILE));
    }

    public static void main(String[] args)
    throws IOException
    {
        SegmenterUI ui = new SegmenterUI();
        System.out.println(ui.segmenter.segment("savapils").toJSON());

    }
}
