# Dockerizing MongoDB: Dockerfile for building MongoDB images
# Based on ubuntu:latest, installs MongoDB following the instructions from:
# http://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/

# parent of this dockerized image
# Format: FROM repository[:version]
FROM ubuntu:latest

# Format: MAINTAINER Name <email@addr.ess>
MAINTAINER PSS Applied Research

# Import MongoDB public GPG key AND create a MongoDB list file 
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.0.list

# update apt-get sources AND install MongoDB
RUN apt-get update && apt-get install -y mongodb-org

# create the mongodb data directory 
RUN mkdir -p /data/db

# expose port 27017 from the container to the host 
EXPOSE 27017

# set usr/bin/mongod as the dockerized entry-point application 
ENTRYPOINT ["/usr/bin/mongod"]
