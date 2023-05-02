# Settings Service

## Technologies

 * Microservice Framework: `Spring Boot` 
 * Service Discovery: `Eureka Client`
 * Database: `PostgreSQL` 
 * DB Migration: `Flyway`
 * Test DB: `H2` 
 
## Building

To build the settings-service, change into project-directory of settings-service.
Then execute the maven command:
```bash
mvn install
```

## Configuration

The configuration of the database is in the `.env` file which is located in the project's home directory.
An example file looks like this:

```bash
DATABASE_NAME_SETTINGS=settings
DATABASE_USER=user
DATABASE_PASSWORD=pass
DATABASE_PORT=5432
```

## Documentation   

## Overview 

Link to Overview: [README-OVERVIEW](../README.md)