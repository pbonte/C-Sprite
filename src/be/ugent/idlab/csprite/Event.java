/**
 * 
 */
package be.ugent.idlab.csprite;

/**
 * @author pbonte
 *
 */
public class Event {
	private long ts;
	private String subject, property, object;
	public enum EventType {TYPE, OBJECTProperty, DataProperty};
	private EventType selectedType;
	public Event(String subject, String property, String object, long ts, EventType selectedType) {
		this.subject = subject;
		this.property = property;
		this.object = object;
		this.ts = ts;
		this.selectedType=selectedType;
	}


	public long getTs() {
		return ts;
	}


	public String getSubject() {
		return subject;
	}


	public String getProperty() {
		return property;
	}


	public String getObject() {
		return object;
	}
	public EventType getType(){
		return selectedType;
	}

	
}
