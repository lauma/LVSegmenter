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
		assertEquals("tests segments", String.join(" ", segmenter.segment("testasegments").primaryResult()));
	}
	
	@Test
	public void languages() {
		assertEquals("king size", String.join(" ", segmenter.segment("kingsize").primaryResult()));		 
		assertEquals("birojs - iekārta", String.join(" ", segmenter.segment("biroja-iekartas").primaryResult()));
	}
	
	@Test
	public void unicode() {
		assertEquals("vīns skola", String.join(" ", segmenter.segment("xn--vnaskola-9ib").primaryResult()));
		assertEquals("betonēt", String.join(" ", segmenter.segment("xn--betonana-7cb49e").primaryResult()));
	}


}
