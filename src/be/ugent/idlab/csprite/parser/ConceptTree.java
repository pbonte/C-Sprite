/**
 * 
 */
package be.ugent.idlab.csprite.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pbonte
 *
 */
public class ConceptTree {

	private ConceptNode root;
	private Map<String,ConceptNode> conceptIndex;
	public ConceptTree(ConceptNode root){
		this.root=root;
		this.conceptIndex=new HashMap<String,ConceptNode>();
		this.conceptIndex.put(root.getConcept(), root);
	}
	public ConceptTree(String root){
		this(new ConceptNode(root));
	}
	public void addConcept(String child,String parent){
		if(!conceptIndex.containsKey(child)){
			conceptIndex.put(child, new ConceptNode(child));
		}
		if(!conceptIndex.containsKey(parent)){
			conceptIndex.put(parent, new ConceptNode(parent));
		}
		ConceptNode childNode = conceptIndex.get(child);
		ConceptNode parentNode = conceptIndex.get(parent);
		parentNode.addChild(childNode);
		childNode.addParent(parentNode);
	}
	public ConceptNode getRoot(){
		return root;
	}
	public ConceptNode getNode(String index){
		return conceptIndex.get(index);
	}
}
