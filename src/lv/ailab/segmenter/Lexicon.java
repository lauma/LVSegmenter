package lv.ailab.segmenter;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * Created on 2015-05-06.
 * @author Lauma
 */
public class Lexicon
{
    public PatriciaTrie<Entry> data = new PatriciaTrie<>();

    public void loadFromFile(String wordListFile)
    throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(wordListFile), "UTF-8"));
        String line = in.readLine();
        while (line != null)
        {
            String[] parts = line.split("\t");
            if (parts.length != 2)
                System.err.println("Unexpected line in word file: \"" + line + "\"");
            else
            {
                Entry e = new Entry (parts[0], parts[1]);
                data.put(e.form, e);
                data.put(e.lemma, e);
                data.put(e.noDiacritics, e);
                data.put(e.transliterated, e);
            }
        }
        in.close();
    }

    /**
     * Immutable entry describing one word.
     */
    public static class Entry
    {
        public final String form;
        public final String lemma;
        public final String noDiacritics;
        public final String transliterated;

        public Entry (String form, String lemma)
        {
            this.form = form == null ? "" : form.toLowerCase();
            this.lemma = lemma == null ? "" : lemma.toLowerCase();
            this.noDiacritics = StringUtils.replaceChars(this.form,
                    "āčēģīķļņōŗšūž",
                    "acegiklnorsuz");
            this.transliterated = StringUtils.replaceEach(this.form,
                    new String[]{"ā",  "č",  "ē",  "ģ",  "ī",  "ķ",  "ļ",  "ņ",  "ō",  "ŗ",  "š",  "ū",  "ž"},
                    new String[]{"aa", "ch", "ee", "gj", "ii", "kj", "lj", "nj", "oo", "rj", "sh", "uu", "zh"});

        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (this == null || o == null) return false;
            try
            {
                Entry oe = (Entry) o;
                return (form == oe.form || form != null && form.equals(oe.form)) &&
                        (lemma == oe.lemma || lemma != null && lemma.equals(oe.lemma)) &&
                        (noDiacritics == oe.noDiacritics || noDiacritics != null && noDiacritics.equals(oe.noDiacritics)) &&
                        (transliterated == oe.transliterated || transliterated != null && transliterated.equals(oe.transliterated));
            } catch (ClassCastException e)
            {
                return false;
            }
        }

        public int hashCode()
        {
            return (form == null ? 0 : form.hashCode()) * 7 +
                    (lemma == null ? 0 : lemma.hashCode()) * 47 +
                    (noDiacritics == null ? 0 : noDiacritics.hashCode()) * 647 +
                    (transliterated == null ? 0 : transliterated.hashCode()) * 1637;
        }
    }
}

