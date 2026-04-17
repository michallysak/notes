package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoteUpdatedEventDTOTest {

  @Test
  void getType_shouldReturnNoteUpdatedEvent() {
    // given
    NoteUpdatedEventDTO dto =
        new NoteUpdatedEventDTO() {
          @Override
          public UUID getId() {
            return null;
          }

          @Override
          public NoteResponse getPayload() {
            return null;
          }
        };
    // when
    String type = dto.getType();
    // then
    assertEquals("NOTE_UPDATED_EVENT", type);
  }
}
