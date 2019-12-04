package be.ugent.idlab.csprite.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.semanticweb.owlapi.model.OWLOntology;

import be.ugent.idlab.csprite.CSpriteEngine;
import be.ugent.idlab.csprite.windowing.EsperWindow;

public class CSpriteSPARQLEngine {

	private CSpriteEngine engine;
	private int queryCounter;

	public CSpriteSPARQLEngine(OWLOntology ontology) {
		this.engine = new CSpriteEngine(ontology);
		this.queryCounter = 0;

	}

	public void registerQuery(String queryString,int windowSize,int windowSlide) {
		String queryID="Q"+queryCounter++;
		Query query = QueryFactory.create(queryString);
		ConceptExtractor conceptExt = new ConceptExtractor();
		ElementWalker.walk(query.getQueryPattern(), conceptExt);
		//extract concept queries
		for (String queryCon : conceptExt.getConcepts()) {
			engine.addConceptQuery(queryCon, queryID);
		}
		//extract property queries
		for (String queryProp : conceptExt.getProperties()) {
			engine.addPropertyQuery(queryProp, queryID);
		}
		//register a jena SPARQL engine
		JenaQueryEngine jena = new JenaQueryEngine(queryID);
		jena.addContinuousQuery(query);
		//register a window
		EsperWindow window = new EsperWindow(windowSize, windowSlide, jena);
		engine.addWindow(queryID, window);
	}
	public void addTriple(String subject, String property, String object) {
		this.engine.addTriple(subject, property, object);
	}
	public void addTriple(Statement statement) {
		this.engine.addTriple(statement.asTriple().getSubject().getURI(),
				statement.asTriple().getPredicate().getURI(),
				statement.asTriple().getObject().toString());
	}

}
