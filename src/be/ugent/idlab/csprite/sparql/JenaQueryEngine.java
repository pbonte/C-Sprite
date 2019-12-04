package be.ugent.idlab.csprite.sparql;


import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;



/**
 * @author pbonte
 *
 */
public class JenaQueryEngine {
	private Model infModel;
	private List<Query> queries;
	private String queryID;

	
	public JenaQueryEngine(String queryID){
		Model dataModel = ModelFactory.createDefaultModel();
		this.infModel = dataModel;
		this.queryID = queryID;
	}
	
	public void addContinuousQuery(String queryString){
		if(queries == null){
			queries = new ArrayList<Query>();
		}
		Query query = QueryFactory.create(queryString);
		addContinuousQuery(query);
	}
	
	public void addContinuousQuery(Query query){
		if(queries == null){
			queries = new ArrayList<Query>();
		}
		queries.add(query);
	}

	public void setupSimpleAdd(List<Statement> add) {
		infModel.add(add);
	}
	public void setupSimpleAdd(String s, String p ,String o) {
		infModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(s), ResourceFactory.createProperty(p), ResourceFactory.createResource(o)) );
	}
	public void setupSimpleDelete(List<Statement> delete) {
		infModel.remove(delete);

	}
	
	
		
	public List<ResultSet> query() {
		long counter = 0;
		List<ResultSet> results = new ArrayList<ResultSet>();
		for (Query query : queries) {
			if(query.isSelectType()){
			try (QueryExecution qexec = QueryExecutionFactory.create(query, infModel)) {
				ResultSet result = qexec.execSelect();
				while(result.hasNext()){
					counter++;
					QuerySolution sol = result.nextSolution();
					System.out.println(sol);
				}
			}
		}else{
			try (QueryExecution qexec = QueryExecutionFactory.create(query, infModel)) {
				Model result = qexec.execConstruct();
				System.out.println(queryID + " " + result);
			}
		}
		}
		//remove all statements
		infModel.removeAll();
		
		return results;
	}

	
}
