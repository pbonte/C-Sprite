/**
 * 
 */
package be.ugent.idlab.csprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import be.ugent.idlab.csprite.parser.HierarchyGenerator;
import be.ugent.idlab.csprite.utils.OntologyUtils;
import be.ugent.idlab.csprite.windowing.EsperWindow;

/**
 * @author pbonte
 *
 */
public class CSpriteEngine {


	protected HierarchyGenerator hierarchyGen;
	protected Map<String, List<QueryPair>> supertypes;
	protected Map<String, List<QueryPair>> supertprops;

	protected Map<String, String> prefixMapper;
	protected Set<String> supportedConcepts;
	protected Map<String, List<String>> subTypes;
	protected long totalDiff = 0;
	protected long hits = 0;
	
	protected Map<String,EsperWindow> queryWindowMapping;

	public CSpriteEngine(OWLOntology ontology) {
		this.hierarchyGen = new HierarchyGenerator(ontology);
		this.prefixMapper = hierarchyGen.getPrefixes();
		this.supportedConcepts = hierarchyGen.getSupportedConcepts();
		this.subTypes = hierarchyGen.getSuperTypes();
		this.supertypes = new HashMap<String, List<QueryPair>>();
		this.supertprops = new HashMap<String, List<QueryPair>>();
		this.queryWindowMapping = new HashMap<String,EsperWindow>();
	}

	public boolean addQuery(String superType) {
		HashSet<String> supertypes = hierarchyGen.getSupTypes(superType);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertypes = new HashMap<String, List<QueryPair>>();
			for (String s : supertypes) {
				this.supertypes.put(s, Collections.singletonList(new QueryPair("Q1",superType)));
			}
			return true;
		} else {
			return false;
		}
	}
	public boolean addConceptQuery(String superType, String queryID) {
		String queryConceptStripped = OntologyUtils.strip(superType, hierarchyGen);
		HashSet<String> supertypes = hierarchyGen.getSupTypes(queryConceptStripped);
		if (supertypes != null && !supertypes.isEmpty()) {			
			for (String s : supertypes) {
				if(!this.supertypes.containsKey(s)) {
					this.supertypes.put(s, new ArrayList<QueryPair>());
				}
				this.supertypes.get(s).add(new QueryPair(queryID,superType));
			}
			return true;
		} else {
			return false;
		}
	}
	public void addWindow(String queryID, EsperWindow window) {
		queryWindowMapping.put(queryID, window);
	}
	public boolean addPropertyQuery(String superProp, String queryID) {
		String queryPropStripped = OntologyUtils.strip(superProp, hierarchyGen);
		HashSet<String> supertypes = hierarchyGen.getSupProperties(queryPropStripped);
		if (supertypes != null && !supertypes.isEmpty()) {
			for (String s : supertypes) {
				if(!this.supertprops.containsKey(s)) {
					this.supertprops.put(s, new ArrayList<QueryPair>());
				}
				this.supertprops.get(s).add(new QueryPair(queryID,superProp));
			}
			return true;
		} else {
			return false;
		}
	}
	public boolean addQuery2(String superProp) {
		HashSet<String> supertypes = hierarchyGen.getSupProperties(superProp);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertprops = new HashMap<String, List<QueryPair>>();
			for (String s : supertypes) {
				this.supertprops.put(s, Collections.singletonList(new QueryPair("Q1",superProp)));
			}
			return true;
		} else {
			return false;
		}
	}

	private void printDiff(int number) {
		System.out.println("triples/second: " + (1000000000l * number) / (totalDiff));
	}

	public void advanceTime(long time) {

	}

	public void addTriple(String subject, String property, String object) {
		if (property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {

			String concept = OntologyUtils.strip(object, hierarchyGen);
			

			List<QueryPair> queryIDs = supertypes.get(concept);
			if(queryIDs!=null) {
				for(QueryPair queryPair  :queryIDs) {
					queryWindowMapping.get(queryPair.getQueryID()).addEvent(subject, property, queryPair.getConcept());
				}
				hits++;
			}
		}else {
			String concept = OntologyUtils.strip(property, hierarchyGen);
			List<QueryPair> queryIDs = supertprops.get(concept);
			if(queryIDs!=null) {
				for(QueryPair queryPair  :queryIDs) {
					queryWindowMapping.get(queryPair.getQueryID()).addEvent(subject, queryPair.getConcept(), object);
				}
				hits++;
			}
		}
	}
	public void close() {
		System.out.println("Closing on:\t" + System.nanoTime());
		System.exit(1);
	}
	
}
