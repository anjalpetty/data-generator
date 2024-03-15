
#### Running the data generator from command line to generate synthetic data
```
cd  <repo>/data-generator
java -cp ./target/data-generator-<version>-SNAPSHOT-jar-with-dependencies.jar \
com.orange.dataeng.JsonGenerator \
-t src/main/resources/templates/election.json \
-n <numRecords> \
> /path/to/generated/data/election-generated-data.json
```
e.g.
```
java -cp ./target/data-generator-0.1-SNAPSHOT-jar-with-dependencies.jar \
com.orange.dataeng.JsonGenerator \
-t src/main/resources/templates/election.json  \
-n 200 \
> /my/generated/data/election-generated-data.json
```

e.g.
```
java -cp ./target/data-generator-0.1-SNAPSHOT-jar-with-dependencies.jar \
com.orange.dataeng.CSVGenerator \
-t src/main/resources/templates/election.json  \
-n 200 \
> /my/generated/data/election-generated-data.json
```
