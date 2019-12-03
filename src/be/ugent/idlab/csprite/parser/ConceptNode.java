/**
 * 
 */
package be.ugent.idlab.csprite.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author pbonte
 *
 */
public class ConceptNode {
	private String concept;
	private Set<ConceptNode> childeren;
	private Set<ConceptNode> parents;
	
	public ConceptNode(String concept){
		this.concept= concept;
		childeren=new HashSet<ConceptNode>();
		parents=new HashSet<ConceptNode>();
	}
	public void addChild(ConceptNode child){
		childeren.add(child);
	}
	public void addParent(ConceptNode parent){
		parents.add(parent);
	}
	public String getConcept(){
		return concept;
	}
	public Set<ConceptNode> getChilderen() {
		return childeren;
	}
	public Set<ConceptNode> getParents() {
		return parents;
	}
	public String toEPL(){
		if(parents!=null&&!parents.isEmpty()){
			String superConceptString = parents.stream().map(p->p.getConcept()).collect(Collectors.joining(", "));
			return String.format("create schema %s() inherits %s;", concept,superConceptString);
		}else{
			return String.format("create schema %s() ;", concept);
		}
	}

}
