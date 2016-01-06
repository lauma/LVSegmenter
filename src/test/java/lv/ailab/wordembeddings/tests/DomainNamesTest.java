package lv.ailab.wordembeddings.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import lv.ailab.domainnames.AlternativeBuilder;
import lv.ailab.segmenter.datastruct.Lexicon;
import lv.ailab.segmenter.datastruct.Lexicon.Entry;
import lv.ailab.segmenter.datastruct.SegmentationResult;
import lv.ailab.wordembeddings.WordEmbeddings;

import org.junit.BeforeClass;
import org.junit.Test;

public class DomainNamesTest {
	static AlternativeBuilder alternativebuilder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
	    String WORDLIST_FILE_EN = "wordsEn-sil-filtered.txt";
	    String EMBEDDINGS_LV_FILENAME = "lv_visaslemmas.out";
	    String EMBEDDINGS_EN_FILENAME = "polyglot_en.out";
	    String SYNONYMS_FILENAME = "sinonimi.txt";
	    String BLACKLIST_FILENAME = "blacklist.txt";	    
	    String[][] lexiconFiles = {{WORDLIST_FILE_LV, "lv"}, {WORDLIST_FILE_EN, "en"}};
	    alternativebuilder = new AlternativeBuilder(
				lexiconFiles, true, true, EMBEDDINGS_LV_FILENAME, EMBEDDINGS_EN_FILENAME, SYNONYMS_FILENAME, BLACKLIST_FILENAME);
	}

	@Test
	public void testEnglish() throws Exception {
		List<String> alternatives = alternativebuilder.buildAlternatives("suncity");
		System.out.println(AlternativeBuilder.resultToJson(alternatives));
		assertTrue(alternatives.contains("moon-city"));	
	}
	
	@Test
	public void testNumbers() throws Exception {
		List<String> alternatives = alternativebuilder.buildAlternatives("licis-93");
		System.out.println(AlternativeBuilder.resultToJson(alternatives));
		assertFalse(alternatives.contains("licis-72"));	
	}
	
    @Test
    public void nolang_only() throws Exception {
    	SegmentationResult segments = alternativebuilder.segmenter.segment("nafhqbn");
//    	System.out.println(segments.toJSON());
    	List<Entry> result = segments.primaryResult();
    	assertTrue(segments != null);
		List<String> alternatives = alternativebuilder.buildAlternatives("nafhqbn");
		System.out.println(AlternativeBuilder.resultToJson(alternatives));
    }

}
