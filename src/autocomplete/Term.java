package autocomplete;

public class Term implements ITerm {
	private String query;
	private long weight;
	
	/**
	 * Initializes a term with the given query string and weight.
	 * @param query the query of the term
	 * @param weight the weight of the term
	 */
	public Term(String query, long weight) {
		if(query == null || weight < 0) {
			throw new IllegalArgumentException();
		}
		this.query = query;
		this.weight = weight;
	}

	@Override
	public int compareTo(ITerm that) {		
		return this.query.compareTo(((Term)that).query);
	}

	/**
	 * get the weight of the term
	 * @return the weight associated with the term
	 */
	public long getWeight() {
		return this.weight;
	}
	
	/**
	 * set the weight of the term
	 * @param weight the weight of the term to be set
	 */
	public void setWeight(long weight) {
		this.weight = weight;
	}

	/**
	 * get the query of the term
	 * @return the query associated with the term
	 */
	public String getTerm() {
		return this.query;
	}
	
	/**
	 * set the query of the term
	 * @param query the query of the term to be set
	 */
	public void setTerm(String query) {
		this.query = query;
	}
	
	/**
	 * @return the string representation of the term
	 */
	public String toString() {
		return this.weight + "	" + this.query;
	}

}
