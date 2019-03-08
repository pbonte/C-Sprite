# C-Sprite

How to run CSprite with the DBPedia live stream experiment:
1) mvn clean install
2) unzip the triples.zip file in experiments/edbt
4) go to the target folder
3) java -cp CSprite-0.0.1-SNAPSHOT-jar-with-dependencies.jar be.ugent.idlab.csprite.CSpriteTest ../experiments/debs/dbpedia_stripped3.owl ../experiments/debs/dbpedia_nolonglines.nt "<http://dbpedia.org/ontology/Work>"
