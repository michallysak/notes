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

## Generating RSA Keys for JWT

To use RSA for JWT signing and verification, generate your key pair with the following commands:

```bash
openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

Place both `privateKey.pem` and `publicKey.pem` in `notes-app/src/main/resources/`.


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
  * `--persistence=sql` flag uses  panache repository (with PostgreSQL, instruction bellow).

### Run with SQL persistence (PostgreSQL)
A Docker Compose file is available in project root: `compose.yaml`.

1. Start PostgreSQL:
```bash
docker compose up -d
```

2. Run Quarkus with SQL persistence and datasource settings:
```bash
# directly with arguments:
mvn quarkus:dev -Dpersistence=sql \
  -Dquarkus.datasource.db-kind=postgresql \
  -Dquarkus.datasource.username=notes \
  -Dquarkus.datasource.password=notes \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/notes
# Or using profile:
mvn quarkus:dev -Dquarkus.profile=sql
```

3. Stop PostgreSQL when done:
```bash
docker compose down
```

Optional (remove data volume too):
```bash
docker compose down -v
```

