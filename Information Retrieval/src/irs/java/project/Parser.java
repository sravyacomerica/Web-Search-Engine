package irs.java.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Parser {

	private final Porter stemmer = new Porter();
	private final List<String> stopWords = new ArrayList<>();
	private final Set<String> stemmedWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); // new
																							// TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private int id = 1;
	int number = 1;
	int tnumber = 1;
	static Map<Integer, String> result = new LinkedHashMap<>();
	static Map<Integer, String> docid = new LinkedHashMap<>();

	public static void main(String[] args) throws Exception {
		Parser word = new Parser();
		word.execute();
	}

	public void execute() throws Exception {
		Map<Integer, String> text = new LinkedHashMap<>();

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
			deleteStopWords(tokens);
		}
		stemmedWords.remove("");
		System.out.println(stemmedWords.size());
		printWords(stemmedWords);	
	}
	
	public LinkedHashMap<Integer, String> Stemmed() {
		
		return (LinkedHashMap<Integer, String>) result;		
	}

	void deleteStopWords(String[] tokens) throws Exception {

		if (stopWords.isEmpty()) {
			try (FileReader fr = new FileReader("stopwordlist.txt"); BufferedReader reader = new BufferedReader(fr)) {
				String value;
				while ((value = reader.readLine()) != null) {
					stopWords.add(value.trim().toLowerCase());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<String> words = new ArrayList<>();
		words.addAll(Arrays.asList(tokens));
		words.removeAll(stopWords);

		for (String word : words) {
			String stemmedWord = stemmer.stripAffixes(word);
			stemmedWords.add(stemmedWord);
		}
	}

	void printWords(Set<String> words) {
		try (FileWriter fw = new FileWriter("parser_output.txt", true);
				BufferedWriter writer = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(writer)) {
			out.println("Token  TokenID");
			for (String value : words) {
				result.put(id, value);
				id++;
			}
			for (Entry<Integer, String> entry : result.entrySet()) {
				out.println(entry.getValue() + "     " + entry.getKey());
			}
			out.println("---------------------------");
			out.println("Document   DocID");
			for (Entry<Integer, String> entry : docid.entrySet()) {
				out.println(entry.getValue() + "     " + entry.getKey());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
