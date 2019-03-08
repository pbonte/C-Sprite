/**
 * 
 */
package be.ugent.idlab.csprite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.idlab.csprite.Event.EventType;
import be.ugent.idlab.csprite.networking.FastStreamingWebSocketHandler;
import be.ugent.idlab.csprite.networking.StreamingWebSocketHandler;
import be.ugent.idlab.csprite.parser.HierarchyGenerator;
import be.ugent.idlab.csprite.utils.OntologyUtils;

/**
 * @author pbonte
 *
 */
public class CSpriteEngine {


	protected HierarchyGenerator hierarchyGen;
	protected Map<String, String> supertypes;
	protected Map<String, String> supertprops;

	protected Map<String, String> prefixMapper;
	protected Set<String> supportedConcepts;
	protected Map<String, List<String>> subTypes;
	protected long totalDiff = 0;
	protected long hits = 0;

	public CSpriteEngine(OWLOntology ontology) {
		this.hierarchyGen = new HierarchyGenerator(ontology);
		this.prefixMapper = hierarchyGen.getPrefixes();
		this.supportedConcepts = hierarchyGen.getSupportedConcepts();
		this.subTypes = hierarchyGen.getSuperTypes();
	}

	public boolean addQuery(String superType) {
		HashSet<String> supertypes = hierarchyGen.getSupTypes(superType);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertypes = new HashMap<String, String>();
			for (String s : supertypes) {
				this.supertypes.put(s, "Q1");
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean addQuery2(String superProp) {
		HashSet<String> supertypes = hierarchyGen.getSupProperties(superProp);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertprops = new HashMap<String, String>();
			for (String s : supertypes) {
				this.supertprops.put(s, "Q1");
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
		if (property.equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) {

			String concept = OntologyUtils.strip(object, prefixMapper);

			if (supertypes.containsKey(concept)) {
				System.out.println("Match: " + subject);

				hits++;
			}
		}
	}
	public void close() {
		System.out.println("Closing on:\t" + System.nanoTime());
		System.exit(1);
	}
	
}
