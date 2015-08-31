# Dockerizing Analytics Studio

FROM emr/java8:latest

MAINTAINER PSS Applied Research 

# install unzip 
RUN apt-get update && \
   apt-get install unzip

# copy the studio zip file 
COPY studio-1.0.zip /opt/

# unzip 
RUN unzip /opt/studio-1.0.zip -d /opt/ && \
   rm /opt/studio-1.0.zip

# expose port 9000 from the container to the host 
EXPOSE 9000

ENTRYPOINT ["/opt/studio-1.0/bin/./studio"]
