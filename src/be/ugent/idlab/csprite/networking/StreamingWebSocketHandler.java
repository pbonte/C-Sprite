/**
 * 
 */
package be.ugent.idlab.csprite.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import be.ugent.idlab.csprite.CSpriteEngine;

/**
 * @author pbonte
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class StreamingWebSocketHandler {
	private String sender, msg;
	private Map<String, String> prefixMapper;
	private Set<String> supportedConcepts;
	private CSpriteEngine engine;
	private long counter = 0;
	private long lastTime = 0;

	public StreamingWebSocketHandler(CSpriteEngine engine) {
		this.engine = engine;

	}

	@OnWebSocketConnect
	public void onConnect(Session user) throws Exception {
		System.out.println("Connecting on:\t" + System.nanoTime());
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		System.out.println("Closing on:" + System.nanoTime() + "\tprocessed:\t" + counter);

		engine.close();
	}

	@OnWebSocketMessage
	public void onMessage(Session user, String messages) {
		// streamingFox.addEvent(convert(message));
		// streamingFox.addEvent(message);
		long time0 = System.nanoTime();

		if (lastTime > 0) {
			System.out.println("Message time: " + (System.nanoTime() - lastTime));
		}
		counter++;
		// String[] messageSplit = messages.split("\n");
		// for (String message : messageSplit) {
		// if (!message.contains("http://dbpedia.org/datatype")
		// && !message.contains("http://www.w3.org/2001/XMLSchema#")) {
		// counter++;
		// String[] triple = messages.split(" ");
		// engine.addTriple(triple[0], triple[1], triple[2]);
		long time1 = System.nanoTime();
		StringTokenizer st = new StringTokenizer(messages, " ");
		String subject = st.nextToken();
		String prop = st.nextToken();
		String object = st.nextToken();
		System.out.println("Tokenizer time: " + (System.nanoTime() - time1));
		time1 = System.nanoTime();

		engine.addTriple(subject, prop, object);
		System.out.println("Process triple time: " + (System.nanoTime() - time1));

		engine.advanceTime(System.currentTimeMillis());
		//
		//
		// }
		// }
		lastTime = System.nanoTime();
		System.out.println("Total time: " + (System.nanoTime() - time0));

	}

	private static Map<String, String> createTriple(String s) {
		Map<String, String> m = new HashMap<String, String>();
		m.put("s", s);
		m.put("ts", System.nanoTime() + "");

		return m;

	}

	private static Map<String, String> createTriple(String s, String o) {
		Map<String, String> m = new HashMap<String, String>();
		m.put("s", s);
		m.put("o", o);
		m.put("ts", System.nanoTime() + "");

		return m;

	}
}
