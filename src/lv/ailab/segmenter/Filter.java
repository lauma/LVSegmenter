package lv.ailab.segmenter;

import lv.ailab.segmenter.datastruct.Lexicon;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.*;
import java.net.IDN;

/**
 * Filtering functionality. Filter object is contains a list of strings.
 * Filtering is done, comparing given string against *all substrings* of the
 * strings contained in the Filter.
 * This can be used to reduce Segmenter's lexicon size: if list of strings to
 * segment is available during the inicialization of the Segmenter, lexicon can
 * can be filtered against that list with the help of Filter object.
 * Created on 2015-05-07.
 * @author Lauma
 */
public class Filter
{
    /**
     * Minimum word length to be accepted.
     */
    public static int MINIMUM_LENGTH = 1;
    /**
     * Data structure with the list of strings to filter against. For each
     * string to filter against, this structure contains subststring(0, length),
     * substring(1, length), ..., substring (length-1, length), and it is used
     * as prefix map, thus, providing effective way to compare given string
     * against all substrings of the strings in the filter list.
     */
    public PatriciaTrie<Boolean> data = new PatriciaTrie<>();

    /**
     * Initialize Filter object from a list of strings given in the file.
     * @param wordListFile  path to file with a list of string
     * @return  Filter objest that can filter against substrings of the strings
     *          given in the input file
     * @throws IOException
     */
    public void loadFromFile(String wordListFile)
    throws IOException
    {
        System.err.println("Loading filter list...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(wordListFile), "UTF-8"));
        String readLine = in.readLine();
        int count = 1;
        while (readLine != null)
        {
            //if (readLine.startsWith("xn--"))
            //  System.err.println(IDN.toUnicode(readLine));
            addAllSubstrings(readLine);
            if (count % 1000 == 0) System.err.print(count + " loaded.\r");
            readLine = in.readLine();
            count++;
        }
        in.close();
        System.err.println(count + " loaded. Done.");
        return;
    }

    /**
     * Adds one string so that filter can filter against all substrings of this
     * string.
     * @param string
     */
    public void addAllSubstrings(String string)
    {
        string = IDN.toUnicode(string, 0);
        String[] parts = string.split("[.-]");

        for (String part : parts)
            for (int i = 0; i < part.length(); i++)
                data.put(part.substring(i, part.length()), true);
    }

    /**
     * Evaluate single word.
     * @param word token to be evaluated
     * @return true if filterlist contains word as substring for any of the
     *         strings stored in the filter; false otherwise
     */
    public boolean isAccepted(String word)
    {
        return !data.prefixMap(word).isEmpty() && word.length() >= MINIMUM_LENGTH;
    }

    /**
     * Creates a filtered version of the wordlist.
     * @param inFile path to the wordlist file; expected format: one word per
     *               line with ignorable information after tab character
     *               (optional)
     * @param outFile path to the result file; result file will contain only
     *                lines where word before tab gives true by isAccepted()
     */
    public void filterList (String inFile, String outFile)
            throws IOException
    {
        System.err.println("Filtering file...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        String readLine = in.readLine();
        int countAll = 0;
        int countGood = 0;
        while (readLine != null)
        {
            countAll++;
            String[] parts = readLine.split("\t");
            if ((Lexicon.generateTranslitVariants(parts[0]).stream().map(var -> isAccepted(var))
                    .reduce(false, (a, b) -> a||b)))
            {
                out.write(readLine);
                out.newLine();
                countGood++;
            }
            if (countAll % 1000 == 0) System.err.print(countAll + " processed. " + countGood + " good.\r");
            readLine = in.readLine();
        }
        System.err.println(countAll + " processed. " + countGood + " good. Done.");
        in.close();
        out.flush();
        out.close();
    }

    /**
     * Comandline inteface. Call without parameters to get help.
     * @param args parameter list: 1) file against which filter; 2) wordlist to
     *             be filtered; 3) where to put result.
     * @throws IOException
     */
    public static void main (String[] args)
    throws IOException
    {
        if (args.length < 3)
        {
            System.err.println("To filter a wordlist, pass following parameters:");
            System.err.println("\t1) file against which filter;");
            System.err.println("\t2) wordlist to be filtered;");
            System.err.println("\t3) where to put result.");
            return;
        }
        Filter f = new Filter();
        f.loadFromFile(args[0]);
        f.filterList(args[1], args[2]);
    }
}
