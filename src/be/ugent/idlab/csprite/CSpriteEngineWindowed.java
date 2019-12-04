/**
 * 
 */
package be.ugent.idlab.csprite;

import org.semanticweb.owlapi.model.OWLOntology;
import be.ugent.idlab.csprite.Event.EventType;
import be.ugent.idlab.csprite.utils.OntologyUtils;

/**
 * @author pbonte
 *
 */
public class CSpriteEngineWindowed extends CSpriteEngine {

	private JoinWindow joinWindow;

	public CSpriteEngineWindowed(OWLOntology ontology) {
		super(ontology);
		this.joinWindow = new JoinWindow(1000);
	}

	public void advanceTime(long time) {

		joinWindow.advanceTime(time);

	}

	public void addTriple(String subject, String property, String object) {
		long time1 = System.nanoTime();
		if (property.equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) {

			String concept = OntologyUtils.strip(object, hierarchyGen);

			if (supertypes.containsKey(concept)) {
				System.out.println("Match: " + subject);
				joinWindow.addEvent(new Event(subject, property, object, System.currentTimeMillis(), EventType.TYPE));

				hits++;
			} else {
				System.out.println("Type no match: " + (System.nanoTime() - time1));
			}
			
		} else {
			String concept = OntologyUtils.strip(property, hierarchyGen);

			if (supertprops.containsKey(concept)) {
				joinWindow.addEvent(
						new Event(subject, property, object, System.currentTimeMillis(), EventType.OBJECTProperty));

			}
		}
	}

	
}
