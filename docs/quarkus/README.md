# Notes Quarkus Application

This is a Quarkus-based notes application following a layered architecture:

```
Presentation (Resources)
   ↓
Controller (Controllers)
   ↓
Domain Layer (Entities, Validators, Services)
   ↓
Persistence (Repositories)
```

**Note**

The Quarkus application exposes REST endpoints via controllers, which delegate to domain logic. The controller layer is present, unlike the CLI variant.

## Run the Quarkus application:
```bash
mvn exec:java -Dexec.args="--quarkus --persistence=in-memory"
```
### Development mode:
```bash
mvn quarkus:dev
```
### Arguments:
* Persistence:
  * `--persistence=in-memory` flag uses an in-memory repository (default).
