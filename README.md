akka-persistence-event-sourcing
==============================

### Dependencies
#### Eventstore (geteventstore.com)

```
cd docker
docker build -t petervdm/eventstore:3.2.2 .
docker run --name eventstore -d -p 2113:2113 -p 1113:1113 petervdm/eventstore:3.2.2
```

### Overview
Example project with a simple CRUD REST API to a domain model persisted using akka-persistence with event sourcing.


To start the spray-can server from sbt:
> re-start

To stop:
> re-stop


### Project summary

- simple CRUD REST API
- Spray "actor per request" model inspired by spray-actor-per-request Activator template and Cameo Pattern from Jamie Allen's "Effective Akka"
- simple domain model representing a Vehicle and User
- akka-persistence event sourcing used to track changes to the domain model
