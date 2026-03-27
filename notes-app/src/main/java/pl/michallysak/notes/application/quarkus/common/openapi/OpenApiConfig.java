package pl.michallysak.notes.application.quarkus.common.openapi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenApiConfig {
  // quarkus.smallrye-openapi.security-scheme-name
  public static final String SECURITY_SCHEME_NAME = "bearer_auth";
}
