#! /bin/bash
if [ $# -gt 2 ]; then
	
	append='</owl:Class><owl:Class rdf:about="'${2}'_Top"></owl:Class>\n'
	append+='<owl:Class rdf:about="'${2}'_0"><rdfs:subClassOf rdf:resource="'${2}'_TOP"/></owl:Class>\n'
	for i in $(seq 1 $(($3-1)))
	do
		append+='<owl:Class rdf:about="'${2}_${i}'"><rdfs:subClassOf rdf:resource="'${2}_$(($i-1))'"/></owl:Class>\n'
	done
	append+='<owl:Class rdf:about="'${2}'"><rdfs:subClassOf rdf:resource="'${2}_$(($3-1))'"/>\n'
	#echo -e $append
	cat $1|sed 's~</owl:Class><owl:Class rdf:about="http://dbpedia.org/ontology/Work">~'"$append"'~'
else
	echo "Paramters: <ontology file> <exending iri> <length>"
fi
