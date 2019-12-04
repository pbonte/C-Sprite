package be.ugent.idlab.csprite.networking;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import be.ugent.idlab.csprite.sparql.CSpriteSPARQLEngine;


@WebSocket

public class WebSocketInputStream {

	private CSpriteSPARQLEngine engine;

	public WebSocketInputStream(CSpriteSPARQLEngine engine) {
		this.engine = engine;
	}
    @OnWebSocketConnect
    public void connected(Session session) {
    	System.out.println("connecting");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
    	
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	Dataset model = DatasetFactory.create();
		InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		RDFDataMgr.read(model, stream, null, Lang.JSONLD);
		Model merged = model.getDefaultModel();
		model.listNames().forEachRemaining(m -> merged.add(model.getNamedModel(m)));
		StmtIterator it = model.getDefaultModel().listStatements();
		while(it.hasNext()) {
			engine.addTriple(it.next());
		}
    }

}
