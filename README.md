# C-Sprite

How to run CSprite with the DBPedia live stream experiment:
1) mvn clean install
2) unzip the triples.zip file in experiments/debs
4) go to the target folder
3) java -cp CSprite-0.0.1-SNAPSHOT-jar-with-dependencies.jar be.ugent.idlab.csprite.CSpriteTest ../experiments/debs/dbpedia_stripped3.owl ../experiments/debs/dbpedia_nolonglines.nt "<http://dbpedia.org/ontology/Work>"

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
