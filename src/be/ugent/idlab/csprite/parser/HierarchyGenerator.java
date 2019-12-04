/**
 * 
 */
package be.ugent.idlab.csprite.parser;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;


/**
 * @author pbonte
 *
 */
public class HierarchyGenerator {
	/**
	 * Generates the full ontology hiearchy in EPL
	 * 
	 */
	private static Map<String,String> prefixMapper = new HashMap<String,String>();
	private static Set<String> supportedConcepts = new HashSet<String>();
	private static char counter = 'A';
	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ClassLoader classLoader = HierarchyGenerator.class.getClassLoader();

		OWLOntology ont = manager
				.loadOntologyFromOntologyDocument(new File(classLoader.getResource("dbpedia_stripped2.owl").getFile()));
		OWLDataFactory factory = manager.getOWLDataFactory();
		// find the top classes:
		HierarchyGenerator gen = new HierarchyGenerator(ont);
		gen.generateHierarchy();
		System.out.println(gen.getSuperTypes());
	}
	private OWLOntology ont;
	public HierarchyGenerator(OWLOntology ontology){
		this.ont = ontology;
		this.generateHierarchy();
	}
	
	public static Map<String,String> getPrefixes(){
		return prefixMapper;
	}
	public static Set<String> getSupportedConcepts(){
		return supportedConcepts;
	}
	public static Map<String,String> getReveserdPrefixes(){
		Map<String,String> reveserd = new HashMap<String,String>();
		for(Entry<String,String> ent:prefixMapper.entrySet()){
			reveserd.put(ent.getValue(), ent.getKey());
		}
		return reveserd;
	}
	private ConceptHandler conceptHandler;
	private ConceptHandler roleHandler;
	private void  generateHierarchy() {
		prefixMapper.clear();
		supportedConcepts.clear();

		this.conceptHandler = new ConceptHandler("Concept");
		this.roleHandler = new ConceptHandler("Property");
		generateConcepts(ont, conceptHandler);
		generateObjectProps(ont, roleHandler);
			
	}
	public HashSet<String> getSupTypes(String superType){
		HashSet<String> subTypes = conceptHandler.getSubsTypes(superType);
		return subTypes;
	}
	public HashSet<String> getSupProperties(String superProp){
		HashSet<String> subProps = roleHandler.getSubsTypes(superProp);
		return subProps;
	}
	public Map<String,List<String>> getSuperTypes(){
		return conceptHandler.getSuperTypes();
	}
	public static void generateConcepts(OWLOntology ont, ConceptHandler results) {
		Set<OWLClass> superClasses = new HashSet<OWLClass>();
		
		// results.append("//Concepts\n");
		// Map<String,Set<String>> inherits = new HashMap<String,Set<String>>();
		for (OWLClass cls : ont.classesInSignature().collect(Collectors.toSet())) {
			supportedConcepts.add("<"+cls.getIRI().getIRIString()+">");
			if (!ont.subClassAxiomsForSubClass(cls).findAny().isPresent()) {
				superClasses.add(cls);
				// inherits.put(cls.toString(), Collections.emptySet());
			}
		}
		// generate the subsumption tree depth first
		for (OWLClass superCls : superClasses) {
			// results.append(generateEPL(strip(superCls), "Concept"));
			results.add(strip(superCls), "Concept");
			generateTree(ont, superCls, results);
		}
	}

	public static void generateObjectProps(OWLOntology ont, ConceptHandler result) {
		Set<OWLObjectProperty> superObjProps = new HashSet<OWLObjectProperty>();

		// result.append("//Object Properties\n");
		// find the top objects
		for (OWLObjectProperty obj : ont.objectPropertiesInSignature().collect(Collectors.toSet())) {
			supportedConcepts.add("<"+obj.getIRI().getIRIString()+">");
			if (!ont.objectSubPropertyAxiomsForSubProperty(obj).findAny().isPresent()) {
				superObjProps.add(obj);
			}
		}
		for (OWLObjectProperty superProp : superObjProps) {
			result.add(strip(superProp), "Property");
			generateTree(ont, superProp, result);
		}
	}

	public static void generateTree(OWLOntology ontology, OWLClass cls, ConceptHandler result) {
		Set<OWLSubClassOfAxiom> subClasses = ontology.subClassAxiomsForSuperClass(cls).collect(Collectors.toSet());

		for (OWLSubClassOfAxiom subClsAx : subClasses) {

			// result.append(generateEPL(subClsAx.getSubClass(), cls));
			result.add(strip(subClsAx.getSubClass()), strip(cls));
			generateTree(ontology, subClsAx.getSubClass().asOWLClass(), result);
		}
	}

	public static void generateTree(OWLOntology ontology, OWLObjectProperty cls, ConceptHandler result) {
		Set<OWLSubObjectPropertyOfAxiom> subClasses = ontology.objectSubPropertyAxiomsForSuperProperty(cls)
				.collect(Collectors.toSet());

		for (OWLSubObjectPropertyOfAxiom subClsAx : subClasses) {
			result.add(strip(subClsAx.getSubProperty().asOWLObjectProperty()), strip(cls));
			generateTree(ontology, subClsAx.getSubProperty().asOWLObjectProperty(), result);
		}
	}



	public static String strip(OWLClassExpression clsExp) {
		String uri = clsExp.asOWLClass().getIRI().getIRIString();
		return encode(uri);
	}
	public static String encode(String uri){
		if (uri.startsWith("<")) {
			uri = uri.substring(1);
		}
		if (uri.endsWith(">")) {
			uri = uri.substring(0, uri.length());
		}
		char findChar = '/';
		if (uri.contains("#")) {
			findChar = '#';
		}
		String prefix = uri.substring(0,uri.lastIndexOf(findChar));
		String stripped = uri.substring(uri.lastIndexOf(findChar) + 1, uri.length());
		
		if(!prefixMapper.containsKey(prefix)){
			prefixMapper.put(prefix,(counter++) +"");

		}
		String tempCounter = prefixMapper.get(prefix);
		if(tempCounter.equals("")){
			System.out.println();
		}
		
		return tempCounter+"_"+removeSpecialChars(stripped);
	}
	public static char getNextCounter() {
		return counter++;
	}
	public static String removeSpecialChars(String uri){
		String result= uri;
		result = result.replaceAll("%", "");
		result = result.replaceAll("\\.", "");
		result = result.replaceAll("/", "");
		result = result.replaceAll("\\\\", "");
		result = result.replaceAll("-", "");
		result = result.replaceAll(",", "");
		result = result.replaceAll(":", "");

		return result;


	}
	public static String strip(OWLObjectProperty clsExp) {
		String uri = clsExp.getIRI().getIRIString();
		return encode(uri);
	}


}
