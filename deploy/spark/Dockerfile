# Dockerizing Spark

FROM emr/java8:latest

MAINTAINER PSS Applied Research

COPY spark-1.4.1-bin-hadoop2.6.tgz /usr/local/spark-1.4.1-bin-hadoop2.6.tgz

RUN cd /usr/local && \
   tar -xzvf spark-1.4.1-bin-hadoop2.6.tgz && \
   rm spark-1.4.1-bin-hadoop2.6.tgz && \
   ln -s spark-1.4.1-bin-hadoop2.6 spark

ENV SPARK_HOME /usr/local/spark
