package lv.ailab.segmenter.datastruct;

import lv.ailab.segmenter.LangConst;

import java.util.Comparator;
import java.util.Iterator;
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
     * Language change count and other stats used for comparing different
     * LanguageSequences.
     */
    protected Stats stats;
    /**
     * Langugage sequence. First element is language for first segment in a
     * segmentation variant, second element is language for second segment, etc.
     */
    protected LinkedList<String> langs;

    /**
     * Create language sequence from given list of languages and already
     * calculated change count, seperator count and regexp count. Intended for
     * inner usage.
     * @param langs             list of languages
     * @param differences       precalculated language change count
     * @param separatorCount    precalculated seperator count
     * @param regExpCount       precalculated regexp count
     */
    protected LanguageSequence(
            List<String> langs, int differences, int separatorCount, int regExpCount)
    {
        this.stats = new Stats(differences, separatorCount, regExpCount);
        this.langs = new LinkedList<String>(){{addAll(langs);}};
    }

    /**
     * Create empty langugage sequence.
     */
    public LanguageSequence()
    {
        this.stats = new Stats();
        this.langs = new LinkedList<String>();
    }

    /**
     * Create language sequence with one element.
     * @param firstLang language of the first segment.
     */
    public LanguageSequence(String firstLang)
    {
        this.stats = new Stats(0,
                LangConst.SEPARATOR.equals(firstLang) ? 1 : 0,
                LangConst.REGEXP.equals(firstLang) ? 1 : 0);
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
        LanguageSequence res = new LanguageSequence(
                langs, stats.differences, stats.separatorCount, stats.regExpCount);
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
        if (LangConst.SEPARATOR.equals(nextLang))
            stats.separatorCount++;
        else if (LangConst.REGEXP.equals(nextLang))
            stats.regExpCount++;
        else
        {
            String prevRealLang = null;
            Iterator<String> desc = langs.descendingIterator();
            while (desc.hasNext())
            {
                String lang = desc.next();
                if (!lang.equals(LangConst.REGEXP) && !lang.equals(LangConst.SEPARATOR))
                {
                    prevRealLang = lang;
                    break;
                }
            }
            if (prevRealLang != null && !prevRealLang.equals(nextLang))
                    stats.differences++;

        }
        langs.add(nextLang);
    }

    /**
     * @return list of languages formatted as JSON array
     */
    public String toJSONLangList()
    {
        return "[\"" + String.join("\", \"", langs) + "\"]";
    }


    /**
     * Language sequence characteristics
     */
    public static class Stats implements Comparable
    {
        /**
         * Language change count. For sequences "" and "lv lv lv" this is 0, for
         * sequence "lv lv en" - 1, "lv en lv" - 2, etc. Languages with codes
         * LangConst.REGEXP, LangConst.SEPARATOR are not counted.
         */
        public int differences;

        /**
         * Count of the elements with language LangConst.SEPARATOR. Used for
         * sorting.
         */
        public int separatorCount;

        /**
         * Count of the elements with language LangConst.REGEXP. Used for sorting.
         */
        public int regExpCount;

        public Stats(int differences, int separatorCount, int regExpCount)
        {
            this.differences = differences;
            this.separatorCount = separatorCount;
            this.regExpCount = regExpCount;
        }

        public Stats()
        {
            this.differences = 0;
            this.separatorCount = 0;
            this.regExpCount = 0;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (this == null || o == null) return false;
            try
            {
                Stats os = (Stats) o;
                return ((differences == os.differences) &&
                        (separatorCount == os.separatorCount) &&
                        (regExpCount == os.regExpCount));
            } catch (ClassCastException e)
            {
                return false;
            }
        }

        @Override
        public int hashCode()
        {
            return differences * 787 + separatorCount * 97 + regExpCount * 7;
        }

        /**
         * Less language changes is better. If language change counts are
         * equal, less regexp fragments is better. If regexp counts are equal,
         * less seperators is better. Best object is the smallest one according
         * to this comparator.
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(Object o)
        {

            Stats os = (Stats) o;
            if (differences < os.differences) return -1;
            if (differences > os.differences) return 1;
            // if (differences == os.differences)
            if (regExpCount < os.regExpCount) return -1;
            if (regExpCount > os.regExpCount) return 1;
            // if (differences == os.differences && regExpCount == os.regExpCount)
            if (separatorCount < os.separatorCount) return -1;
            if (separatorCount > os.separatorCount) return 1;
            // if (differences == os.differences && regExpCount == os.regExpCount
            //      && separatorCount == os.separatorCount)
            return 0;
        }
    }

    /**
     * Comparator for comparing LanguageSequences. This determines which
     * language sequences are considered better than other for final result
     * ordering purposes. Best = smallest according to comparator.
     */
    public static class CountComparator implements Comparator<LanguageSequence>
    {
        protected static CountComparator singleton = null;
        private CountComparator() {};
        public static CountComparator get()
        {
            if (singleton == null) singleton = new CountComparator();
            return singleton;
        }
        @Override
        public int compare(LanguageSequence o1, LanguageSequence o2)
        {
            return o1.stats.compareTo(o2.stats);
        }
    }
}
