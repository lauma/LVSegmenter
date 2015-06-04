package lv.ailab.segmenter.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;

import org.junit.BeforeClass;
import org.junit.Test;

public class SegmenterTest {
	static Lexicon lexicon;
	static Segmenter segmenter;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
	    String WORDLIST_FILE_LV = "wordlist-lv.txt";
	    String WORDLIST_FILE_EN = "wordsEn-sil.txt";
	    boolean SORT_BY_LANG_CHANGES = true;

		lexicon = new Lexicon();
		lexicon.addFromFile(WORDLIST_FILE_LV, "lv");
		lexicon.addFromFile(WORDLIST_FILE_EN, "en");
        segmenter = new Segmenter (lexicon);
        segmenter.sortByLanguageChanges = SORT_BY_LANG_CHANGES;
	}
	
	@Test
	public void sanityCheck() {
		assertEquals("tests segments", segmenter.segment("testasegments").primaryResultString());
	}
	
	@Test
	public void languages() {
		assertEquals("king size", segmenter.segment("kingsize").primaryResultString());		 
		assertEquals("birojs - iekārta", segmenter.segment("biroja-iekartas").primaryResultString());
	}
	
	@Test
	public void unicode() {
		assertEquals("vīns skola", segmenter.segment("xn--vnaskola-9ib").primaryResultString());
		assertEquals("betonēt", segmenter.segment("xn--betonana-7cb49e").primaryResultString());
	}


}
