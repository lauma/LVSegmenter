package lv.ailab.segmenter.datastruct;

import java.util.LinkedList;
import java.util.List;

/**
 * Information about languages used in a segmentation variant.
 * NB! One segmentation variant can have multiple language arrangement
 * variants.
 */
public class LanguageSequence
{
    /**
     * Language change count. For sequences "" and "lv lv lv" this is 0, for
     * sequence "lv lv en" - 1, "lv en lv" - 2, etc.
     */
    protected int differences;
    /**
     * Langugage sequence. First element is language for first segment in a
     * segmentation variant, second element is language for second segment, etc.
     */
    protected LinkedList<String> langs;

    /**
     * Create language sequence from given list of languages and already
     * calculated change count. Intended for inner usage.
     * @param langs         list of languages
     * @param differences   precalculated language change count
     */
    protected LanguageSequence(List<String> langs, int differences)
    {
        this.differences = differences;
        this.langs = new LinkedList<String>(){{addAll(langs);}};
    }

    /**
     * Create language sequence with one element.
     * @param firstLang language of the first segment.
     */
    public LanguageSequence(String firstLang)
    {
        this.differences = 0;
        this.langs = new LinkedList<String>(){{add(firstLang);}};
    }

    /**
     * Make new language sequence that contains all languages of this
     * sequence + one more.
     * @param nextLang last language in the new sequence
     * @return new sequence
     */
    public LanguageSequence makeNext (String nextLang)
    {
        LanguageSequence res = new LanguageSequence(langs, differences);
        res.addNext(nextLang);
        return res;
    }

    /**
     * Add next language to sequence and update language change count
     * accordingly.
     * @param nextLang  language to add
     */
    public void addNext (String nextLang)
    {
        if (langs.size() > 0 && !langs.getLast().equals(nextLang))
            differences++;
        langs.add(nextLang);
    }

    /**
     * @return list of languages formatted as JSON array
     */
    public String toJSONLangList()
    {
        return "[\"" + String.join("\", \"", langs) + "\"]";
    }

}
