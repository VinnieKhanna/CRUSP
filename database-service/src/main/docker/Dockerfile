FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN apk update && apk add --no-cache bash
RUN apk add --no-cache postgresql-client
COPY database-service-0.1.0-SNAPSHOT.jar app.jar
COPY wait-for-postgres.sh wait-for-postgres.sh
ENTRYPOINT ["./wait-for-postgres.sh", "postgres-database-service", "--", "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]