/**
 * 
 */
package be.ugent.idlab.csprite.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import be.ugent.idlab.csprite.parser.HierarchyGenerator;

/**
 * @author pbonte
 *
 */
public class OntologyUtils {
	
	/**
	 * Generates all the leaf classes in an ontology
	 * @param ontology
	 * @return
	 */
	public static Set<OWLClass> getLeafs(OWLOntology ontology){
		Set<OWLClass> leafs = new HashSet<OWLClass>();
		for(OWLClass cs:ontology.classesInSignature().collect(Collectors.toSet())){
			if(!ontology.subClassAxiomsForSuperClass(cs).findAny().isPresent()){
				leafs.add(cs);
			}
		}
		return leafs;
	}
	/**
	 * Generates all the leaf classes in a specific branch indicated by the superClass
	 * @param ontology
	 * @param superClass
	 * @return
	 */
	public static Set<OWLClass> getLeafs(OWLOntology ontology, OWLClass superClass){
		Set<OWLClass> leafs = new HashSet<OWLClass>();
		getLeafs_helper(ontology, superClass, leafs);
		return leafs;
	}
	private static void getLeafs_helper(OWLOntology ontology, OWLClass superClass,Set<OWLClass> leafs){
		for(OWLSubClassOfAxiom cs:ontology.subClassAxiomsForSuperClass(superClass).collect(Collectors.toSet())){
			if(!ontology.subClassAxiomsForSuperClass(cs.getSubClass().asOWLClass()).findAny().isPresent()){
				leafs.add(cs.getSubClass().asOWLClass());
			}else{
				getLeafs_helper(ontology,cs.getSubClass().asOWLClass(),leafs);
			}
		}
	}
	
	public static String strip(OWLEntity ent){
		String iriString = ent.getIRI().getIRIString();
		char findChar = '/';
		if(iriString.contains("#")){
			findChar = '#';
		}
		return iriString.substring(iriString.lastIndexOf(findChar)+1,iriString.length());
		
	}
	public static String strip(String uri){
		if(uri.startsWith("<")){
			uri = uri.substring(1);
		}
		if(uri.endsWith(">")){
			uri = uri.substring(0, uri.length()-1);
		}
		char findChar = '/';
		if(uri.contains("#")){
			findChar = '#';
		}
		return uri.substring(uri.lastIndexOf(findChar)+1,uri.length());
	}
	public static String removeHooks(String uri) {
		return uri.substring(1, uri.length()-1);
		
	}
	public static String strip(String uri, HierarchyGenerator hierarch){
		if(uri.startsWith("<")){
			uri = uri.substring(1);
		}
		if(uri.endsWith(">")){
			uri = uri.substring(0, uri.length()-1);
		}
		//uri = uri.substring(1, uri.length()-1);
		char findChar = '/';
		if(uri.contains("#")){
			findChar = '#';
		}
		String prefix = uri.substring(0,uri.lastIndexOf(findChar));
		String stripped = uri.substring(uri.lastIndexOf(findChar) + 1, uri.length());
		String smallPrefix="";
		if(!hierarch.getPrefixes().containsKey(prefix)){
			hierarch.getPrefixes().put(prefix,hierarch.getNextCounter()+"");
		}	
		smallPrefix=hierarch.getPrefixes().get(prefix);
		
		return smallPrefix+"_"+removeSpecialChars(stripped);
		
	}
	public static String destrip(String uri, Map<String,String> reversredprefixes){

		int index = uri.indexOf("_");
		String prefix = uri.substring(0, index);
		String rest = uri.substring(index+1);
		
		return reversredprefixes.get(prefix)+"/"+rest;
		
	}
	public static String encode(OWLEntity ent){
		String iriString = ent.getIRI().getIRIString();
		
		return encode(iriString);
		
	}
	public static String encode(String uri){
//		try {
//			return URLEncoder.encode(uri, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return strip(uri);
	}
	public static String encode(String uri,HierarchyGenerator hierarch){
		return strip(uri, hierarch);
	}
	public static String decode(String uri){
		try {
			return URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static String removeSpecialChars(String uri){
		String result= uri;
		result = result.replaceAll("%", "");
		result = result.replaceAll("/", "");
		result = result.replaceAll("\\\\", "");
		result = result.replaceAll("-", "");
		result = result.replaceAll(",", "");
		result = result.replaceAll(":", "");

		return result;


	}

}
