package autocomplete;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Autocomplete implements IAutocomplete {
	private Node root;
	private int maxSuggestion;
		
	/**
	 * Initializes the Autocomplete, set the root by calling Node Constructor
	 */
	public Autocomplete() {
		root = new Node("", 0);
	}

	@Override
	public void addWord(String word, long weight) {
		if(word.length() == 0) {
			return;
		}
		if(!word.matches("^[a-z]*$")) {
			return;
		}
		String str = word.toLowerCase();		
		char[] s = str.toCharArray();
		Node cur = root;
		for(int i = 0; i < s.length; i++) {
			int c = s[i] - 'a';
			if((cur.getReferences())[c] == null) {
				cur.getReferences()[c] = new Node();
			} 
			cur.setPrefixes(cur.getPrefixes() + 1);
			cur = cur.getReferences()[c];			
		}
		cur.setWords(cur.getWords() + 1);
		cur.setPrefixes(cur.getPrefixes() + 1);
		cur.setTerm(new Term(word, weight));
	}

	@Override
	public Node buildTrie(String filename, int k) {
		maxSuggestion = k;
		try {
			Reader in = new FileReader(filename);
			BufferedReader reader = new BufferedReader(in);			
			String s = reader.readLine();;
			while((s = reader.readLine()) != null) {
				String[] c = s.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
				if (c.length >= 2) {
					long weight = Long.parseLong(c[0]);
					String word = c[1].toLowerCase();
					if (word.matches("^[a-z]*$")) {
						addWord(word, weight);
					}
				}			
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return root;
	}

	@Override
	public int numberSuggestions() {
		return maxSuggestion;
	}

	@Override
	public Node getSubTrie(String prefix) {	
		return find(prefix, root);
	}
	
	/**
	 * helper function to get subtrie recursively
	 * @param prefix the prefix of a subtrie
	 * @param cur the current node
	 * @return the root of subtrie
	 */
	private Node find(String prefix, Node cur) {
		if(prefix.length() == 0) {
			return cur;
		}
		String str = prefix.toLowerCase();
		Character c = str.charAt(0);
		if(!Character.isLetter(c)) {
			return null;
		}
		if(cur.getReferences()[c - 'a'] == null) {
			return null;
		} 
		cur = cur.getReferences()[c - 'a'];
		return find(prefix.substring(1), cur);
	}

	@Override
	public int countPrefixes(String prefix) {
		if(prefix == null) {
			return 0;
		}
		if(!prefix.matches("^[a-z]*$")) {
			return 0;
		}
		String str = prefix.toLowerCase();
		Node cur = getSubTrie(str);
		return cur.getPrefixes();
	}

	@Override
	public List<ITerm> getSuggestions(String prefix) {
		List<ITerm> list = new ArrayList<>();
		Node subtrie = getSubTrie(prefix);
		getWords(subtrie, list);
		return list;
	}
	
	/**
	 * helper function to get the list of words of a prefix
	 * @param node the root of a subtrie
	 * @param list the list of the words with the prefix
	 */
	private void getWords(Node node, List<ITerm> list) {
		if(node == null) {
			return;
		}
		if (node.getWords() != 0) {
			list.add(node.getTerm());
		}
		for(int i = 0; i < 26; i++) {
			Node cur = node.getReferences()[i];
			if(cur == null) {
				continue;
			} 
			getWords(cur, list);
		}
	}
	
	/**
	 * to get the root of the trie
	 * @return the root of of the trie
	 */
	public Node getRoot() {
		return root;
	}
}
