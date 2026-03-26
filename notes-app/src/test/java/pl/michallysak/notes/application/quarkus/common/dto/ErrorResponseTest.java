package pl.michallysak.notes.application.quarkus.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {
  @Test
  void constructor_shouldCreateMessage() {
    // given
    String expectedMessage = "error";
    // when
    ErrorResponse response = new ErrorResponse(expectedMessage);
    // then
    assertEquals(expectedMessage, response.getMessage());
  }
}
