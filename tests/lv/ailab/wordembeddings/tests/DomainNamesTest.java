package lv.ailab.wordembeddings.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import lv.ailab.domainnames.AlternativeBuilder;
import lv.ailab.wordembeddings.WordEmbeddings;

import org.junit.BeforeClass;
import org.junit.Test;

public class DomainNamesTest {
	static AlternativeBuilder alternativebuilder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
	    String WORDLIST_FILE_EN = "wordsEn-sil-filtered.txt";
	    String EMBEDDINGS_LV_FILENAME = "lv_lemmas_70p.out";
	    String EMBEDDINGS_EN_FILENAME = "polyglot_en.out";
	    String[][] lexiconFiles = {{WORDLIST_FILE_LV, "lv"}, {WORDLIST_FILE_EN, "en"}};
	    alternativebuilder = new AlternativeBuilder(lexiconFiles, true, EMBEDDINGS_LV_FILENAME, EMBEDDINGS_EN_FILENAME);
	}

	@Test
	public void testEnglish() throws Exception {
		List<String> alternatives = alternativebuilder.buildAlternatives("suncity");
		System.out.print(alternativebuilder.resultToJson(alternatives));
		assertTrue(alternatives.contains("moon-city"));	
	}
	
	@Test
	public void testNumbers() throws Exception {
		assertFalse(alternativebuilder.buildAlternatives("licis-93").contains("licis-72"));	
	}
}
