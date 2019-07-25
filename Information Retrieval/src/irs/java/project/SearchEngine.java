package irs.java.project;
/**
 * @author Sri Sravya Tirupachur Comerica
 * Date: 12/7/2018
 *
 *    
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SearchEngine {

	static Map<String, String> query_title = new HashMap<>();
	static Map<Integer, String> query_num = new HashMap<>();
	static Map<String, String> query_desc_title = new HashMap<>();
	static Map<String, String> query_narr_title = new HashMap<>();

	static Map<Integer, String> text = new HashMap<>();
	static Map<Integer, String> docid = new HashMap<>();

	static Map<String, Map<String, Integer>> query_title_res = new HashMap<>();
	static Map<String, Map<String, Integer>> query_desc_title_res = new HashMap<>();
	static Map<String, Map<String, Integer>> query_narr_title_res = new HashMap<>();

	static final Map<String, Entry<Integer, Map<Integer, Integer>>> invertedWordIndex = new HashMap<>();
	static final Map<String, Integer> wordsWithIds = new HashMap<>();

	static final Map<String, Map<Integer, Double>> tf_idf_title = new HashMap<>();
	static final Map<String, Map<Integer, Double>> tf_idf_desc_title = new HashMap<>();
	static final Map<String, Map<Integer, Double>> tf_idf_narr_title = new HashMap<>();

	static final Map<Integer, Double> tf_idf_normalize = new HashMap<>();
	static final Map<String, Double> tf_idf_query_normalize = new HashMap<>();
	static final Map<String, Double> relevance_title = new TreeMap<>();
	static final Map<String, Double> relevance_desc_title = new TreeMap<>();
	static final Map<String, Double> relevance_narr_title = new TreeMap<>();

	static final Map<String, Map<String, Double>> tf_idf_Query_title = new HashMap<>();
	static final Map<String, Map<String, Double>> tf_idf_Query_desc_title = new HashMap<>();
	static final Map<String, Map<String, Double>> tf_idf_Query_narr_title = new HashMap<>();

	static final List<Object> relevance_judgement = new ArrayList<Object>();

	static Map<Integer, Set<String>> relevant_docs = new HashMap<>();

	static final Porter stemmer = new Porter();

	static final Set<String> stopWords = new HashSet<>();

	static int number0 = 0;

	static int number1, number2, number3 = 0;

	static int number = 1;
	static int tnumber = 1;

	public static void main(String[] args) throws Exception {
		List<InputStream> root = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
				new FileInputStream("topics.xml"), new ByteArrayInputStream("</root>".getBytes()));
		InputStream filedata = new SequenceInputStream(Collections.enumeration(root));
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filedata);
		NodeList childnodes = document.getDocumentElement().getChildNodes();
		for (int j = 0; j < childnodes.getLength(); j++) {
			Node child = childnodes.item(j);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element node = (Element) child;

				NodeList list_num = node.getElementsByTagName("num");
				for (int k = 0; k < list_num.getLength(); k++) {
					Node title = list_num.item(k);
					if (title.getNodeType() == Node.ELEMENT_NODE) {
						Element tag = (Element) title;
						String truncate = tag.getTextContent();
						truncate = "   " + truncate.substring(8, 12);
						query_num.put(number0, truncate);
						number0++;
					}
				}
				NodeList list_title = node.getElementsByTagName("title");
				String title_text = "", Desc = "";
				for (int k = 0; k < list_title.getLength(); k++) {
					Node title = list_title.item(k);
					if (title.getNodeType() == Node.ELEMENT_NODE) {
						Element tag = (Element) title;
						query_title.put(query_num.get(number1), tag.getTextContent());
						title_text = tag.getTextContent();
						number1++;
					}
				}
				NodeList list_desc_title = node.getElementsByTagName("desc");
				for (int k1 = 0; k1 < list_desc_title.getLength(); k1++) {
					Node docno1 = list_desc_title.item(k1);
					if (docno1.getNodeType() == Node.ELEMENT_NODE) {
						Element tag = (Element) docno1;
						Desc = tag.getTextContent();
					}
					String result = title_text.concat(" ");
					result = result.concat(Desc);
					query_desc_title.put(query_num.get(number2), result);
					number2++;
				}

				NodeList list_narr_title = node.getElementsByTagName("narr");
				String narr = "";
				for (int k = 0; k < list_narr_title.getLength(); k++) {
					Node docno = list_narr_title.item(k);
					if (docno.getNodeType() == Node.ELEMENT_NODE) {
						Element tag = (Element) docno;
						narr = tag.getTextContent();
					}
				}
				String result = title_text.concat("");
				result = result.concat(narr);
				query_narr_title.put(query_num.get(number3), result);
				number3++;

			}
		}

		for (int i = 1; i <= 15; i++) {
			List<InputStream> root1 = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
					new FileInputStream("ft911_" + i + ".xml"), new ByteArrayInputStream("</root>".getBytes()));
			InputStream filedata1 = new SequenceInputStream(Collections.enumeration(root1));
			Document document1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filedata1);
			NodeList childnodes1 = document1.getDocumentElement().getChildNodes();
			for (int j = 0; j < childnodes1.getLength(); j++) {
				Node child = childnodes1.item(j);
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
				if (stemmedWord.length() == 0) {
					continue;
				}
				processWordForInvIndex(stemmedWord, entry.getKey());

			}
		}

		processQueries(query_title, query_title_res);
		processQueries(query_desc_title, query_desc_title_res);
		processQueries(query_narr_title, query_narr_title_res);
		calculate_tf_idf(invertedWordIndex, query_title_res, tf_idf_Query_title, tf_idf_title);
		calculate_tf_idf(invertedWordIndex, query_desc_title_res, tf_idf_Query_desc_title, tf_idf_desc_title);
		calculate_tf_idf(invertedWordIndex, query_narr_title_res, tf_idf_Query_narr_title, tf_idf_narr_title);
		normalization(tf_idf_title, tf_idf_Query_title);
		normalization(tf_idf_desc_title, tf_idf_Query_desc_title);
		normalization(tf_idf_narr_title, tf_idf_Query_narr_title);
		cosinesimilarity(tf_idf_title, tf_idf_Query_title, relevance_title);
		cosinesimilarity(tf_idf_desc_title, tf_idf_Query_desc_title, relevance_desc_title);
		cosinesimilarity(tf_idf_narr_title, tf_idf_Query_narr_title, relevance_narr_title);
		relevance_judgement();
		sortValues(relevance_title, relevance_desc_title, relevance_narr_title);
	}

	// Reads the main.qrels file and stores the values that are relevant into relevance_judgement list
	static void relevance_judgement() {
		try (BufferedReader br = new BufferedReader(new FileReader("main.qrels"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("352") || line.startsWith("353") || line.startsWith("354")
						|| line.startsWith("359") && line.contains("FT911")) {
					String sub = line.substring(14, line.length());
					if (sub.contains(" 1")) { //only if line contains 1
						relevance_judgement.add(line);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// Deletes Stopwords
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
	
	// Pre processes the text and converts them into stemmed words from topics.txt files
	public static void processQueries(Map<String, String> query, Map<String, Map<String, Integer>> Name)
			throws Exception {

		for (Entry<String, String> entry : query.entrySet()) {
			String doctext = entry.getValue().toLowerCase();
			String[] tokens = doctext.split("[^a-z]+");
			Set<String> Words = deleteStopWords(tokens);
			for (String word : Words) {
				String stemmedWord = stemmer.stripAffixes(word);
				if (stemmedWord.length() == 0) {
					continue;
				}
				processWordForInvIndexQueries(stemmedWord, entry.getKey(), Name); //calls inverted index for queries
			}
		}
	}

	// Creates an inverted index for the document collection
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

	// Creates an inverted index for the words in the queries with respect to their document numbers
	static void processWordForInvIndexQueries(String word, String docId, Map<String, Map<String, Integer>> Name) {

		Map<String, Integer> entry = Name.get(word);

		if (entry == null) {
			entry = new HashMap<>();
			Name.put(word, entry);
		}

		Integer wordFreq = entry.get(docId);

		if (wordFreq == null) {
			wordFreq = 1;
		} else {
			wordFreq += 1;
		}

		entry.put(docId, wordFreq);
	}
	
	//Calculates the tf_idf for terms in both queries and documents
	public static void calculate_tf_idf(Map<String, Entry<Integer, Map<Integer, Integer>>> invertedwordindex,
			Map<String, Map<String, Integer>> Name, Map<String, Map<String, Double>> tfidfquery,
			Map<String, Map<Integer, Double>> tfidf) {

		for (Entry<String, Map<String, Integer>> entry : Name.entrySet()) {

			Entry<Integer, Map<Integer, Integer>> map = invertedwordindex.get(entry.getKey());
			Map<Integer, Double> calculated_tf_idf = new HashMap<>();
			Map<String, Double> calculated_tf_idf_query = new HashMap<>();
			int df;
			Double idf = 0.0;
			if (map != null) {
				df = map.getValue().size();
				idf = Math.log10(docid.size() / df); // divides the total number of documents in collection by df of a term
				Map<Integer, Integer> map2 = map.getValue();
				for (Entry<Integer, Integer> entry1 : map2.entrySet()) {
					int tf = entry1.getValue();
					Double tf_idf = tf * idf; //multiplies the values of idf to tf of the term in document
					calculated_tf_idf.put(entry1.getKey(), tf_idf);
				}
				tfidf.put(entry.getKey(), calculated_tf_idf);

				Map<String, Integer> map3 = entry.getValue();
				for (Entry<String, Integer> entry2 : map3.entrySet()) {
					int tf = entry2.getValue(); 
					Double tf_idf = tf * idf; //multiples the values of idf to the tf of query
					calculated_tf_idf_query.put(entry2.getKey(), tf_idf);
				}

				tfidfquery.put(entry.getKey(), calculated_tf_idf_query);
			}
		}

	}
	
	// Finds the euclidean distance with respect to each document and query and divides each tf_idf values in both query an document with the euclidean distance found 
	public static void normalization(Map<String, Map<Integer, Double>> tfIdf,
			Map<String, Map<String, Double>> tfIdfQueryTitle) {

		for (Entry<String, Map<Integer, Double>> entry : tfIdf.entrySet()) {

			Map<Integer, Double> map = entry.getValue();

			for (Entry<Integer, Double> entry2 : map.entrySet()) {

				Double tf_idf = entry2.getValue();
				Double prev = tf_idf_normalize.get(entry2.getKey());
				if (prev == null) {
					Double squared = Math.pow(tf_idf, 2);
					tf_idf_normalize.put(entry2.getKey(), squared);
				} else {
					Double squared = Math.pow(tf_idf, 2);
					prev = prev + squared; //squares and adds the values with respect to the document in document collection
					tf_idf_normalize.put(entry2.getKey(), prev);
				}

			}
		}

		for (Entry<Integer, Double> entry1 : tf_idf_normalize.entrySet()) {

			Double do_squareroot = entry1.getValue();
			Double result = Math.sqrt(do_squareroot); //Squareroot is applied to the sum of squares
			tf_idf_normalize.put(entry1.getKey(), result);
		}

		for (Entry<String, Map<Integer, Double>> entry5 : tfIdf.entrySet()) {

			Map<Integer, Double> map1 = entry5.getValue();

			for (Entry<Integer, Double> entry6 : map1.entrySet()) {

				Double normalization = tf_idf_normalize.get(entry6.getKey());
				Double tf_idf = entry6.getValue();
				if (tf_idf != null && normalization != null || tf_idf != 0.0 && normalization != 0.0) {
					Double divide_tf_idf = tf_idf / normalization;
					if (divide_tf_idf != null) {
						map1.put(entry6.getKey(), divide_tf_idf);
					}
				}
			}

		}

		for (Entry<String, Map<String, Double>> entry7 : tfIdfQueryTitle.entrySet()) {

			Map<String, Double> map2 = entry7.getValue();

			for (Entry<String, Double> entry8 : map2.entrySet()) {
				Double prev = 0.0;
				Double tf_idf = entry8.getValue();
				prev = tf_idf_query_normalize.get(entry8.getKey());
				if (prev == null) {
					Double squared = Math.pow(tf_idf, 2);
					tf_idf_query_normalize.put(entry8.getKey(), squared);
				} else {
					Double squared = Math.pow(tf_idf, 2);
					prev += prev + squared;	//squares and adds the values with respect to the query in query collection
					tf_idf_query_normalize.put(entry8.getKey(), prev);
				}

			}
		}

		for (Entry<String, Double> entry9 : tf_idf_query_normalize.entrySet()) {

			Double do_squareroot = entry9.getValue();
			Double result = Math.sqrt(do_squareroot); //Squareroot is applied to the sum of squares
			tf_idf_query_normalize.put(entry9.getKey(), result);
		}

		for (Entry<String, Map<String, Double>> entry10 : tfIdfQueryTitle.entrySet()) {

			Map<String, Double> map3 = entry10.getValue();

			for (Entry<String, Double> entry11 : map3.entrySet()) {

				Double normalization = tf_idf_query_normalize.get(entry11.getKey());
				Double tf_idf = entry11.getValue();
				if (tf_idf != null && normalization != null) {
					Double divide_tf_idf = tf_idf / normalization; //divides each tf*idf by the normalized values
					if (divide_tf_idf != null) {
						map3.put(entry11.getKey(), divide_tf_idf);
					}
				}

			}

		}

	}
	
  //finds cosine similarity between document and query
	private static void cosinesimilarity(Map<String, Map<Integer, Double>> tfIdf,
			Map<String, Map<String, Double>> tfIdfQuery, Map<String, Double> relevance) {

		for (Entry<String, Map<Integer, Double>> entry : tfIdf.entrySet()) {

			Map<Integer, Double> map = entry.getValue();

			Map<String, Double> map1 = tfIdfQuery.get(entry.getKey());

			for (Entry<Integer, Double> entry2 : map.entrySet()) {
				for (Entry<String, Double> entry3 : map1.entrySet()) {

					Double doc = entry2.getValue();
					Double query = entry3.getValue();
					Double relevancy = doc * query;
					String doc_name = entry2.getKey().toString();
					doc_name = " FT911-" + doc_name;
					String relevancy_result = entry3.getKey();
					relevancy_result = relevancy_result.concat(doc_name);
					Double prev = relevance.get(relevancy_result);

					if (prev == null) {
						relevance.put(relevancy_result, relevancy);
					} else {
						prev = prev + relevancy;  
						relevance.put(relevancy_result, prev);
					}
				}
			}
		}
	}
	
	//sorts the values found in descending order
	public static void sortValues(Map<String, Double> relevanceTitle, Map<String, Double> relevanceDescTitle,
			Map<String, Double> relevanceNarrTitle) {

		Map<Object, Object> relevance_title_sorted = relevanceTitle.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		print(relevance_title_sorted, "vsm_output_title.txt");

		Map<Object, Object> relevance_desc_title_sorted = relevanceDescTitle.entrySet().stream()
				.sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		print(relevance_desc_title_sorted, "vsm_output_title_desc.txt");

		Map<Object, Object> relevance_narr_title_sorted = relevanceNarrTitle.entrySet().stream()
				.sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		print(relevance_narr_title_sorted, "vsm_output_title_narr.txt");

	}
	

	//Writes the sorted values as per query and query setting into the files
	static void print(Map<Object, Object> sortedMap, String filename) {
		int total_relevant_docs = 0;
		try (FileWriter fw = new FileWriter(filename, true);
				BufferedWriter writer = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(writer)) {
			DecimalFormat numberFormat = new DecimalFormat("0.000000");
			out.println("==================================================================================");
			out.println("Query  Doc Number   Rank      Similarity");
			int rank = 1;
			List<Object> keyList = new ArrayList<Object>(sortedMap.keySet());
			for (int i = keyList.size() - 1; i >= 0; i--) {
				Double value = (Double) sortedMap.get(keyList.get(i));

				String j = (String) keyList.get(i);
				String query_no = j.substring(4, j.length());
				j = j.substring(4, 7);
				if (j.equals("352")) {
					out.println(query_no+ "       " + rank + "            " + numberFormat.format(value));
					rank++;
				}
			}		
			for(int i=0; i<relevance_judgement.size(); i++) {
				String t= (String) relevance_judgement.get(i);
				if(t.contains("352 0 FT911")) {
				total_relevant_docs = total_relevant_docs+1;
				}
			}
			calculate_precision_recall(filename, 352, sortedMap, rank, total_relevant_docs);
			total_relevant_docs =0;
			rank = 1;
			out.println("==================================================================================");
			out.println("Query  Doc Number   Rank      Similarity");
			for (int i = keyList.size() - 1; i >= 0; i--) {
				Double value = (Double) sortedMap.get(keyList.get(i));
				String j = (String) keyList.get(i);
				String query_no = j.substring(4, j.length());
				j = j.substring(4, 7);
				if (j.equals("353")) {
					out.println(query_no + "          " + rank + "            " + numberFormat.format(value));
					rank++;
				}
			}
			for(int i=0; i<relevance_judgement.size(); i++) {
				String t= (String) relevance_judgement.get(i);
				if(t.contains("353 0 FT911")) {
				total_relevant_docs = total_relevant_docs+1;
				}
			}
			calculate_precision_recall(filename, 353, sortedMap, rank, total_relevant_docs);
			total_relevant_docs =0;
			rank = 1;
			out.println("==================================================================================");
			out.println("Query  Doc Number   Rank      Similarity");
			for (int i = keyList.size() - 1; i >= 0; i--) {
				Double value = (Double) sortedMap.get(keyList.get(i));
				if (value > 0.0) {
					String j = (String) keyList.get(i);
					String query_no = j.substring(4, j.length());
					j = j.substring(4, 7);
					if (j.equals("354")) {
						out.println(query_no + "       " + rank + "            " + numberFormat.format(value));
						rank++;
					}
				}
			}
			for(int i=0; i<relevance_judgement.size(); i++) {
				String t= (String) relevance_judgement.get(i);
				if(t.contains("354 0 FT911")) {
				total_relevant_docs = total_relevant_docs+1;
				}
			}
			calculate_precision_recall(filename, 354, sortedMap, rank, total_relevant_docs);
			total_relevant_docs =0;
			rank = 1;
			out.println("==================================================================================");
			out.println("Query  Doc Number   Rank      Similarity");
			for (int i = keyList.size() - 1; i >= 0; i--) {
				Double value = (Double) sortedMap.get(keyList.get(i));
				if (value > 0.0) {
					String j = (String) keyList.get(i);
					String query_no = j.substring(4, j.length());
					j = j.substring(4, 7);
					if (j.equals("359")) {
						out.println(query_no+ "       " + rank + "            " + numberFormat.format(value));
						rank++;
					}
				}
			}
			for(int i=0; i<relevance_judgement.size(); i++) {
				String t= (String) relevance_judgement.get(i);
				if(t.contains("359 0 FT911")) {
				total_relevant_docs = total_relevant_docs+1;
				}
			}
			calculate_precision_recall(filename, 359, sortedMap, rank, total_relevant_docs);
			total_relevant_docs =0;
			rank = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Calculates precision and recall
	static void calculate_precision_recall(String filename, int query, Map<Object, Object> sortedMap, int retreieved_docs, int total_relevant_docs) {
		List<Object> keyList = new ArrayList<Object>(sortedMap.keySet());
		DecimalFormat numberFormat = new DecimalFormat("0.000000");
		for (int i = keyList.size() - 1; i >= 0; i--) {

			String index = (String) keyList.get(i);
			index = index.substring(4, index.length());
			String relevant = index.substring(0, 4) + "0 " + index.substring(4, index.length()) + " 1";
			if (relevance_judgement.contains(relevant)) {

				int query_no = Integer.parseInt(index.substring(0, 3));
				String doc_no = (index.substring(4, index.length()));
				Set<String> map = relevant_docs.get(query_no);
				if (map == null) {
					Set<String> map1 = new HashSet<>();
					map1.add(doc_no);
					relevant_docs.put(query_no, map1);
				} else {
					map.add(doc_no);
					relevant_docs.put(query_no, map);
				}

			}
		}
		      filename = filename.substring(11,filename.length()-4 );
			  Set<String> map = relevant_docs.get(query);
			  System.out.println(filename+"  Query:"+query +"  Precision: "+numberFormat.format(((double)map.size()/retreieved_docs)));
			  
			  System.out.println(filename+"  Query:"+query +"   Recall: "+numberFormat.format(((double)map.size()/total_relevant_docs)));
			  
	  System.out.println("\n");
	}

}