package indexing;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexBuilder implements IIndexBuilder {

	@Override
	public Map<String, List<String>> parseFeed(List<String> feeds) {
		Map<String, List<String>> map = new HashMap<>();
		try {
			for(String feed: feeds) {
				Document doc = Jsoup.connect(feed).get();
				Elements links = doc.getElementsByTag("link");
				for(Element link: links) {
					String linkText = link.text();
					map.put(linkText, new ArrayList<>());
					Document  page = Jsoup.connect(linkText).get();
					Elements body = page.getElementsByTag("body");
					String s = body.text().replaceAll("\\p{Punct}", "");
					String[] contents = s.replaceAll("(^\\\\s+|\\\\s+$)", "").toLowerCase().split("\\s+");
					map.get(linkText).addAll(Arrays.asList(contents));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return map;
	}

	@Override
	public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
		Map<String, Map<String, Double>> index = new HashMap<>();
		Map<String, Map<String, Integer>> map = new HashMap<>();
		Map<String, Integer> totalCount = new HashMap<>();
		for(Map.Entry<String, List<String>> entry: docs.entrySet()) {
			Map<String, Integer> count = new HashMap<>();
			for(String word: entry.getValue()) {
				count.putIfAbsent(word, 0);
				count.put(word, count.get(word) + 1);				
			}
			for(Map.Entry<String, Integer> word: count.entrySet()) {
				totalCount.putIfAbsent(word.getKey(), 0);
				totalCount.put(word.getKey(), totalCount.get(word.getKey()) + 1);
			}
			map.put(entry.getKey(), count);
		}
		for(Map.Entry<String, Map<String, Integer>> entry: map.entrySet()) {
			int total = docs.size();
			int number = docs.get(entry.getKey()).size();
			Map<String, Double> res = new TreeMap<>();
			for(Map.Entry<String, Integer> word: entry.getValue().entrySet()) {
				double TF = (double)word.getValue() / number;
				double IDF = Math.log((double)total / totalCount.get(word.getKey()));
				res.put(word.getKey(), TF * IDF);				
			}
			index.put(entry.getKey(), res);
		}
		return index;
	}

	@Override
	public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
		Map<String, List<Map.Entry<String, Double>>> invertedIndex = new HashMap<String, List<Map.Entry<String, Double>>>();
		for(Map.Entry<String, Map<String, Double>> entry: index.entrySet()) {
			for(Map.Entry<String, Double> word: entry.getValue().entrySet()) {
				Map.Entry<String, Double> e = new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), word.getValue());
				invertedIndex.putIfAbsent(word.getKey(), new ArrayList<Map.Entry<String, Double>>());
				invertedIndex.get(word.getKey()).add(e);
			}
		}
		for(Map.Entry<String, List<Map.Entry<String, Double>>> entry: invertedIndex.entrySet()){
			Collections.sort(entry.getValue(), new Comparator<Entry<String, Double>>() {
				public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
	        		return e2.getValue().compareTo(e1.getValue());
	        	}
			});
		}
		return invertedIndex;
	}

	@Override
	public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
		List<Entry<String, List<String>>> homepage = new ArrayList<Map.Entry<String, List<String>>>();
		for(Map.Entry<?, ?> entry: invertedIndex.entrySet()) {
			if(IIndexBuilder.STOPWORDS.contains(entry.getKey())) {
				continue;
			}
			Map.Entry<String, List<String>> e = new AbstractMap.SimpleEntry<String, List<String>>((String) entry.getKey(), new ArrayList<String>());
			homepage.add(e);
			@SuppressWarnings("unchecked")
			List<Map.Entry<String, Double>> docs = (List<Entry<String, Double>>) entry.getValue();
			for(Map.Entry<String, Double> doc: docs) {
				e.getValue().add(doc.getKey());
			}
		}
		
		Collections.sort(homepage, new Comparator<Entry<String, List<String>>>() {
			public int compare(Entry<String, List<String>> e1, Entry<String, List<String>> e2) {
        		if(e1.getValue().size() != e2.getValue().size()) {
        			return e2.getValue().size() - e1.getValue().size();
        		}
        		return e2.getKey().compareTo(e1.getKey());
        	}
		});
		
		return homepage;
	}

	@Override
	public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
		List<String> list = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<Map.Entry<String, Double>> docs = (List<Entry<String, Double>>) invertedIndex.get(queryTerm);
		if(docs == null) {
			return list;
		}
		for(Map.Entry<String, Double> doc: docs) {
			list.add(doc.getKey());
		}
		return list;
	}
	
	@Override
	public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {
		Collection<String> collection = new ArrayList<String>();
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("autocomplete.txt"));
			out.write(homepage.size());
			out.newLine();
			for(Map.Entry<String, List<String>> entry: homepage) {
				out.write(0);
				out.write(" ");
				out.write(entry.getKey());
				collection.add(entry.getKey());
				out.newLine();
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort((ArrayList<String>) collection);
		return collection;
	}

}