
if [ ! -z $1 ] 
then 
    VM=$1
    
    # set the the active docker vm
    eval "$(docker-machine env $VM)"

    # remove containers
    docker rm -f $(docker ps -a -q)

    # set host ip as environment variable
    ANALYTICS_HOST=$(docker-machine ip $VM)

    cd bin
    
    # run all the other containers 
	docker run --name mongo -p 27017:27017 -d emr/mongo
	java -cp Wrapper-1.0.jar emr.analytics.wrapper.Main
	docker run --name kafka --net=host -p 2181:2181 -p 9092:9092 -d --env ADVERTISED_HOST=$ANALYTICS_HOST --env ADVERTISED_PORT=9092 emr/kafka
	docker create -v /opt/output --name analyticsData emr/base
	docker run --name analytics --net=host --volumes-from analyticsData --env ANALYTICS_HOST=$ANALYTICS_HOST -p 2552:2552 -d emr/analytics
	docker run --name studio --net=host --volumes-from analyticsData --env ANALYTICS_HOST=$ANALYTICS_HOST -p 9000:9000 -d emr/studio

	echo
	docker-machine ip $VM
	echo
else
    echo please provide virtual machine name
fi