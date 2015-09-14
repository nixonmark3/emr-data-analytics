
if [ ! -z $1 ] 
then 
    VM=$1
    
    # set the the active docker vm
    eval "$(docker-machine env $VM)"

    # remove containers
    docker rm -f kafka
    docker rm -f analytics
    docker rm -f studio

    # set host ip as environment variable
    ANALYTICS_HOST=$(docker-machine ip $VM)

    # restart the mongo container
    docker restart mongo

    # run all the other containers 
	docker run --name kafka --net=host -p 2181:2181 -p 9092:9092 -d --env ADVERTISED_HOST=$ANALYTICS_HOST --env ADVERTISED_PORT=9092 emr/kafka
	docker run --name analytics --net=host --volumes-from analyticsData --env ANALYTICS_HOST=$ANALYTICS_HOST -p 2552:2552 -d emr/analytics
	docker run --name studio --net=host --volumes-from analyticsData --env ANALYTICS_HOST=$ANALYTICS_HOST -p 9000:9000 -d emr/studio

	echo
	docker-machine ip $VM
	echo
else
    echo please provide virtual machine name
fi