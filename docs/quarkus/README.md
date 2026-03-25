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
