#!/bin/sh


# Set the external host and port
if [ ! -z "$ADVERTISED_HOST" ]; then
    echo "advertised host: $ADVERTISED_HOST"
    sed -r -i "s/#(advertised.host.name)=(.*)/\1=$ADVERTISED_HOST/g" $KAFKA_HOME/config/server.properties
fi
if [ ! -z "$ADVERTISED_PORT" ]; then
    echo "advertised port: $ADVERTISED_PORT"
    sed -r -i "s/#(advertised.port)=(.*)/\1=$ADVERTISED_PORT/g" $KAFKA_HOME/config/server.properties
fi

echo "auto.create.topics.enable = true" >> $KAFKA_HOME/config/server.properties
echo "delete.topic.enable=true" >> $KAFKA_HOME/config/server.properties

# Run Kafka
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties