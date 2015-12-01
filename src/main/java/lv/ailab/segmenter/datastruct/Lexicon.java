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
                addWord(form.toLowerCase(), lemma.toLowerCase(), lang); // FIXME - bija slikts efekts no personvārdiem, varbūt to toLowercase nevajag.
            }
            if (count % 10000 == 0) System.err.print(count + " loaded.\r");
            line = in.readLine();
        }
        in.close();
        System.err.println(count + " loaded. Done.");
    }

    /**
     * Add to this lexicon one new wordform.
     * @param form  wordform to add
     * @param lemma lemma for this wordform
     * @param lang  language for this wordform
     */
    public void addWord(String form, String lemma, String lang)
    {
        Entry e = new Entry (form, lemma, lang);
        for (String var : generateTranslitVariants(form))
        {
            addEntry(var, e);
        }
    }

    /**
     * For a given wordform creates trnsliteration variants - 1) without
     * diacrictical marks, 2) with double letters, 3) lowercase
     * @param word wordform to transliterate
     * @return list of transliteration variants, original form included as first
     */
    public static List<String>generateTranslitVariants(String word)
    {
        ArrayList<String> result = new ArrayList<>();
        result.add(word);
        result.add(word.toLowerCase());
        String tmp = StringUtils.replaceChars(word,
                "ĀāČčĒēĢģĪīĶķĻļŅņŌōŖŗŠšŪūZž",
                "AaCcEeGgIiKkLlNnOoRrSsUuZz");
        result.add(tmp);
        result.add(tmp.toLowerCase());
        tmp = StringUtils.replaceEach(word,
                new String[]{"Ā", "ā", "Č", "č", "Ē", "ē", "Ģ", "ģ", "Ī", "ī", "Ķ", "ķ", "Ļ", "ļ", "Ņ", "ņ", "Ō", "ō", "Ŗ", "ŗ", "Š", "š", "Ū", "ū", "Ž", "ž"},
                new String[]{"Aa","aa","Ch","ch","Ee","ee","Gj","gj","Ii","ii","Kj","kj","Lj","lj","Nj","nj","Oo","oo","Rj","rj","Sh","sh","Uu","uu","Zh","zh"});
        result.add(tmp);
        result.add(tmp.toLowerCase());
        return result;
    }

    public Set<String> getLanguages ()
    {
        return langStubs;
    }

    public Set<String> getLanguages (String key)
    {
        List<Entry> entries = data.getOrDefault(key, new LinkedList<>());
        return entries.stream().map(e -> e.lang).collect(Collectors.toSet());
    }

    public Set<String> getLemmas (String key)
    {
        List<Entry> entries = data.getOrDefault(key, new LinkedList<>());
        return entries.stream().map(e -> e.lemma).collect(Collectors.toSet());
    }

    public List<Entry> get(String key)
    {
        return data.get(key);
    }

    protected void addEntry(String key, Entry desc)
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
        public final String originalForm;
        public final String lang;
        public final String lemma;

        public Entry (String form, String lemma, String language)
        {
            originalForm = form;
            this.lemma = lemma;
            this.lang = language;

        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null) return false;
            try
            {
                Entry oe = (Entry) o;
                return ((lemma == oe.lemma || lemma != null && lemma.equals(oe.lemma)) &&
                        (lang == oe.lang || lang != null && lang.equals(oe.lang)) &&
                        (originalForm == oe.originalForm || originalForm != null && originalForm.equals(oe.originalForm)));
            } catch (ClassCastException e)
            {
                return false;
            }
        }

        public int hashCode()
        {
            return (lang == null ? 0 : lang.hashCode()) * 647 +
                    (lemma == null ? 0 : lemma.hashCode()) * 47 +
                    (originalForm == null ? 0 : originalForm.hashCode()) * 17;
        }
    }
}

