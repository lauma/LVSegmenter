package lv.ailab.segmenter;

import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.*;
import java.net.IDN;

/**
 * Created on 2015-05-07.
 *
 * @author Lauma
 */
public class Filter
{
    public static int MINIMUM_LENGTH = 2;
    public PatriciaTrie<Boolean> data = new PatriciaTrie<>();

    public static Filter loadFromFile(String wordListFile)
    throws IOException
    {
        Filter f = new Filter();
        System.out.println("Loading filter list...");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(wordListFile), "UTF-8"));
        String readLine = in.readLine();
        int count = 1;
        while (readLine != null)
        {
            //if (readLine.startsWith("xn--"))
            //  System.out.println(IDN.toUnicode(readLine));
            readLine = IDN.toUnicode(readLine, 0);
            String[] parts = readLine.split("[.-]");

            for (String part : parts)
                for (int i = 0; i < part.length(); i++)
                    f.data.put(part.substring(i, part.length()), true);

            if (count % 1000 == 0) System.out.print(count + " loaded.\r");
            readLine = in.readLine();
            count++;
        }
        in.close();
        System.out.println(count + " loaded. Done.");
        return f;
    }

    /**
     * Evaluate single word.
     * @param word token to be evaluated
     * @return true if filterlist contained word string as substring for any of
     *         the strings; false otherwise
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
        System.out.println("Filtering file...");
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
            if (isAccepted(parts[0]))
            {
                out.write(readLine);
                out.newLine();
                countGood++;
            }
            if (countAll % 1000 == 0) System.out.print(countAll + " processed. " + countGood + " good.\r");
            readLine = in.readLine();
        }
        System.out.println(countAll + " processed. " + countGood + " good. Done.");
        in.close();
        out.flush();
        out.close();
    }

    public static void main (String[] args)
    throws IOException
    {
        if (args.length < 3)
        {
            System.out.println("To filter a wordlist, pass following parameters:");
            System.out.println("\t1) file against which filter;");
            System.out.println("\t2) wordlist to be filtered;");
            System.out.println("\t3) where to put result.");
            return;
        }
        Filter.loadFromFile(args[0]).filterList(args[1], args[2]);
    }
}
