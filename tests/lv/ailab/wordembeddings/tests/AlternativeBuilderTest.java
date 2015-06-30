package lv.ailab.wordembeddings.tests;

import static org.junit.Assert.*;

import java.util.List;

import lv.ailab.domainnames.AlternativeBuilder;

import org.junit.BeforeClass;
import org.junit.Test;

public class AlternativeBuilderTest {
	static AlternativeBuilder alternativebuilder;	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
	    String WORDLIST_FILE_EN = "wordsEn-sil-filtered.txt";
	    String EMBEDDINGS_LV_FILENAME = "lv_visaslemmas.out";
	    String EMBEDDINGS_EN_FILENAME = "polyglot_en.out";
	    String SYNONYMS_FILENAME = "sinonimi.txt";
	    String[][] lexiconFiles = {{WORDLIST_FILE_LV, "lv"}, {WORDLIST_FILE_EN, "en"}};
	    alternativebuilder = new AlternativeBuilder(
				lexiconFiles, true, true, EMBEDDINGS_LV_FILENAME, EMBEDDINGS_EN_FILENAME, SYNONYMS_FILENAME);		
	}

	@Test
	public void normalTest() throws Exception {
		List<String> alternatives = alternativebuilder.buildAlternatives("saulesbaterijas");
		System.out.println(AlternativeBuilder.resultToJson(alternatives));
		assertTrue(alternatives.contains("mēness-baterijas"));	
	}

	@Test
	public void synonymsTest() throws Exception {
		List<String> alternatives = alternativebuilder.buildAlternatives("abažūrs");
		System.out.println(AlternativeBuilder.resultToJson(alternatives));
		assertTrue(alternatives.contains("atgaismis"));	
	}
}
