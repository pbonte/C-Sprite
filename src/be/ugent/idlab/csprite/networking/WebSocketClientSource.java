package be.ugent.idlab.csprite.networking;

import java.net.URI;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import be.ugent.idlab.csprite.sparql.CSpriteSPARQLEngine;



public class WebSocketClientSource {

	private String wsURL;
	private CSpriteSPARQLEngine engine;

	public WebSocketClientSource(String wsURL, CSpriteSPARQLEngine engine) {
		this.wsURL = wsURL;
		this.engine = engine;
	}

	

	public void stream() {
		WebSocketClient client = new WebSocketClient();

		WebSocketInputStream socket = new WebSocketInputStream(engine);
		try {
			client.start();

			URI echoUri = new URI(wsURL);
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(socket, echoUri, request);
			System.out.printf("Connecting to : %s%n", echoUri);

		} catch (Exception t) {
			t.printStackTrace();
		} finally {
			try {
				//client.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
