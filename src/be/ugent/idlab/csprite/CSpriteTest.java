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
import java.util.StringTokenizer;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.idlab.csprite.networking.FastStreamingWebSocketHandler;
import be.ugent.idlab.csprite.networking.StreamingWebSocketHandler;
import be.ugent.idlab.csprite.utils.OntologyUtils;

/**
 * @author pbonte
 *
 */
public class CSpriteTest {

	public static void main(String[] args)
			throws OWLOntologyCreationException, URISyntaxException, FileNotFoundException, IOException {
		if(args.length<3){
			System.out.println("USAGE: <Ontology location> <triples file> <query concept>");
			System.exit(1);
		}
		String ontLoc = args[0];
		String file= args[1];
		String queryConcept = args[2];
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		// System.out.println("Reading file");
		// try (Stream<String> stream =
		// Files.lines(Paths.get("filtered234_type.nt"))) {
		//
		// stream.forEach(e -> memList.add(e));
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// System.out.println("Read file");
		// OWLOntology ont = manager
		// .loadOntologyFromOntologyDocument(classloader.getResourceAsStream(("dbpedia_stripped2.owl")));
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(args[0]));
		// OWLOntology ont = manager
		// .loadOntologyFromOntologyDocument(new
		// File(("/tmp/dbpedia_test.owl")));
		// find the top classes:

		CSpriteEngine engine = new CSpriteEngine(ont);
		String queryConceptStripped = OntologyUtils.strip(queryConcept, engine.prefixMapper);
		engine.addQuery(queryConceptStripped);
		// engine.addQuery2("Property");
		// engine.connectToSocket("ws://localhost:4000/stream");
		long time1 = System.currentTimeMillis();
		// for (int i = 0; i < memList.size(); i++) {
		// final int j = i;
		//// executor.submit(() -> {
		//
		// String input1 = memList.get(j);
		// String[] split1 = input1.split(" ");
		//
		// engine.addTriple(split1[0], split1[1], split1[2]);
		//// });
		//
		// }
		// int runs = 100000;
		// for(int i=0;i<runs;i++){
		// engine.addTriple("test"+i,"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<http://dbpedia.org/ontology/Software>");
		// engine.addTriple("test"+i,"<http://dbpedia.org/ontology/topProp","<test2>");
		// engine.advanceTime(i);
		// engine.addTriple("test"+i,"<http://dbpedia.org/ontology/topProp","<test3>");
		// engine.addTriple("test"+i,"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<http://dbpedia.org/ontology/Software>");
		// }
		long triples = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				triples++;
				StringTokenizer st = new StringTokenizer(line, " ");
				String subject = st.nextToken();
				String prop = st.nextToken();
				String object = st.nextToken();
				engine.addTriple(subject, prop, object);
				engine.advanceTime(triples);
			}
		}
		long difftime = System.currentTimeMillis() - time1;
		System.out.println("throughput:\t" + triples * 1000 / difftime + "triples/s");
		// while(!executor.getQueue().isEmpty()){
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// engine.printDiff(10000*4);

		// Runnable r = new Runnable() {
		//
		// @Override
		// public void run() {
		// WebSocketClient client = new WebSocketClient();
		// StreamingWebSocketHandler socket = new
		// StreamingWebSocketHandler(engine);
		//
		// try {
		// client.start();
		// ClientUpgradeRequest request = new ClientUpgradeRequest();
		// client.connect(socket, new URI(args[0]), request);
		// System.out.printf("Connecting to : %s%n", args[0]);
		//
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// };
		// Thread thread1 = new Thread(r);
		// thread1.start();
		FastStreamingWebSocketHandler socket = new FastStreamingWebSocketHandler(engine);
		socket.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
