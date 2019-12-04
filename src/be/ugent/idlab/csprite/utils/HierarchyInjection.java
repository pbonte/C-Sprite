/**
 * 
 */
package be.ugent.idlab.csprite.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.idlab.csprite.parser.HierarchyGenerator;

/**
 * @author pbonte
 *
 */
public class HierarchyInjection {

	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, IOException{
		if(args.length<3){
			System.out.println("Usage: <program name> [file name] [<Query Concept>] [output file] [ontology file]");
			System.exit(0);
		}
		String file = args[0];
		String outputFile = args[2];
		String ontologyFile = args[3];
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ClassLoader classLoader = HierarchyGenerator.class.getClassLoader();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		OWLOntology ont = manager
				.loadOntologyFromOntologyDocument(new File(ontologyFile));
		HierarchyGenerator gen = new HierarchyGenerator(ont);
		String query = OntologyUtils.strip(args[1], gen);
		Map<String,String>  reversedPrefixes = gen.getReveserdPrefixes();
		HashSet<String> supertypes = gen.getSupTypes(query);
		Map<String, List<String>> allSuperTypes = gen.getSuperTypes();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	String[] triple = line.split(" ");
		    	String concept = OntologyUtils.strip(triple[2], gen);
		    	 if(supertypes.contains(concept)){
		    		 //System.out.println(line);
		    		 //System.out.println(allSuperTypes.get(concept));
		    		 int random = getRandomIntegerBetweenRange(-1,allSuperTypes.get(concept).size()-3);
		    		 //System.out.println("between: "+"-1 and " +(allSuperTypes.get(concept).size()-3)+" : "+random);
		    		 if(random == -1){
		    			 writer.write(line+"\n");				    			 
		    		 }else{
		    			 String newConcept = allSuperTypes.get(concept).get(random);
		    			 if(newConcept.equals("Concept")|| newConcept.contains("Thing")){
		    				 writer.write(line);
		    			 }else{
		    				 writer.write(String.format("%s %s <%s> .\n",triple[0],triple[1],OntologyUtils.destrip(newConcept, reversedPrefixes)));
		    			 }
		    		 }
		    	 }else{
		    		 writer.write(line +"\n");
		    	 }
		    }
		}
		writer.close();
	}
	public static int getRandomIntegerBetweenRange(int min, int max){
	    int x = (int)(Math.random()*((max-min)+1))+min;
	    return x;
	}
}
