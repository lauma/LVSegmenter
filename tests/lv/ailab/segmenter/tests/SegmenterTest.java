package lv.ailab.segmenter.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import lv.ailab.segmenter.Segmenter;
import lv.ailab.segmenter.datastruct.Lexicon;

import lv.ailab.segmenter.datastruct.SegmentationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Current segmentation tests are designed to be small and independent. Each
 * test loads its own data into the lexicon and cleanUpAfterTest() method erases
 * lexicon contents after each test
 */
public class SegmenterTest
{
	static Lexicon lexicon;
	static Segmenter segmenter;

    @Before
    public void setUpBeforeTest() throws IOException
    {
        lexicon = new Lexicon();
        segmenter = Segmenter.fullFunctionalitySegmenter (lexicon);
    }

    @After
    public void cleanUpAfterTest() throws IOException
    {
        lexicon = null;
        segmenter = null;
    }
	
	@Test
	public void sanityCheck() {
        lexicon.addWord("testa", "tests", "lv");
        lexicon.addWord("segments", "segments", "lv");
        assertEquals("tests segments", segmenter.segment("testasegments").primaryResultString());
	}

    @Test
    public void resultSorting() {
        lexicon.addWord("sun", "sun", "en");
        lexicon.addWord("sun", "Šūns", "lv");
        lexicon.addWord("city", "city", "en");
        lexicon.addWord("king", "king", "en");
        lexicon.addWord("size", "size", "en");
        lexicon.addWord("size", "sizēt", "lv");
        assertEquals("sun", segmenter.segment("sun-city").primaryResult().get(0).lemma);
        assertEquals("en", segmenter.segment("sun-city").primaryResult().get(0).lang);
        assertEquals("king size", segmenter.segment("kingsize").primaryResultString());
    }
    @Test
    public void length() {
        lexicon.addWord("biroja", "birojs", "lv");
        lexicon.addWord("iekartas", "iekārta", "lv");
        lexicon.addWord("bi", "bi", "lv");
        lexicon.addWord("roja", "roja", "lv");
        assertEquals("birojs - iekārta", segmenter.segment("biroja-iekartas").primaryResultString());
    }
	
	@Test
	public void unicode() {
        lexicon.addWord("vīna", "vīns", "lv");
        lexicon.addWord("skola", "skola", "lv");
        lexicon.addWord("betonēšana", "betonēt", "lv");
		assertEquals("vīns skola", segmenter.segment("xn--vnaskola-9ib")
                .primaryResultString());
		assertEquals("betonēt", segmenter.segment("xn--betonana-7cb49e").primaryResultString());
	}

    @Test
    public void nolang()
    {
        lexicon.addWord("zeme", "zeme", "lv");
        lexicon.addWord("size", "size", "en");
        SegmentationResult res = segmenter.segment("xxsizemeyy");
        res.sortSegmentations();
        assertEquals("[\"xxsi\", \"zeme\", \"yy\"]", res.segmentations.get(0).toJSONSegmentList());
        assertEquals("[\"xx\", \"size\", \"meyy\"]", res.segmentations.get(1).toJSONSegmentList());
    }

    @Test
    public void nolangSorting()
    {
        lexicon.addWord("zeme", "zeme", "lv");
        lexicon.addWord("size", "size", "en");
        lexicon.addWord("me", "me", "es");
        SegmentationResult res = segmenter.segment("sizeme");
        res.sortSegmentations();
        assertEquals("[\"size\", \"me\"]", res.segmentations.get(0).toJSONSegmentList());
        assertEquals("[\"si\", \"zeme\"]", res.segmentations.get(1).toJSONSegmentList());
    }


}
