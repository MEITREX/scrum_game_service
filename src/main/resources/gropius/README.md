The "gropius" file contains the Gropius GraphQl schema and is generated by the gradle task:

```sh
./gradlew :refreshGropiusSchema
```

Do not edit this file manually.

It has not the file ending ".graphqls" to prevent it from being loaded by the Spring Boot application and to prevent
IntelliJ from using it for syntax highlighting.