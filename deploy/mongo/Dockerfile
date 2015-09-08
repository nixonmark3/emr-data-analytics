## Mongo container 
#  https://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/

FROM ubuntu:latest 

MAINTAINER PSS Applied Research

# import the public ket used by the package management system 
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10

# creat ea list file for MongoDB 
RUN echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.0.list

# update apt-get sources AND install MongoDB
RUN apt-get -y update && apt-get install -y mongodb-org

# create the mongodb data directory 
RUN mkdir -p /data/db

# expose port 27017 from the container to the host 
EXPOSE 27017

# set usr/bin/mongod as the dockerized entry-point application 
ENTRYPOINT ["/usr/bin/mongod"]
