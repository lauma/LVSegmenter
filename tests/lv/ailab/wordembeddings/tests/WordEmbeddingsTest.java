package lv.ailab.wordembeddings.tests;

import java.util.Arrays;

import lv.ailab.wordembeddings.WordEmbeddings;

import org.junit.BeforeClass;
import org.junit.Test;

public class WordEmbeddingsTest {
	static WordEmbeddings wordembeddings;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String EMBEDDINGS_FILENAME = "lv_lemmas_70p.out";
		wordembeddings = new WordEmbeddings(EMBEDDINGS_FILENAME);
	}

	@Test
	public void testCosineSimilarity() throws Exception {
		// hardcoded values for a single file - check if we match the C word2vec output
		// slight mismatch - numeric rounding?? 
		System.out.println(wordembeddings.wordSimilarity("Latvija", "Eiropa"));		
	}

	@Test
	public void testSimilarWords() throws Exception {
		// hardcoded values for a single file - check if we match the C word2vec output
		// slight mismatch - numeric rounding?? 
		System.out.println(Arrays.toString(wordembeddings.similarWords("Latvija", 10).toArray()));		
	}
}
