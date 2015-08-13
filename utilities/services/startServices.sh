#!/bin/bash
# start a bash session 
screen -AdmS services -t bash bash
screen -S services -X screen -t mongo mongod --dbpath=/users/jkidd/data/mongo/db
screen -S services -X select 0
screen -S services -X screen -t zookeeper $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
screen -S services -X select 0
screen -S services -X screen -t kafka $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
screen -S services -X select 0

# list kafka topcis 
# $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper localhost:2181

# create a kafka topic 
# $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic Simulated

# deleting topics 
# $KAFKA_HOME/bin/kafka-topics.sh --zookeeper zk_host:port/chroot --delete --topic my_topic_name

# server.properties settings for auto creating and deleting topics 
# delete.topic.enable=true
# auto.create.topics.enable = true

# killing screen session 
# screen -X -S kafkaRuntime quit

# consumer 
# $KAFKA_HOME/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic OPC --from-beginning

# stopping kafka
# $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
