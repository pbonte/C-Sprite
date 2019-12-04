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
		boolean openSocket=false;
		String ontLoc = args[0];
		String file= args[1];
		String queryConcept = args[2];
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		//load the ontology
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontLoc));
		
		//create new C-Sprite engine
		CSpriteEngine engine = new CSpriteEngine(ont);
		//extract prefixes from registered query concept
		String queryConceptStripped = OntologyUtils.strip(queryConcept, engine.hierarchyGen);
		//register the query
		engine.addQuery(queryConceptStripped);
		// engine.addQuery2("Property");
		// engine.connectToSocket("ws://localhost:4000/stream");
		long time1 = System.currentTimeMillis();
		
		long triples = 0;
		//read the triples file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				triples++;
				//split the triple in (s,p,o)
				StringTokenizer st = new StringTokenizer(line, " ");
				String subject = st.nextToken();
				String prop = st.nextToken();
				String object = st.nextToken();
				//add triple to the engine
				engine.addTriple(subject, prop, object);
				//advance time (usefull for windowing)
				engine.advanceTime(triples);
			}
		}
		long difftime = System.currentTimeMillis() - time1;
		System.out.println("throughput:\t" + triples * 1000 / difftime + " triples/s");
		if(openSocket) {
			FastStreamingWebSocketHandler socket = new FastStreamingWebSocketHandler(engine);
			socket.start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
