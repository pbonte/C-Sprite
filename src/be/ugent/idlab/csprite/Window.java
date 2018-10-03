/**
 * 
 */
package be.ugent.idlab.csprite;

import java.util.LinkedList;

/**
 * @author pbonte
 *
 */
public class Window {

	protected int size;
	protected LinkedList<Event> linkedEvents;
	public Window(int size){
		this.size = size;
		linkedEvents = new LinkedList<Event>();
	}
	public void addEvent(Event event){
		//Drop out of order events
		if(linkedEvents.isEmpty()){
			linkedEvents.add(event);
		}
		else if(linkedEvents.getFirst().getTs()<=event.getTs()){
			linkedEvents.add(event);
		}
	}
	public void advanceTime(long newTime){
		while(linkedEvents.peekFirst()!=null && linkedEvents.peekFirst().getTs()-newTime > size){
			linkedEvents.pollFirst();
		}
	}
	
}
