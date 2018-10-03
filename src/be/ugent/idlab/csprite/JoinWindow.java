/**
 * 
 */
package be.ugent.idlab.csprite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import be.ugent.idlab.csprite.Event.EventType;

/**
 * @author pbonte
 *
 */
public class JoinWindow extends Window {

	Map<String,Event> joinIndexerVar1;
	Map<String,Set<Event>> joinIndexerVar2;

	public JoinWindow(int size) {
		super(size);
		joinIndexerVar1 = new HashMap<String,Event>();
		joinIndexerVar2 = new HashMap<String,Set<Event>>();
	}

	public void addEvent(Event event){
		//Drop out of order events
		super.addEvent(event);
		//join ?var a Work. ?var topProp ?var2
		if(event.getType() == EventType.TYPE){
			if(!joinIndexerVar1.containsKey(event.getSubject())){
				joinIndexerVar1.put(event.getSubject(), event);
			}
			if(joinIndexerVar2.containsKey(event.getSubject())){
				System.out.println("join " + event.getSubject());
				joinIndexerVar2.get(event.getSubject()).forEach(a->System.out.println("\t"+a.getObject()));
			}
		}else{
			if(!joinIndexerVar2.containsKey(event.getSubject())){
				joinIndexerVar2.put(event.getSubject(),new HashSet<Event>());
			}
			joinIndexerVar2.get(event.getSubject()).add(event);
			if(joinIndexerVar1.containsKey(event.getSubject())){
				System.out.println("join " + event.getSubject());
				System.out.println("\t"+event.getObject());
				//joinIndexerVar2.get(event.getSubject()).forEach(a->System.out.println("\t"+a.getObject()));
				}
		}
		
	}
	public void advanceTime(long newTime){
		while(linkedEvents.peekFirst()!=null && newTime-linkedEvents.peekFirst().getTs() > size){
			Event remove = linkedEvents.pollFirst();
			if(joinIndexerVar1.containsKey(remove.getSubject())){
				joinIndexerVar1.remove(remove.getSubject());
			}
			if(joinIndexerVar2.containsKey(remove.getSubject())){
				joinIndexerVar2.get(remove.getSubject()).remove(remove);
				if(joinIndexerVar2.get(remove.getSubject()).isEmpty()){
					joinIndexerVar2.remove(remove.getSubject());
				}
			}
		}
	}
}
