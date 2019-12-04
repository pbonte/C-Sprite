package be.ugent.idlab.csprite.sparql;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;

public class ConceptExtractor extends ElementVisitorBase{
	
	private HashSet<String> concepts;
	private HashSet<String> properties;
	private HashSet<String> subjects;
	private HashSet<String> objects;
	private boolean containsVariableProp;

	public ConceptExtractor() {
		this.concepts = new HashSet<String>();
		this.properties = new HashSet<String>();
		this.subjects = new HashSet<String>();
		this.objects =new HashSet<String>();
		this.containsVariableProp = false;
	}
	
	 public HashSet<String> getConcepts() {
		return concepts;
	}

	public HashSet<String> getProperties() {
		return properties;
	}

	public HashSet<String> getSubjects() {
		return subjects;
	}

	public HashSet<String> getObjects() {
		return objects;
	}
	public boolean getContainsVariableProperty() {
		return containsVariableProp;
	}
	public void visit(ElementPathBlock el) {
         // when it's a block of triples, add in some triple
         ElementPathBlock elCopy = new ElementPathBlock();
         Iterator<TriplePath> triples = el.patternElts();
         while (triples.hasNext()) {
             TriplePath t = triples.next();
            
             
             if(t.getSubject().isURI()) {
            	 subjects.add(t.getSubject().getURI());
             }
             if(t.getObject().isURI()) {
            	 objects.add(t.getObject().getURI());
             }
             if(t.getPredicate().isURI()) {
            	 if(t.getPredicate().getURI().equals(RDF.type.getURI())){
                     concepts.add(t.getObject().getURI());
                 }else {
                	 properties.add(t.getPredicate().getURI());
                 }
             }else {
            	 containsVariableProp = true;
             }
         }
         el = elCopy;
     }

     public void visit(ElementSubQuery el) {
         // get the subquery and walk it
         ElementGroup subQP = (ElementGroup) el.getQuery().getQueryPattern();
         ElementWalker.walk(subQP, this);
     }

     public void visit(ElementOptional el) {
         // get optional elements and walk them
         Element optionalQP = el.getOptionalElement();
         ElementWalker.walk(optionalQP, this);
     }

}
