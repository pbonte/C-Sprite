/**
 * 
 */
package be.ugent.idlab.csprite.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author pbonte
 *
 */
public class ConceptHandler {

	private ConceptTree conceptTree;
	private Map<String, List<String>> superTypes;

	public ConceptHandler(String root) {
		this.conceptTree = new ConceptTree(root);
	}

	public void add(String concept, String superConcept) {
		conceptTree.addConcept(concept, superConcept);
	}

	public HashSet<String> getSubsTypes(String superType) {
		HashSet<String> subTypes = new HashSet<String>();
		ConceptNode root = conceptTree.getNode(superType);
		if (root != null) {
			subTypes.add(root.getConcept());
			// traverse tree to find
			Queue<ConceptNode> queue = new LinkedList<ConceptNode>();
			queue.add(root);
			HashSet<ConceptNode> visited = new HashSet<ConceptNode>();
			visited.add(root);
			while (!queue.isEmpty()) {
				ConceptNode node = queue.remove();
				for (ConceptNode child : node.getChilderen()) {
					if (!visited.contains(child)) {
						subTypes.add(child.getConcept());
						queue.add(child);
					}
				}

			}
		}else {
			subTypes.add(superType);
		}
		return subTypes;
	}

	public Map<String, List<String>> getSuperTypes() {
		// BFS uses Queue data structure
		Map<String, List<String>> superTypes = new HashMap<String, List<String>>();
		Queue<ConceptNode> queue = new LinkedList<ConceptNode>();
		queue.add(conceptTree.getRoot());
		HashSet<ConceptNode> visited = new HashSet<ConceptNode>();
		visited.add(conceptTree.getRoot());
		while (!queue.isEmpty()) {
			ConceptNode node = queue.remove();
			for (ConceptNode child : node.getChilderen()) {
				if (!visited.contains(child)) {
					if (!superTypes.containsKey(child.getConcept())) {
						superTypes.put(child.getConcept(), new ArrayList<String>());
					}
					superTypes.get(child.getConcept()).add(node.getConcept());
					if (superTypes.containsKey(node.getConcept())) {
						superTypes.get(child.getConcept()).addAll(superTypes.get(node.getConcept()));
					}
					queue.add(child);
				}
			}

		}
		return superTypes;
	}
}
