package irs.java.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Indexing {

	static int number = 1;
	static int tnumber = 1;

	static Map<Integer, String> text = new HashMap<>();
	static Map<Integer, String> docid = new HashMap<>();

	static final Porter stemmer = new Porter();
	static final Set<String> stopWords = new HashSet<>();
	static final Map<String, Integer> wordsWithIds = new HashMap<>();

	static final Map<String, Entry<Integer, Map<Integer, Integer>>> invertedWordIndex = new HashMap<>();
	static final Map<Integer, Map<Integer, Integer>> forwardWordIndex = new HashMap<>();

	public static void main(String[] args) throws Exception {
		for (int i = 1; i <= 15; i++) {
			List<InputStream> root = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
					new FileInputStream("ft911_" + i + ".xml"), new ByteArrayInputStream("</root>".getBytes()));
			InputStream filedata = new SequenceInputStream(Collections.enumeration(root));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filedata);
			NodeList childnodes = document.getDocumentElement().getChildNodes();
			for (int j = 0; j < childnodes.getLength(); j++) {
				Node child = childnodes.item(j);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element node = (Element) child;
					NodeList list = node.getElementsByTagName("DOCNO");
					for (int k = 0; k < list.getLength(); k++) {
						Node docno = list.item(k);
						if (docno.getNodeType() == Node.ELEMENT_NODE) {
							Element tag = (Element) docno;
							docid.put(number, tag.getTextContent());
							number++;

						}
					}

					NodeList list1 = node.getElementsByTagName("TEXT");
					for (int k = 0; k < list1.getLength(); k++) {
						Node docno = list1.item(k);
						if (docno.getNodeType() == Node.ELEMENT_NODE) {
							Element tag = (Element) docno;
							text.put(tnumber, tag.getTextContent());
							tnumber++;

						}
					}
				}
			}
		}

		for (Entry<Integer, String> entry : text.entrySet()) {
			String doctext = entry.getValue().toLowerCase();
			String[] tokens = doctext.split("[^a-z]+");
			Set<String> stopWords = deleteStopWords(tokens);
			for (String word : stopWords) {
				String stemmedWord = stemmer.stripAffixes(word);
				if(stemmedWord.length() == 0)
				{
					continue;
				}
				processWordForInvIndex(stemmedWord, entry.getKey());
				processWordForFwdIndex(stemmedWord, entry.getKey());

			}
		}
		printInvIndex();
		printForwIndex();
		printWords();
		userInput();
	}

	public static Set<String> deleteStopWords(String[] tokens) throws Exception {

		if (stopWords.isEmpty()) {
			try (FileReader fr = new FileReader("stopwordlist.txt"); BufferedReader reader = new BufferedReader(fr)) {
				String value;
				while ((value = reader.readLine()) != null) {
					stopWords.add(value.trim().toLowerCase());
				}
				stopWords.add(null);
				stopWords.add("");
				stopWords.add(" ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Set<String> words = new HashSet<>();
		words.addAll(Arrays.asList(tokens));
		words.removeAll(stopWords);
		return words;
	}

	static void processWordForInvIndex(String word, int docId) {

		Entry<Integer, Map<Integer, Integer>> entry = invertedWordIndex.get(word);

		if (entry == null) {
			int wordId = invertedWordIndex.size() + 1;
			entry = new AbstractMap.SimpleImmutableEntry<>(wordId, new HashMap<>());
			invertedWordIndex.put(word, entry);
			wordsWithIds.put(word, wordId);
		}

		Map<Integer, Integer> map = entry.getValue();
		Integer wordFreq = map.get(docId);

		if (wordFreq == null) {
			wordFreq = 1;
		} else {
			wordFreq += 1;
		}

		map.put(docId, wordFreq);
	}

	static void processWordForFwdIndex(String word, int docId) {

		Map<Integer, Integer> entry = forwardWordIndex.get(docId);

		if (entry == null) {
			entry = new HashMap<>();
			forwardWordIndex.put(docId, entry);
		} else {
			if (entry.get(wordsWithIds.get(word)) == null) {
				int value = 1;
				entry.put(wordsWithIds.get(word), value);
				forwardWordIndex.put(docId, entry);
			} else {
				int freq = entry.get(wordsWithIds.get(word));
				int count = freq + 1;
				entry.put(wordsWithIds.get(word), count);
				forwardWordIndex.put(docId, entry);
			}
		}
	}

	private static void printInvIndex() {
		try (FileWriter fw = new FileWriter("inverted_index.txt", true);
				BufferedWriter writer = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(writer)) {

			for (Entry<String, Entry<Integer, Map<Integer, Integer>>> entry : invertedWordIndex.entrySet()) {
				int wordId = entry.getValue().getKey();
				String wordString = wordId + ":\t\t";
				for (Entry<Integer, Integer> mapEntry : entry.getValue().getValue().entrySet()) {
					int docId = mapEntry.getKey();
					int freq = mapEntry.getValue();

					String docString = docId + ": " + freq + "; ";
					wordString += docString;
				}
				out.println(wordString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printForwIndex() {
		try (FileWriter fw = new FileWriter("forward_index.txt", true);
				BufferedWriter writer = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(writer)) {

			for (Entry<Integer, Map<Integer, Integer>> entry : forwardWordIndex.entrySet()) {
				int docId = entry.getKey();
				String docString = docId + ":\t\t";
				for (Entry<Integer, Integer> mapEntry : entry.getValue().entrySet()) {
					int wordId = mapEntry.getKey();
					int freq = mapEntry.getValue();

					String wordString = wordId + ": " + freq + "; ";
					docString += wordString;
				}
				out.println(docString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printWords() {
		try (FileWriter fw = new FileWriter("words.txt", true);
				BufferedWriter writer = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(writer)) {

			for (Entry<String, Integer> entry : wordsWithIds.entrySet()) {

				out.println(entry.getKey() + ":\t\t" + entry.getValue());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void userInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = null;
		try {
			System.out.println("Please enter a word:");
			input = br.readLine();
		} catch (IOException e) {

			System.out.println("No input has been provided");
			e.printStackTrace();
		}
		search(input);
	}

	private static void search(String input) {

		int wordId = 0, freq = 0, tokenID;
		input = stemmer.stripAffixes(input.trim());
		Entry<Integer, Map<Integer, Integer>> map = invertedWordIndex.get(input);
		if (map != null) {
			String docString = "";
			tokenID = map.getKey();
			for (Entry<Integer, Integer> mapEntry : map.getValue().entrySet()) {
				wordId = mapEntry.getKey();
				freq = mapEntry.getValue();

				String wordString = wordId + ": " + freq + "; ";
				docString += wordString;
			}
			String result = tokenID + "           " + docString;

			System.out.println("Inverted Index");
			System.out.println("Word \t\t\t  Index");
			System.out.println(result);

		} else {
			System.out.println("No such word exists!");
		}
	}
}
