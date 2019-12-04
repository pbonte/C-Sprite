/**
 * 
 */
package be.ugent.idlab.csprite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.idlab.csprite.networking.StreamingWebSocketHandler;
import be.ugent.idlab.csprite.sparql.CSpriteSPARQLEngine;

/**
 * @author pbonte
 *
 */
public class CSpriteSPARQLTest {

	public static void main(String[] args)
			throws OWLOntologyCreationException, URISyntaxException, FileNotFoundException, IOException {
		if (args.length < 3) {
			System.out.println("USAGE: <Ontology location> <triples file> <query concept>");
			System.exit(1);
		}
		boolean openSocket = false;
		String ontLoc = args[0];
		String file = args[1];
		String queryConcept = args[2];
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// load the ontology
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontLoc));

		// create new C-Sprite engine
		CSpriteSPARQLEngine engine = new CSpriteSPARQLEngine(ont);
		
		//register query
		String queryString = "CONSTRUCT{?test <http://test/Prop> <http://dbpedia.org/ontology/Work>}"
				+ "WHERE {{?test a <http://dbpedia.org/ontology/Work>."
				+ "}}";
		String queryString2 = "CONSTRUCT{?test <http://test/Prop> <http://dbpedia.org/ontology/Software>}"
				+ "WHERE {{?test a <http://dbpedia.org/ontology/Software>."
				+ "}}";
		engine.registerQuery(queryString, 2, 1);
		engine.registerQuery(queryString2, 2, 1);

		// engine.addQuery2("Property");
		// engine.connectToSocket("ws://localhost:4000/stream");
		long time1 = System.currentTimeMillis();

		long triples = 0;
		for (int i = 0; i < 100; i++) {
			engine.addTriple("http://dbpedia.org/resource/single" + i + "",
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/Single");
			engine.addTriple("http://dbpedia.org/resource/software" + i + "",
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/Software");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// read the triples file line by line
//		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//			String line;
//			while ((line = br.readLine()) != null) {
//				triples++;
//				//split the triple in (s,p,o)
//				StringTokenizer st = new StringTokenizer(line, " ");
//				try {
//				String subject = OntologyUtils.removeHooks(st.nextToken());
//				String prop = OntologyUtils.removeHooks(st.nextToken());
//				String object = OntologyUtils.removeHooks(st.nextToken());
//				
//				//add triple to the engine
//				engine.addTriple(subject, prop, object);
//				//advance time (usefull for windowing)
//				//engine.advanceTime(triples);
//				}catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
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

}
