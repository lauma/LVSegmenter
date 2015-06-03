package lv.ailab.segmenter;

import static org.junit.Assert.*;

import java.io.IOException;

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
	public void testSegment() {
		assertEquals("testa segments", String.join(" ", segmenter.segment("testasegments").primaryResult()));
		assertEquals("king size", String.join(" ", segmenter.segment("kingsize").primaryResult()));		 
		assertEquals("biroja iekārtas", String.join(" ", segmenter.segment("biroja-iekartas").primaryResult()));
//		assertEquals("betonēšana", String.join(" ", segmenter.segment("xn--betonana-7cb49e").primaryResult()));
		assertEquals("vīna skola", String.join(" ", segmenter.segment("xn--vnaskola-9ib").primaryResult()));
	}

}
