## Dockerizing Spark

FROM emr/base:latest

MAINTAINER PSS Applied Research

COPY spark-1.4.1-bin-hadoop2.6.tgz /usr/local/spark-1.4.1-bin-hadoop2.6.tgz

RUN cd /usr/local && \
   tar -xzvf spark-1.4.1-bin-hadoop2.6.tgz && \
   rm spark-1.4.1-bin-hadoop2.6.tgz && \
   ln -s spark-1.4.1-bin-hadoop2.6 spark

# copy over kafka spark streaming library
COPY spark-streaming-kafka-assembly_2.10-1.4.1.jar /usr/local/spark/external/kafka-assembly/spark-streaming-kafka-assembly_2.10-1.4.1.jar

ENV SPARK_HOME /usr/local/spark
