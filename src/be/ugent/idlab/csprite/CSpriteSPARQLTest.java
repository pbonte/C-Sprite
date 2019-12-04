/**
 * 
 */
package be.ugent.idlab.csprite;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.idlab.csprite.networking.FastStreamingWebSocketHandler;
import be.ugent.idlab.csprite.networking.StreamingWebSocketHandler;
import be.ugent.idlab.csprite.networking.WebSocketClientSource;
import be.ugent.idlab.csprite.sparql.CSpriteSPARQLEngine;
import be.ugent.idlab.csprite.utils.OntologyUtils;

/**
 * @author pbonte
 *
 */

public class CSpriteSPARQLTest {
	public enum RDFFORMAT {
		NTriples, JSONLD
	};
	public enum INPUT {FILE, SOCKET};
	public static void main(String[] args)
			throws OWLOntologyCreationException, URISyntaxException, FileNotFoundException, IOException {
		if (args.length < 3) {
			System.out.println("USAGE: <Ontology location> <input type (file|socket)> <RDF format> <triples file|socket url> <query file>  <windowSize> <windowSlide> <sleep (file)>");
			System.exit(1);
		}
		
		String ontLoc = args[0];
		INPUT inputType = INPUT.SOCKET;
		RDFFORMAT format = RDFFORMAT.NTriples;
		if(args[1].toLowerCase().contains("file")) {
			inputType = INPUT.FILE;
		}
		if(args[2].toLowerCase().contains("json")) {
			format = RDFFORMAT.JSONLD;
		}
		String inputSource = args[3];
		String queriesLoc = args[4];
		int windowSize = Integer.parseInt(args[5]);
		int windowSlide = Integer.parseInt(args[6]);
		long sleep = Long.parseLong(args[7]);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// load the ontology
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontLoc));

		// create new C-Sprite engine
		CSpriteSPARQLEngine engine = new CSpriteSPARQLEngine(ont);
		//read the queries
		List<String> queries = readQueries(queriesLoc);
		for(String queryStr:queries) {
			engine.registerQuery(queryStr, windowSize, windowSlide);
		}
		
		long time1 = System.currentTimeMillis();

		long triples = 0;

		// read the triples file line by line
		if(inputType == INPUT.FILE) {
		try (BufferedReader br = new BufferedReader(new FileReader(inputSource))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (format == RDFFORMAT.NTriples) {
					triples++;
					// split the triple in (s,p,o)
					StringTokenizer st = new StringTokenizer(line, " ");
					String subject = OntologyUtils.removeHooks(st.nextToken());
					String prop = OntologyUtils.removeHooks(st.nextToken());
					String object = OntologyUtils.removeHooks(st.nextToken());

					// add triple to the engine
					engine.addTriple(subject, prop, object);

				} else if (format == RDFFORMAT.JSONLD) {
					Dataset model = DatasetFactory.create();
					InputStream stream = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));
					RDFDataMgr.read(model, stream, null, Lang.JSONLD);
					Model merged = model.getDefaultModel();
					model.listNames().forEachRemaining(m -> merged.add(model.getNamedModel(m)));
					StmtIterator it = model.getDefaultModel().listStatements();
					while(it.hasNext()) {
						engine.addTriple(it.next());
						triples++;
					}
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		long difftime = System.currentTimeMillis() - time1 + 1;
		System.out.println("throughput:\t" + triples * 1000 / difftime + " triples/s");
		}else if(inputType == INPUT.SOCKET) {
			WebSocketClientSource wsSource = new WebSocketClientSource(inputSource,engine);
			wsSource.stream();
		}

	}


	public static List<String> readQueries(String fileLocation ){
		
		List<String> queries = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
			String line;
			while ((line = br.readLine()) != null) {
				if(!line.equals("")) {
					queries.add(line);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return queries;
	}

}
