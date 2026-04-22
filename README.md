# Smart Campus Sensor & Room Management API

This project is a JAX-RS RESTful service for the 5COSC022W Client-Server Architectures coursework. It manages rooms, sensors, and sensor-reading history for a Smart Campus system using in-memory Java collections only, which matches the coursework rules.

## API design overview

- Base path: `/api/v1`
- Discovery endpoint: `GET /api/v1`
- Room endpoints: `/api/v1/rooms`
- Sensor endpoints: `/api/v1/sensors`
- Nested reading endpoints: `/api/v1/sensors/{sensorId}/readings`
- Storage strategy: singleton repository using `ConcurrentHashMap` and `CopyOnWriteArrayList`
- Error handling: custom exception mappers for `409`, `422`, `403`, plus a global `500`
- Logging: request and response logging through JAX-RS filters

## Build and run

### Prerequisites

- Java 17 or newer
- Maven 3.9 or newer

### Commands

```powershell
cd smart-campus-api
mvn clean compile
mvn exec:java
```

The API runs on `http://localhost:8080/api/v1`.

## Sample curl commands

```bash
curl http://localhost:8080/api/v1
curl http://localhost:8080/api/v1/rooms
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"ENG-105\",\"name\":\"Engineering Seminar Room\",\"capacity\":60}"
curl http://localhost:8080/api/v1/sensors?type=CO2
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"OCC-100\",\"type\":\"Occupancy\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"ENG-105\"}"
curl http://localhost:8080/api/v1/sensors/CO2-001/readings
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings -H "Content-Type: application/json" -d "{\"value\":425.5}"
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

## Report answers

### Part 1.1

By default, JAX-RS resource classes are request-scoped, so the runtime usually creates a fresh resource instance for each incoming request unless the lifecycle is explicitly changed. That is why shared application data should not be kept in ordinary resource instance fields. In this project, shared state is held in a singleton repository backed by thread-safe collections, which prevents data loss between requests and reduces race-condition risks when multiple clients access the API at the same time.

### Part 1.2

Hypermedia is a hallmark of advanced REST design because responses include navigational information that helps clients discover related resources dynamically. Instead of depending entirely on static documentation, client developers can follow server-provided links and resource maps. This makes the API more self-descriptive, reduces tight coupling, and helps clients adapt more safely when the API evolves.

### Part 2.1

Returning only room IDs minimizes payload size and saves bandwidth, which is useful when the client only needs references. Returning full room objects gives the client more context in a single request and reduces follow-up calls, but increases payload size and transfers more data. In this coursework, full room objects are returned because that makes the API easier to use and gives facilities-management clients immediate access to capacity, names, and linked sensor IDs.

### Part 2.2

DELETE is idempotent because sending the same request multiple times leaves the server in the same final state. In this implementation, the first successful delete removes the room. If the same request is repeated later, the room is already gone and the service returns a successful empty response. The exact response payload may differ, but the final state remains unchanged, which satisfies idempotency.

### Part 3.1

`@Consumes(MediaType.APPLICATION_JSON)` means the method only accepts JSON request bodies. If a client sends a different media type such as `text/plain` or `application/xml`, JAX-RS cannot match the request body to the method's declared input contract and normally returns `415 Unsupported Media Type`. This enforces a clear API contract and avoids unsafe or ambiguous deserialization behavior.

### Part 3.2

Using a query parameter for filtering is generally superior because filtering refines a collection rather than representing a different nested resource. `/sensors?type=CO2` keeps the collection endpoint stable and also scales well when more filters are introduced, such as status or room. A path like `/sensors/type/CO2` is less flexible and becomes awkward when multiple search conditions need to be combined.

### Part 4.1

The Sub-Resource Locator pattern helps keep a large API maintainable by separating top-level resource logic from nested-resource logic. In this project, `SensorResource` handles the sensor collection and delegates `/sensors/{sensorId}/readings` to `SensorReadingResource`. That separation improves readability, keeps classes focused, and prevents a single controller from becoming overloaded with unrelated endpoint logic.

### Part 5.2

HTTP 422 is often more semantically accurate than 404 here because the client successfully reached the sensor-creation endpoint and the JSON body itself is structurally valid, but the request still cannot be processed because it contains an invalid internal reference to a room that does not exist. A 404 normally describes a missing target resource identified by the URL, while 422 describes a valid request entity with semantically invalid content.

### Part 5.4

Exposing internal Java stack traces is dangerous because it leaks implementation details that attackers can use to study the system. A stack trace can reveal package names, class names, method names, dependency versions, file paths, and code flow. That information helps attackers fingerprint the technology stack and prepare more targeted attacks, so production APIs should return sanitized error responses instead.

### Part 5.5

JAX-RS filters are a better place for cross-cutting concerns like logging because they centralize behavior that should apply consistently across all endpoints. This avoids repeating logging statements in every resource method, reduces the chance of missing logs on some routes, and keeps the resource classes focused on business logic instead of infrastructure concerns.
