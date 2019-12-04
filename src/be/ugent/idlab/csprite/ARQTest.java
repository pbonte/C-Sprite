package be.ugent.idlab.csprite;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import org.apache.jena.sparql.syntax.ElementWalker;

import be.ugent.idlab.csprite.sparql.ConceptExtractor;


public class ARQTest {
	
	public static void main(String[] args) {
		
		String queryString = "CONSTRUCT{?test <http://test/Prop> <http://test/Work>}"
				+ "WHERE {{?test a <http://dbpedia.org/ontology/Work>;"
						
				+ "}}" ;
		  Query query = QueryFactory.create(queryString) ;
		  ConceptExtractor conceptExt = new ConceptExtractor();
		  ElementWalker.walk(query.getQueryPattern(), conceptExt);
		  System.out.println("concepts: " +conceptExt.getConcepts());
		  System.out.println("properties: " +conceptExt.getProperties());
		  System.out.println("subjects: " +conceptExt.getSubjects());
		  System.out.println("objects: " +conceptExt.getObjects());
		  System.out.println(conceptExt.getContainsVariableProperty());

		  
	}

}
