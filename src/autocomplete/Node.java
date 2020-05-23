package autocomplete;

/**
 * ==== Attributes ====
 * - words: number of words
 * - term: the ITerm object
 * - prefixes: number of prefixes 
 * - references: Array of references to next/children Nodes
 * 
 * ==== Constructor ====
 * Node(String word, long weight)
 * 
 * @author yanya
 */
public class Node
{
    private ITerm term;
    private int words;
    private int prefixes;
    private Node[] references;
    
    /**
     * Initializes a Node with the given query string and weight (calls the Term constructor).
     * @param query the word to add to a trie
     * @param weight the weight of the specific word
     */
    public Node(String query, long weight) {
    	if(query == null || weight < 0) {
			throw new IllegalArgumentException();
		}
    	setTerm(new Term(query, weight));
    	setWords(0);
    	setPrefixes(0);
    	setReferences(new Node[26]);
    }
    
    /**
     * another constructor that takes in no parameters for those node 
     * do not need to store words
     */
    public Node() {
    	setTerm(null);
    	setWords(0);
    	setPrefixes(0);
    	setReferences(new Node[26]);
    }

	/**
	 * get the term of the node
	 * @return the term associated with the Node
	 */
    public Term getTerm() {
		return (Term)term;
	}

	/**
	 * set the term of a ndoe
	 * @param term the term to be set
	 */
    public void setTerm(ITerm term) {
		this.term = term;
	}

	/**
	 * get the words count of a node
	 * @return the number of words associated with the node
	 */
    public int getWords() {
		return words;
	}

	/**
	 * set the number of words of a node
	 * @param words the words number to be set
	 */
    public void setWords(int words) {
		this.words = words;
	}

	/**
	 * get the number of prefixes of a node
	 * @return the number of prefixes of a node
	 */
    public int getPrefixes() {
		return prefixes;
	}

    /**
     * set the number of prefixes of a node
     * @param prefixes the number of prefixes to be set
     */
	public void setPrefixes(int prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * get thr reference array of the node
	 * @return the reference array of the node
	 */
	public Node[] getReferences() {
		return references;
	}

	/**
	 * set the reference array of the node
	 * @param references the reference array to be set
	 */
	public void setReferences(Node[] references) {
		this.references = references;
	}

}
