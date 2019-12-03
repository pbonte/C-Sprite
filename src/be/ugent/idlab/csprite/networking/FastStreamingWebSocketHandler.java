/**
 * 
 */
package be.ugent.idlab.csprite.networking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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
public class FastStreamingWebSocketHandler {
	private String sender, msg;
	private Map<String, String> prefixMapper;
	private Set<String> supportedConcepts;
	private CSpriteEngine engine;
	private long counter = 0;
	private long lastTime = 0;

	public FastStreamingWebSocketHandler(CSpriteEngine engine) {
		this.engine = engine;
		
	}
	public void start(){
		Runnable task = () -> {
			try {
				ServerSocket server = new ServerSocket(8080);
				Socket sock = server.accept();
				InputStream socketStreamIn = sock.getInputStream();
				DataInputStream input = new DataInputStream(socketStreamIn);
				BufferedReader d = new BufferedReader(new InputStreamReader(input));
				long lines = 0;

				String responseLine;
				long time1 = System.nanoTime();
				System.out.println("Connecting on:\t" + System.nanoTime());

				while ((responseLine = d.readLine()) != null) {
//					if (lastTime > 0) {
//						System.out.println("Message time: " + (System.nanoTime() - lastTime));
//					}
					long time0 = System.nanoTime();
					time1 = System.nanoTime();
					StringTokenizer st = new StringTokenizer(responseLine, " ");
					String subject = st.nextToken();
					String prop = st.nextToken();
					String object = st.nextToken();
//					System.out.println("Tokenizer time: " + (System.nanoTime() - time1));

//					System.out.println("TOTAL:Tokenizer time: " + (System.nanoTime() - time0));
					
					time1 = System.nanoTime();
					engine.addTriple(subject, prop, object);
//					System.out.println("Process triple time: " + (System.nanoTime() - time1));
//					System.out.println("TOTAL:Process triple time: " + (System.nanoTime() - time0));

					time1 = System.nanoTime();
//					engine.advanceTime(System.currentTimeMillis());
//					System.out.println("Advance time time: " +(System.nanoTime()-time1));
//					System.out.println("TOTAL:Advance time time: " +(System.nanoTime()-time0));

//					System.out.println("Total time: " + (System.nanoTime() - time0));
//					lastTime = System.nanoTime();

				}
				System.out.println("Closing on:" + System.nanoTime() + "\tprocessed:\t" + counter);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		};

		task.run();

		Thread thread = new Thread(task);
		thread.start();

	}



}
