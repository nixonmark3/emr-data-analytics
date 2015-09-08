# Dockerizing the Analytics Service 

FROM emr/spark:latest 

MAINTAINER PSS Applied Research

# retrieve the analytics service jar 
COPY analytics-service-1.0-SNAPSHOT.jar /opt/analytics/analytics-service-1.0-SNAPSHOT.jar

# copy over python algorithms 
COPY algorithms /opt/algorithms

# copy over pyspark algorithms 
COPY pyspark-algorithms /opt/utilities/pyspark-algorithms

WORKDIR /opt/analytics

EXPOSE 2552

ENTRYPOINT ["java", "-cp", "/opt/analytics/analytics-service-1.0-SNAPSHOT.jar", "emr.analytics.service.JobServer"]
