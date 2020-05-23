package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import indexing.IndexBuilder;


/**
 * @author ericfouh
 */
public class TestIndexBuilder
{
	/**
	 * Store the contents of the pages in a map and test:
	 * - The map has the correct number of files
	 * - The map contains the names of the documents (URLs/keys)
	 * - The map contains the correct number of terms in the lists (values)
	 */
	@Test
	public void testParseFeed() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, List<String>> map = ib.parseFeed(feeds);
		assertEquals(5, map.size());
		assertTrue(map.containsKey("http://localhost:8090/page1.html"));
		assertTrue(map.containsKey("http://localhost:8090/page5.html"));
		assertEquals(10, map.get("http://localhost:8090/page1.html").size());
		assertEquals(18, map.get("http://localhost:8090/page5.html").size());
	}
	
	/**
	 * Test if the words in the index map has correct TF-IDF value
	 */
	@Test
	public void testBuildIndex() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = ib.buildIndex(ib.parseFeed(feeds));
		Double d1 = 0.183;
		assertEquals(index.get("http://localhost:8090/page1.html").get("structures"), d1, 0.001);
		Double d2 = 0.0731;
		assertEquals(index.get("http://localhost:8090/page4.html").get("mallarme"), d2, 0.001);
	}
	
	/**
	 * Build an inverted-index map for each word and test:
	 * - The map is of the correct type (of Map)
	 * - The map associates the correct files to a term 
	 * - The map stores the documents in the correct order
	 */
	@Test
	public void testBuildInvertedIndex() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = ib.buildIndex(ib.parseFeed(feeds));
		Map<?,?> invertedIndex = ib.buildInvertedIndex(index);
		assertEquals(invertedIndex.getClass().toString(), "class java.util.HashMap");
		@SuppressWarnings("unchecked")
		Map<String, List<Map.Entry<String, Double>>> invertedIndexes = (Map<String, List<Map.Entry<String, Double>>>) invertedIndex;
		assertEquals(invertedIndexes.get("data").get(0).getKey(), "http://localhost:8090/page1.html");
		Double d1 = 0.1021;
		assertEquals(invertedIndexes.get("data").get(0).getValue(), d1, 0.001);
		assertEquals(invertedIndexes.get("data").get(1).getKey(), "http://localhost:8090/page2.html");
		Double d2 = 0.0464;
		assertEquals(invertedIndexes.get("data").get(1).getValue(), d2, 0.001);
		assertEquals(invertedIndexes.get("java").get(0).getKey(), "http://localhost:8090/page3.html");
		Double d3 = 0.0487;
		assertEquals(invertedIndexes.get("java").get(0).getValue(), d3, 0.001);
	}
	
	/**
	 * Build the home page words collection and test:
	 * - The Collection is of the correct type
	 * - The collection stores the entries are in the correct order
	 */
	@Test
	public void testBuildHomePage() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = ib.buildIndex(ib.parseFeed(feeds));
		Collection<Entry<String, List<String>>> homepage = ib.buildHomePage(ib.buildInvertedIndex(index));
		assertEquals(homepage.getClass().toString(), "class java.util.ArrayList");
		assertEquals(((List<Entry<String, List<String>>>) homepage).get(0).getKey(), "data");
		assertEquals(((List<Entry<String, List<String>>>) homepage).get(0).getValue().get(0), "http://localhost:8090/page1.html");
		assertEquals(((List<Entry<String, List<String>>>) homepage).get(4).getKey(), "queues");
	}
	
	/**
	 * Test if the method searchArticles can return a correct list that:
	 * - The list contains the correct number of articles
	 * - The list contains the correct of articles
	 */
	@Test
	public void testSearchArticles() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = ib.buildIndex(ib.parseFeed(feeds));
		assertEquals(ib.searchArticles("structures", ib.buildInvertedIndex(index)).size(), 2);
		assertEquals(ib.searchArticles("structures", ib.buildInvertedIndex(index)).get(0), "http://localhost:8090/page1.html");
		assertEquals(ib.searchArticles("random", ib.buildInvertedIndex(index)).size(), 1);
		assertEquals(ib.searchArticles("random", ib.buildInvertedIndex(index)).get(0), "http://localhost:8090/page5.html");
	}
	
	/**
	 * Create the auto-complete file and test:
	 * - The collection is of the correct type
	 * - The collection contains the correct number of words
	 */
	@Test
	public void testCreateAutocompleteFile() {
		IndexBuilder ib = new IndexBuilder();
		List<String> feeds = new ArrayList<>();
		feeds.add("http://localhost:8090/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = ib.buildIndex(ib.parseFeed(feeds));
		Collection<Entry<String, List<String>>> homepage = ib.buildHomePage(ib.buildInvertedIndex(index));
		Collection<?> collection = ib.createAutocompleteFile(homepage);
		assertEquals(collection.getClass().toString(), "class java.util.ArrayList");
		assertEquals(collection.size(), 57);
	}
	
}
