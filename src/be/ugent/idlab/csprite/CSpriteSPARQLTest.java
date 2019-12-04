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

import be.ugent.idlab.csprite.networking.StreamingWebSocketHandler;
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

	public static void main(String[] args)
			throws OWLOntologyCreationException, URISyntaxException, FileNotFoundException, IOException {
		if (args.length < 3) {
			System.out.println("USAGE: <Ontology location> <triples file> <query file> <sleep> <windowSize> <windowSlide>");
			System.exit(1);
		}
		
		boolean openSocket = false;
		String ontLoc = args[0];
		String file = args[1];
		String queriesLoc = args[2];
		long sleep = Long.parseLong(args[3]);
		int windowSize = Integer.parseInt(args[4]);
		int windowSlide = Integer.parseInt(args[5]);
		RDFFORMAT format = RDFFORMAT.JSONLD;
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
		// register query
		

		// engine.addQuery2("Property");
		// engine.connectToSocket("ws://localhost:4000/stream");
		long time1 = System.currentTimeMillis();

		long triples = 0;
//		for (int i = 0; i < 100; i++) {
//			engine.addTriple("http://dbpedia.org/resource/single" + i + "",
//					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/Single");
//			engine.addTriple("http://dbpedia.org/resource/software" + i + "",
//					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/Software");
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
		// read the triples file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
//		if (openSocket) {
//			FastStreamingWebSocketHandler socket = new FastStreamingWebSocketHandler(engine);
//			socket.start();
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	public void connectToSocket(String url, CSpriteEngine engine) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				WebSocketClient client = new WebSocketClient();
				StreamingWebSocketHandler socket = new StreamingWebSocketHandler(engine);

				try {
					client.start();
					ClientUpgradeRequest request = new ClientUpgradeRequest();
					client.connect(socket, new URI(url), request);
					System.out.printf("Connecting to : %s%n", url);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread thread1 = new Thread(r);
		thread1.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
