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
	public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, FileNotFoundException, IOException {
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
//		OWLOntology ont = manager
//				.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(("dbpedia_stripped2.owl")));
		OWLOntology ont = manager
				.loadOntologyFromOntologyDocument(new File(args[1]));
		// OWLOntology ont = manager
		// .loadOntologyFromOntologyDocument(new
		// File(("/tmp/dbpedia_test.owl")));
		// find the top classes:

		CSpriteEngine engine = new CSpriteEngine(ont);
		engine.addQuery(args[0]);
		//engine.addQuery2("Property");
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
//		int runs = 100000;
//		 for(int i=0;i<runs;i++){
//		 engine.addTriple("test"+i,"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<http://dbpedia.org/ontology/Software>");
//		 engine.addTriple("test"+i,"<http://dbpedia.org/ontology/topProp","<test2>");
//		 engine.advanceTime(i);
//		 engine.addTriple("test"+i,"<http://dbpedia.org/ontology/topProp","<test3>");
//		 engine.addTriple("test"+i,"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<http://dbpedia.org/ontology/Software>");
//		 }
//		long triples =0;
//		try (BufferedReader br = new BufferedReader(new FileReader("/tmp/big8.nt"))) {
//		    String line;
//		    while ((line = br.readLine()) != null) {
//		    	triples++;
//		    	StringTokenizer st = new StringTokenizer(line, " ");
//				String subject = st.nextToken();
//				String prop = st.nextToken();
//				String object = st.nextToken();
//				engine.addTriple(subject,prop,object);
//				 engine.advanceTime(triples);
//		    }
//		}
//		 long difftime = System.currentTimeMillis() - time1;
//		 System.out.println("throughput:\t" + triples*1000 / difftime);
		// while(!executor.getQueue().isEmpty()){
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// engine.printDiff(10000*4);

//		Runnable r = new Runnable() {
//
//			@Override
//			public void run() {
//				WebSocketClient client = new WebSocketClient();
//				StreamingWebSocketHandler socket = new StreamingWebSocketHandler(engine);
//
//				try {
//					client.start();
//					ClientUpgradeRequest request = new ClientUpgradeRequest();
//					client.connect(socket, new URI(args[0]), request);
//					System.out.printf("Connecting to : %s%n", args[0]);
//
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
//		Thread thread1 = new Thread(r);
//		thread1.start();
		FastStreamingWebSocketHandler socket = new FastStreamingWebSocketHandler(engine);
		socket.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HierarchyGenerator hierarchyGen;
	private Map<String,String> supertypes;
	private Map<String,String> supertprops;

	private Map<String, String> prefixMapper;
	private Set<String> supportedConcepts;
	private Map<String, List<String>> subTypes;
	private long totalDiff = 0;
	private long hits = 0;
	private JoinWindow joinWindow;

	public CSpriteEngine(OWLOntology ontology) {
		this.hierarchyGen = new HierarchyGenerator(ontology);
		this.prefixMapper = hierarchyGen.getPrefixes();
		this.supportedConcepts = hierarchyGen.getSupportedConcepts();
		this.subTypes = hierarchyGen.getSuperTypes();
		this.joinWindow = new JoinWindow(1000);
	}

	public boolean addQuery(String superType) {
		HashSet<String> supertypes = hierarchyGen.getSupTypes(superType);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertypes = new HashMap<String,String>();
			for(String s: supertypes){
				this.supertypes.put(s,"Q1");
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean addQuery2(String superProp) {
		HashSet<String> supertypes = hierarchyGen.getSupProperties(superProp);
		if (supertypes != null && !supertypes.isEmpty()) {
			this.supertprops = new HashMap<String,String>();
			for(String s: supertypes){
				this.supertprops.put(s,"Q1");
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
		 

		joinWindow.advanceTime(time);

	}

	public void addTriple(String subject, String property, String object) {
		long time1 = System.nanoTime();
		if (property.equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) {
//			System.out.println("Triple type check time: " +(System.nanoTime()-time1));

//			time1 = System.nanoTime();
			String concept = OntologyUtils.strip(object, prefixMapper);
//			System.out.println("Convert time: " +(System.nanoTime()-time1));

//			time1 = System.nanoTime();
			if (supertypes.containsKey(concept)) {
				System.out.println("type: "+subject);
//				System.out.println("Type match: " +(System.nanoTime()-time1));
//				time1 = System.nanoTime();
//				System.out.println("Print time: " +(System.nanoTime()-time1));
//				time1 = System.nanoTime();
				
				//TODO add this AGAIN!!! (only for analysis dataset
				//joinWindow.addEvent(new Event(subject, property, object, System.currentTimeMillis(),EventType.TYPE));
				
				
				//System.out.println("Join time: " +(System.nanoTime()-time1));

				hits++;
			}
//				else{
//				System.out.println("Type no match: " +(System.nanoTime()-time1));
//			}
			// if(subTypes.containsKey(concept)){
			// for(String superType:subTypes.get(concept)){
			// if(superType.equals("A_Work_TOP")){
			// //System.out.println("MATCH:\t"+subject);
			// hits++;
			// }
			// }
			// }
			totalDiff += (System.nanoTime() - time1);
		} else {
			String concept = OntologyUtils.strip(property, prefixMapper);
//			time1 = System.nanoTime();

			if (supertprops.containsKey(concept)) {
//				System.out.println("Prop match: " +(System.nanoTime()-time1));
//				time1 = System.nanoTime();
				joinWindow.addEvent(new Event(subject, property, object, System.currentTimeMillis(),EventType.OBJECTProperty));
//				System.out.println("Prop Join time: " +(System.nanoTime()-time1));

			}
		}
		// else {
		// if (supportedConcepts.contains(triple[1])) {
		// String property = OntologyUtils.strip(triple[1], prefixMapper);
		// String subj = OntologyUtils.encode(triple[0], prefixMapper);
		// String obj = OntologyUtils.encode(triple[2], prefixMapper);
		// //esper.sendEvent(createTriple(subj, obj), property);
		// }
		// }
	}

	public void connectToSocket(String url) {
		CSpriteEngine engine = this;
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

	public void close() {
		System.out.println("Closing on:\t" + System.nanoTime());
		System.exit(1);
	}
}
