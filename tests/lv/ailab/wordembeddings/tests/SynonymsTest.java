package lv.ailab.wordembeddings.tests;

import static org.junit.Assert.*;

import lv.ailab.wordembeddings.Synonyms;

import org.junit.BeforeClass;
import org.junit.Test;

public class SynonymsTest {
	static Synonyms synonyms;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String SYNONYMS_FILENAME = "sinonimi.txt";
		synonyms = new Synonyms(SYNONYMS_FILENAME);
	}

	@Test
	public void testSimilarWords() throws Exception {
		assertTrue(synonyms.similarWords("māja").contains("nams"));
		assertFalse(synonyms.similarWords("māja").contains("abažūrs"));
		assertFalse(synonyms.similarWords("abažūrs").contains("atgaismis"));
	}
	
}
