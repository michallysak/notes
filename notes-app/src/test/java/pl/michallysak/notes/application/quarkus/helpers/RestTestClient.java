package pl.michallysak.notes.application.quarkus.helpers;

import java.util.HashMap;
import java.util.Map;

public class RestTestClient {

  protected final String basePath;
  protected final Map<String, String> authorizationHeaders;

  public RestTestClient(String basePath, String authToken) {
    this.basePath = basePath;
    authorizationHeaders = new HashMap<>();
    if (authToken != null) {
      authorizationHeaders.put("Authorization", "Bearer " + authToken);
    }
  }
}
