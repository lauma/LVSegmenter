package lv.ailab.wordembeddings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lv.ailab.segmenter.datastruct.Lexicon;

public class WordEmbeddings {
	private Map<String, double[]> vectors = new HashMap<String, double[]>();
	int vector_size;
	
	public double[] getVector(String word) {
		return vectors.get(word);
	}
	
	public WordEmbeddings(String filename) throws Exception {
		System.err.println(String.format("Loading word embeddings from %s", filename));
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        String line = in.readLine();
        String[] parts = line.split(" ");
        if (parts.length != 2) {
        	in.close();
        	throw new Exception(String.format("Error when opening word embedding file %s.\nExpected 2 columns in first line, had %d", 
        			filename, parts.length));
        }
        vector_size = Integer.parseInt(parts[1]);
        
        line = in.readLine();
        int count = 0;
        while (line != null) {
            parts = line.split(" ");
            line = in.readLine();
        	count++;

        	if (parts.length != vector_size + 1) {
            	in.close();
            	throw new Exception(String.format("Error when opening word embedding file %s.\nExpected %d columns in every line, had %d in line #%d", 
            			filename, vector_size, parts.length, count));
            }            
            String word = parts[0];
            
            // don't include embeddings for numbers etc
            if (!word.matches("(\\p{L}\\p{M}*)+")) 
            	continue;
            
            double[] vector = new double[vector_size];
            for (int i=1; i<parts.length; i++) {
            	vector[i-1] = Double.parseDouble(parts[i]);
            }
            vectors.put(word, vector); 
            if (count % 10000 == 0) System.err.print(count + " loaded.\r");
        }
        in.close();
        System.err.println(count + " lines of word embeddings loaded. Done.");
	}
	
	public double wordSimilarity(String wordA, String wordB) throws Exception {
		double vectorA[] = vectors.get(wordA);
		double vectorB[] = vectors.get(wordB);
		if (vectorA == null)
			throw new Exception(String.format("wordSimilarity : word %s not found in word embeddings list", wordA));
		if (vectorB == null)
			throw new Exception(String.format("wordSimilarity : word %s not found in word embeddings list", wordB));
		return cosineSimilarity(vectorA, vectorB);
	}
	
	// Blatantly copied from http://stackoverflow.com/a/22913525/366553
	private static double cosineSimilarity(double[] vectorA, double[] vectorB) throws Exception {
		if (vectorA.length != vectorB.length) 
			throw new Exception(String.format("Cosine similarity called for vectors of different length - %d and %d", vectorA.length, vectorB.length));
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	public List<String> similarWords(String word) throws Exception {
		return similarWords(word, 30);  // default value
	}
		
	public List<String> similarWords(String word, int topValues) throws Exception {
		String[] result = new String[topValues];
		double[] result_cosines = new double[topValues];
		double[] vectorA = vectors.get(word);
		// if one of words is not in dictionary, we return an empty list of similar words
		if (vectorA == null) return new ArrayList<String>();
		
		double worstValue = -1;
		int worstIndex = 0;
		for (Entry<String, double[]> candidate : vectors.entrySet()) {
			if (candidate.getKey().equalsIgnoreCase(word)) 
				continue;
			double cosine = cosineSimilarity(vectorA, candidate.getValue());
			
			if (cosine > worstValue) {
				//TODO - pašreiz atgriež sarakstu ar N labākajiem, bet tas saraksts nav sakārtots pēc labuma! šajā posmā veicot kaut vai 'bubble sort' to varētu izdarīt
				result[worstIndex] = candidate.getKey();
				result_cosines[worstIndex] = cosine;
				
				// Find the new worst item
				worstValue = 2; // larger than the maximum possible of 1
				for (int i=0; i<topValues;i++) {
					if (result[i] == null) {
						// no item here yet, this should be replaced with whatever comes in
						worstIndex = i;
						worstValue = -1;
						break;
					}
					if (result_cosines[i] < worstValue) {
						worstIndex = i;
						worstValue = result_cosines[i];
					}
				}
			}
		}
		
		return Arrays.asList(result);
	}
	
	public void addToLexicon(Lexicon lex, String language){
		for (String word : vectors.keySet()) {
			if (word.length() > 2)
				lex.addWord(word, word, language);
		}
	}
}
