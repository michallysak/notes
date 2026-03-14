# Notes CLI Application

This is a simple command-line notes application following a layered architecture:
 

```
Presentation (CLI Note Presenter)
   ↓
Domain Layer (Entities, Validators, Services)
   ↓
Persistence (Repositories)
```

**Note**

For simplicity intentionally omitting controller layer, the CLI presenter
handles directly all user interaction via the command-line menu, and delegates to domain logic


## Run the CLI application:
```bash
mvn exec:java -Dexec.args="--cli [argumets]"
```
### Arguments:
* Persistence:
  * `--persistence=in-memory` flag uses an in-memory repository (default).
