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
			System.out.println("USAGE: <Ontology location> <triples file> <sleep>");
			System.exit(1);
		}
		boolean openSocket = false;
		String ontLoc = args[0];
		String file = args[1];
		long sleep = Long.parseLong(args[2]);
		RDFFORMAT format = RDFFORMAT.JSONLD;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// load the ontology
		OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontLoc));

		// create new C-Sprite engine
		CSpriteSPARQLEngine engine = new CSpriteSPARQLEngine(ont);

		// register query
		engine.registerQuery(created, 3, 1);
		engine.registerQuery(del, 3, 1);
		engine.registerQuery(modif, 3, 1);
		engine.registerQuery(ren, 3, 1);
		engine.registerQuery(copy, 3, 1);

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
	public static final String created = 
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX file: <http://w3id.org/sepses/vocab/unix-event#> " +
			"PREFIX fae: <http://w3id.org/sepses/event/file-access#> " +
			"PREFIX sys: <http://w3id.org/sepses/example/system-knowledge#> " +
			"PREFIX eve: <http://w3id.org/sepses/resource/event#> " +
            "CONSTRUCT {"
                    + "?subject fae:hasFileAccessType sys:Created;"
            		+ "         rdf:type fae:FileAccessEvent;"
            		+ "         fae:timestamp ?logtimestamp;"
            		+ "         fae:stimestamp ?logtimestamp;"
            		+ "         fae:hasSourceFile ?sourceFile;"
            		+ "         fae:hasTargetFile ?targetFile;"
            		+ "         fae:hasSourceHost ?sourceHost;"
            		+ "         fae:hasTargetHost ?targetHost;"		
            		+ "         fae:hasUser ?user ."	
            		+ "?sourceFile   fae:fileName ?filename ."
            		+ "?targetFile   fae:fileName ?filename ."
            		+ "?sourceHost   fae:hostName ?hostname ."
            		+ "?targetHost   fae:hostName ?hostname ."
            		+ "?user   		 fae:userName ?username"
            		+ "}"+
           
            "WHERE { "+
	             		 "?s file:pathName ?filename . " +
			             "?s file:hostName ?hostname . " +
			             "?s file:generatedAtTime ?logtimestamp . " +
			             "?s file:userName ?username ."+
			             "?s file:eventName ?event ."+
			           "FILTER (regex(str(?event),\"created\")) "   
	             + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"event\"),\"-created\")) AS ?subject)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-file\"),\"-created\")) AS ?sourceFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-file\"),\"-created\")) AS ?targetFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-host\"),\"-created\")) AS ?sourceHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-host\"),\"-created\")) AS ?targetHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"user\"),\"-created\")) AS ?user)"+
            "}";
	
	public static final String del = 
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX file: <http://w3id.org/sepses/vocab/unix-event#> " +
			"PREFIX fae: <http://w3id.org/sepses/event/file-access#> " +
			"PREFIX sys: <http://w3id.org/sepses/example/system-knowledge#> " +
			"PREFIX eve: <http://w3id.org/sepses/resource/event#> " +
            "CONSTRUCT {"
                    + "?subject fae:hasFileAccessType sys:Deleted;"
                    + "         rdf:type fae:FileAccessEvent;"
            		+ "         fae:timestamp ?logtimestamp;"
            		+ "         fae:stimestamp ?logtimestamp;"
            		+ "         fae:hasSourceFile ?sourceFile;"
            		+ "         fae:hasTargetFile ?targetFile;"
            		+ "         fae:hasSourceHost ?sourceHost;"
            		+ "         fae:hasTargetHost ?targetHost;"		
            		+ "         fae:hasUser ?user ."	
            		+ "?sourceFile   fae:fileName ?filename ."
            		+ "?targetFile   fae:fileName ?filename ."
            		+ "?sourceHost   fae:hostName ?hostname ."
            		+ "?targetHost   fae:hostName ?hostname ."
            		+ "?user   		 fae:userName ?username"
            		+ "}"+
                 
            "WHERE {" +
             "?s file:pathName ?filename . " +
             "?s file:hostName ?hostname . " +
             "?s file:eventName ?event ."+
             "?s file:userName ?username ."+
             "?s file:timestamp ?logtimestamp . " +
            "FILTER regex(str(?event),\"deleted\")"+
            "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"event\"),\"-deleted\")) AS ?subject)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-file\"),\"-deleted\")) AS ?sourceFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-file\"),\"-deleted\")) AS ?targetFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-host\"),\"-deleted\")) AS ?sourceHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-host\"),\"-deleted\")) AS ?targetHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"user\"),\"-deleted\")) AS ?user)"
            + "}";
	
	public static final String modif = 
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX file: <http://w3id.org/sepses/vocab/unix-event#> " +
			"PREFIX fae: <http://w3id.org/sepses/event/file-access#> " +
			"PREFIX sys: <http://w3id.org/sepses/example/system-knowledge#> " +
			"PREFIX eve: <http://w3id.org/sepses/resource/event#> " +
            "CONSTRUCT {"
                    + "?subject fae:hasFileAccessType sys:Modified;"
            		+ "         rdf:type fae:FileAccessEvent;"
            		+ "         fae:timestamp ?logtimestamp;"
            		+ "         fae:stimestamp ?logtimestamp;"
            		+ "         fae:hasSourceFile ?sourceFile;"
            		+ "         fae:hasTargetFile ?targetFile;"
            		+ "         fae:hasSourceHost ?sourceHost;"
            		+ "         fae:hasTargetHost ?targetHost;"	
            		+ "         fae:hasUser ?user ."	
            		+ "?sourceFile   fae:fileName ?filename ."
            		+ "?targetFile   fae:fileName ?filename ."
            		+ "?sourceHost   fae:hostName ?hostname ."
            		+ "?targetHost   fae:hostName ?hostname ."
            		+ "?user   		 fae:userName ?username"
            		+ "}"+
            "WHERE { "+
	             		 "?s file:pathName ?filename . " +
			             "?s file:hostName ?hostname . " +
			             "?s file:timestamp ?logtimestamp . " +
			             "?s file:eventName ?event ."+
			             "?s file:userName ?username ."+
			           "FILTER (regex(str(?event),\"updated\")) "
	        + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"event\"),\"-modified\")) AS ?subject)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-file\"),\"-modified\")) AS ?sourceFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-file\"),\"-modified\")) AS ?targetFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-host\"),\"-modified\")) AS ?sourceHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-host\"),\"-modified\")) AS ?targetHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"user\"),\"-modified\")) AS ?user)"+
            "}";
          
	public static final String ren =
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX file: <http://w3id.org/sepses/vocab/unix-event#> " +
			"PREFIX fae: <http://w3id.org/sepses/event/file-access#> " +
			"PREFIX sys: <http://w3id.org/sepses/example/system-knowledge#> " +
			"PREFIX eve: <http://w3id.org/sepses/resource/event#> " +
            "CONSTRUCT {"
                    + "?subject fae:hasFileAccessType sys:Renamed;"
            		+ "         rdf:type fae:FileAccessEvent;"
            		+ "         fae:timestamp ?logtimestamp;"
            		+ "         fae:hasSourceFile ?sourceFile;"
            		+ "         fae:hasTargetFile ?targetFile;"
            		+ "         fae:hasSourceHost ?sourceHost;"
            		+ "         fae:hasTargetHost ?targetHost;"	
            		+ "         fae:hasUser ?user ."	

            		+ "?sourceFile   fae:fileName ?filename ."
            		+ "?targetFile   fae:fileName ?filename2 ."
            		+ "?sourceHost   fae:hostName ?hostname ."
            		+ "?targetHost   fae:hostName ?hostname2 ."
            		+ "?user   		 fae:userName ?username ."
            		+ "}"+
                 
            "WHERE {"+
		            "?s file:pathName ?filename2 . " +
		            "?s file:hostName ?hostname2 . " +
		            "?s file:timestamp ?logtimestamp . " +
		            "?s file:userName ?username ."+
		            "?s file:eventName ?event2 ."+
			             "{SELECT * WHERE {" +
				             "?r file:pathName ?filename . " +
				             "?r file:hostName ?hostname . " +
				             "?r file:timestamp ?logtimestamp2 . " +
				             "?r file:eventName ?event"+
			            " FILTER regex(str(?event),\"moved\")}}"+
		            " FILTER (regex(str(?event2),\"created\") && ?filename!=?filename2 && ?hostname=?hostname2 )"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"event\"),\"-renamed\")) AS ?subject)"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-file\"),\"-renamed\")) AS ?sourceFile)"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-file\"),\"-renamed\")) AS ?targetFile)"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"source-host\"),\"-renamed\")) AS ?sourceHost)"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"target-host\"),\"-renamed\")) AS ?targetHost)"
		            + "BIND (URI(CONCAT(REPLACE(str(?s),\"LogEntry\",\"user\"),\"-renamed\")) AS ?user)"
            + "}";
            
	
	public static final String copy = 
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX file: <http://w3id.org/sepses/vocab/unix-event#> " +
			"PREFIX fae: <http://w3id.org/sepses/event/file-access#> " +
			"PREFIX sys: <http://w3id.org/sepses/example/system-knowledge#> " +
			"PREFIX eve: <http://w3id.org/sepses/resource/event#> " +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
            "CONSTRUCT {"
                    + "?subject fae:hasFileAccessType sys:Copied;"
            		+ "         rdf:type fae:FileAccessEvent;"
            		+ "         fae:timestamp ?logtimestamp;"
            		+ "         fae:stimestamp ?logtimestamp2;" 
            		+ "         fae:hasSourceFile ?sourceFile;"
            		+ "         fae:hasTargetFile ?targetFile;"
            		+ "         fae:hasSourceHost ?sourceHost;"
            		+ "         fae:hasTargetHost ?targetHost;"	
            		+ "         fae:hasUser ?user ."	
            		+ "?sourceFile   fae:fileName ?filename ."
            		+ "?targetFile   fae:fileName ?filename2 ."
            		+ "?sourceHost   fae:hostName ?hostname ."
            		+ "?targetHost   fae:hostName ?hostname2 ."
            		+ "?user   		 fae:userName ?username"
            		+ "}"+
            "WHERE { "+
	             "?s file:pathName ?filename . " +
	             "?s file:hostName ?hostname . " +
	             "?s file:timestamp ?logtimestamp2 . " +
	             "?s file:eventName ?event ."+
	             "{SELECT * WHERE {"+
	             		 "?r file:pathName ?filename2 . " +
			             "?r file:hostName ?hostname2 . " +
			             "?r file:timestamp ?logtimestamp . " +
			             "?r file:eventName ?event2 ."+
			             "?r file:userName ?username ."+
			           "FILTER (regex(str(?event2),\"created\")) "+    
	              "}}"+
	            "FILTER (regex(str(?event),\"IN_OPEN\") && ?filename2!=?filename && ?hostname=?hostname2 && substr(str(?logtimestamp),21) <= substr(str(?logtimestamp2),21) ) "
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"event\"),\"-copied\")) AS ?subject)"
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"source-file\"),\"-copied\")) AS ?sourceFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"target-file\"),\"-copied\")) AS ?targetFile)"
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"source-host\"),\"-copied\")) AS ?sourceHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"target-host\"),\"-copied\")) AS ?targetHost)"
            + "BIND (URI(CONCAT(REPLACE(str(?r),\"LogEntry\",\"user\"),\"-copied\")) AS ?user)"+
            "}";

}
