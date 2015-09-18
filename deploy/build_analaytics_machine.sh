TS=$(date +%s)

VM=emr-$TS

docker-machine create --driver virtualbox $VM

eval "$(docker-machine env $VM)"

cd mongo

docker build --no-cache --tag emr/mongo .

cd ../base

docker build --no-cache --tag emr/base .

cd ../spark

# curl -o spark-1.4.1-bin-hadoop2.6.tgz http://apache.mirrors.hoobly.com/spark/spark-1.4.1/spark-1.4.1-bin-hadoop2.6.tgz

# curl -o spark-streaming-kafka-assembly_2.10-1.4.1.jar http://central.maven.org/maven2/org/apache/spark/spark-streaming-kafka-assembly_2.10/1.4.1/spark-streaming-kafka-assembly_2.10-1.4.1.jar

docker build --no-cache --tag emr/spark .

cd ../kafka

docker build --no-cache --tag emr/kafka .

cd ../analytics

cp ../bin/analytics-service-1.0-SNAPSHOT.jar analytics-service-1.0-SNAPSHOT.jar

cp -r ../../algorithms algorithms

cp -r ../../utilities/pyspark-algorithms pyspark-algorithms 

docker build --no-cache --tag emr/analytics .

cd ../studio

cp ../bin/studio-1.0.zip studio-1.0.zip

cp ../bin/plugins-1.0-SNAPSHOT.jar plugins-1.0-SNAPSHOT.jar

docker build --no-cache --tag emr/studio .

cd ..

docker save -o bin/emr-analytics.tar emr/studio:latest emr/analytics:latest emr/spark:latest emr/mongo:latest emr/kafka:latest emr/base:latest ubuntu:latest







