package lv.ailab.wordembeddings.tests;

import java.util.Arrays;

import lv.ailab.wordembeddings.WordEmbeddings;

import org.junit.BeforeClass;
import org.junit.Test;

public class WordEmbeddingsTest {
	static WordEmbeddings wordembeddings_lv;
	static WordEmbeddings wordembeddings_en;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String EMBEDDINGS_LV_FILENAME = "lv_visaslemmas.out";
		wordembeddings_lv = new WordEmbeddings(EMBEDDINGS_LV_FILENAME);
		String EMBEDDINGS_EN_FILENAME = "polyglot_en.out";
		wordembeddings_en = new WordEmbeddings(EMBEDDINGS_EN_FILENAME);
	}

	@Test
	public void testCosineSimilarity() throws Exception {
		// hardcoded values for a single file - check if we match the C word2vec output
		// slight mismatch - numeric rounding?? 
		System.out.println(wordembeddings_lv.wordSimilarity("Latvija", "Eiropa"));		
	}

	@Test
	public void testSimilarWords() throws Exception {
		// hardcoded values for a single file - check if we match the C word2vec output
		// slight mismatch - numeric rounding?? 
		System.out.println(Arrays.toString(wordembeddings_lv.similarWords("Latvija", 10).toArray()));		
	}
	
	@Test
	public void testEnglish() throws Exception {
		System.out.println(Arrays.toString(wordembeddings_en.similarWords("sun", 10).toArray()));		
	}
}
