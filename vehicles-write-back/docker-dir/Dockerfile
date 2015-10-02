FROM java:8-jre
COPY src/main/resources/config.yaml /opt/dropwizard/
COPY target/vehicles-write-back-1.0-SNAPSHOT.jar /opt/dropwizard/
EXPOSE 8080
EXPOSE 8081
WORKDIR /opt/dropwizard
CMD ["java", "-jar", "-Done-jar.silent=true", "vehicles-write-back-1.0-SNAPSHOT.jar", "server", "config.yaml"]