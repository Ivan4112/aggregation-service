# Users Aggregation Service

A Spring Boot service that aggregates users data from multiple, independently
configured databases (PostgreSQL, MySQL, Oracle) and exposes them through a
single REST endpoint:

```
GET /users
```

## Running with Docker Compose

The Compose stack starts all three databases (PostgreSQL, MySQL, Oracle) and the
application itself. On startup the app runs the Liquibase migrations against each
database, creating and seeding the tables.

```bash
docker compose up --build
```

The `app` service waits for all three databases to become healthy before it
starts (Oracle can take a couple of minutes on first run).

Once the stack is up, the endpoint is available at `http://localhost:8080/users`:
Also there is swagger documentation available at `http://localhost:8080/swagger-ui.html`.
```bash
curl http://localhost:8080/users
curl "http://localhost:8080/users?username=user-1"
```
