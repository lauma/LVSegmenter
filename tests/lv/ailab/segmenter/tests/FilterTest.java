package lv.ailab.segmenter.tests;

import static org.junit.Assert.*;

import lv.ailab.segmenter.Filter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created on 2015-06-05.
 *
 * @author Lauma
 */
public class FilterTest
{
    @Test
    public void containsAllSubstrings()
    {
        Filter.MINIMUM_LENGTH = 0;
        Filter f = new Filter();
        f.addAllSubstrings("cilv");
        assertEquals(true, f.isAccepted("cilv"));
        assertEquals(true, f.isAccepted("cil"));
        assertEquals(true, f.isAccepted("ilv"));
        assertEquals(true, f.isAccepted("ci"));
        assertEquals(true, f.isAccepted("il"));
        assertEquals(true, f.isAccepted("lv"));
        assertEquals(true, f.isAccepted("c"));
        assertEquals(true, f.isAccepted("i"));
        assertEquals(true, f.isAccepted("l"));
        assertEquals(true, f.isAccepted("v"));
    }
}
