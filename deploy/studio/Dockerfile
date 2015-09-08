# Dockerizing Analytics Studio

FROM emr/base:latest

MAINTAINER PSS Applied Research 

# install unzip 
RUN apt-get -y update && \
   apt-get -y install unzip

# copy the studio zip file 
COPY studio-1.0.zip /opt/

# unzip 
RUN unzip /opt/studio-1.0.zip -d /opt/ && \
   rm /opt/studio-1.0.zip

# copy plugins jar 
COPY plugins-1.0-SNAPSHOT.jar /opt/plugins/target/plugins-1.0-SNAPSHOT.jar

# expose port 9000 from the container to the host 
EXPOSE 9000

WORKDIR /opt/studio-1.0

ENTRYPOINT ["/opt/studio-1.0/bin/./studio"]
