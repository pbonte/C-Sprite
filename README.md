[![Build Status](https://travis-ci.com/pbonte/C-Sprite.svg?branch=master)](https://travis-ci.com/pbonte/C-Sprite)
# C-Sprite

C-Sprite is an RDF stream processing engine to perform efficient hierarchical reasoning. All the technical details can be found [here](https://biblio.ugent.be/publication/8635009/file/8635011).

## How to use C-Sprite with SPARQL queries:

```
USAGE: <Ontology location> <input type (file|socket)> <triples file|socket url> <rdf type (ntriples|json-ld)> <query file>  <windowSize> <windowSlide> <sleep (file)>

```
With 

* _Ontology location_: the location of the ontology TBox.
* _input type (file|socket)_: either file or socket. Depending if you read from file or a websocket.
* _triples file|socket url_: the location of the triples that need to be streamed if you read from file. Or the websocket url when you want to connect to a websocket.
* _rdf type (ntriples|json-ld)_: the RDF serialization, either json-ld or N-triples are currently supported.
* _query file_: a file containing SPARQL queries on each line. Make to remove the newlines in your query such that each query is on a separate line.
* _windowSize_: the size of the window
* _windowSlide_: the slide of window
* _sleep_: the sleep time between each line that needs to be read when reading from file. Use 0 when using the socket.

How to run C-Sprite with the DBPedia live stream experiment:
1) mvn clean install
2) unzip the triples.zip file in experiments/debs
4) go to the target folder
3) java -cp CSprite-0.0.2-SNAPSHOT-jar-with-dependencies.jar be.ugent.idlab.csprite.CSpriteSPARQLTest ../experiments/debs/dbpedia_stripped3.owl file ntriples ../experiments/debs/dbpedia_nolonglines.nt ../experiments/debs/queries.q 2 1 0

## How to use C-Sprite with Type queries:

```
USAGE: <Ontology location> <triples file> <query concept>
```
With 
* <Ontology location> the location of the ontology TBox.
* <triples files> the location of the triples that need to be streamed. 
* <query concept> the query that needs to be executed over the data stream. This should be a concept from the used ontology TBox.

How to run C-Sprite with the DBPedia live stream experiment:
1) mvn clean install
2) unzip the triples.zip file in experiments/debs
4) go to the target folder
3) java -cp CSprite-0.0.2-SNAPSHOT-jar-with-dependencies.jar be.ugent.idlab.csprite.CSpriteTest ../experiments/debs/dbpedia_stripped3.owl ../experiments/debs/dbpedia_nolonglines.nt "<http://dbpedia.org/ontology/Work>"

How to cite:
```
@inproceedings{bonte2019c,
  title={C-Sprite: Efficient Hierarchical Reasoning for Rapid RDF Stream Processing},
  author={Bonte, Pieter and Tommasini, Riccardo and De Turck, Filip and Ongenae, Femke and Valle, Emanuele Della},
  booktitle={Proceedings of the 13th ACM International Conference on Distributed and Event-based Systems},
  pages={103--114},
  year={2019},
  organization={ACM}
}
```
