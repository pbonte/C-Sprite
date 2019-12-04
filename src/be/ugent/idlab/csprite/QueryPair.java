package be.ugent.idlab.csprite;

public class QueryPair {
	
	private String queryID;
	private String concept;

	public QueryPair(String queryID,String concept) {
		this.queryID=queryID;
		this.concept = concept;
	}

	public String getQueryID() {
		return queryID;
	}

	public String getConcept() {
		return concept;
	}
	@Override
	public String toString() {
		return String.format("QueryID: %s, Query Concept: %s", queryID,concept);
	}
}
