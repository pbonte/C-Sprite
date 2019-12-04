[![Build Status](https://travis-ci.com/pbonte/C-Sprite.svg?branch=master)](https://travis-ci.com/pbonte/C-Sprite)
# C-Sprite

How to use C-Sprite:

```
USAGE: <Ontology location> <input type (file|socket)> <triples file|socket url> <rdf type (ntriples|json-ld)> <query file>  <windowSize> <windowSlide> <sleep (file)>

```
With 

* Ontology location: the location of the ontology TBox.
* input type (file|socket): either file or socket. Depending if you read from file or a websocket.
* triples file|socket url: the location of the triples that need to be streamed if you read from file. Or the websocket url when you want to connect to a websocket.
* rdf type (ntriples|json-ld): the RDF serialization, either json-ld or N-triples are currently supported.
* query file: a file containing SPARQL queries on each line. Make to remove the newlines in your query such that each query is on a separate line.
* windowSize: the size of the window
* windowSlide: the slide of window
* sleep: the sleep time between each line that needs to be read when reading from file. Use 0 when using the socket.

How to run C-Sprite with the DBPedia live stream experiment:
1) mvn clean install
2) unzip the triples.zip file in experiments/debs
4) go to the target folder
3) java -cp CSprite-0.0.2-SNAPSHOT-jar-with-dependencies.jar be.ugent.idlab.csprite.CSpriteSPARQLTest ../experiments/debs/dbpedia_strippedowl file ntriples ../experiments/debs/dbpedia_nolonglines.nt ../experiments/debs/queries.q 2 1 0

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
