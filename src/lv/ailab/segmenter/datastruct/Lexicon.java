package lv.ailab.segmenter.datastruct;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2015-05-06.
 * @author Lauma
 */
public class Lexicon
{
    protected PatriciaTrie<List<Entry>> data = new PatriciaTrie<>();
    protected Set<String> langStubs = new HashSet<>();

    /**
     * Load contents of the file in to this lexicon without deleting previously
     * loaded things.
     * @param wordListFile file to load
     * @param lang language or source information to add for words from this
     *             file
     * @throws IOException
     */
    public void addFromFile(String wordListFile, String lang)
    throws IOException
    {
        System.err.println("Loading wordlist...");
        langStubs.add(lang);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(wordListFile), "UTF-8"));
        String line = in.readLine();
        int count = 0;
        while (line != null)
        {
            count++;
            String[] parts = line.split("\t");
            if (parts.length < 1 || parts.length > 2 )
                System.err.println("Unexpected line in word file: \"" + line + "\"");
            else
            {
                String form = parts[0];
                String lemma = parts.length > 1 ? parts[1] : parts[0];
                String noDiacritics = StringUtils.replaceChars(form,
                        "āčēģīķļņōŗšūž",
                        "acegiklnorsuz");
                String transliterated = StringUtils.replaceEach(form,
                        new String[]{"ā", "č", "ē", "ģ", "ī", "ķ", "ļ", "ņ", "ō", "ŗ", "š", "ū", "ž"},
                        new String[]{"aa", "ch", "ee", "gj", "ii", "kj", "lj", "nj", "oo", "rj", "sh", "uu", "zh"});
                Entry e = new Entry (lemma, lang);

                add(form, e);
                add(noDiacritics, e);
                add(transliterated, e);
            }
            if (count % 10000 == 0) System.out.print(count + " loaded.\r");
            line = in.readLine();
        }
        in.close();
        System.err.println(count + " loaded. Done.");
    }

    public Set<String> getLanguages ()
    {
        return langStubs;
    }

    public Set<String> getLanguages (String key)
    {
        List<Entry> entries = data.getOrDefault(key, new LinkedList<Entry>());
        return entries.stream().map(e -> e.lang).collect(Collectors.toSet());
    }

    public Set<String> getLemmas (String key)
    {
        List<Entry> entries = data.getOrDefault(key, new LinkedList<Entry>());
        return entries.stream().map(e -> e.lemma).collect(Collectors.toSet());
    }

    public List<Entry> get(String key)
    {
        return data.get(key);
    }

    public void add(String key, Entry desc)
    {
        if (data.containsKey(key))
        {
            List<Entry> container = data.get(key);
            if (!container.contains(desc))
                container.add(desc);
        }
        else data.put(key, new LinkedList<Entry>(){{add(desc);}});
    }

    /**
     * Immutable entry describing one word.
     */
    public static class Entry
    {
        public final String lang;
        public final String lemma;

        public Entry (String lemma, String language)
        {
            this.lemma = lemma;
            this.lang = language;

        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (this == null || o == null) return false;
            try
            {
                Entry oe = (Entry) o;
                return ((lemma == oe.lemma || lemma != null && lemma.equals(oe.lemma)) &&
                        (lang == oe.lang || lang != null && lang.equals(oe.lang)));
            } catch (ClassCastException e)
            {
                return false;
            }
        }

        public int hashCode()
        {
            return (lang == null ? 0 : lang.hashCode()) * 647 +
                    (lemma == null ? 0 : lemma.hashCode()) * 47;
        }
    }
}

