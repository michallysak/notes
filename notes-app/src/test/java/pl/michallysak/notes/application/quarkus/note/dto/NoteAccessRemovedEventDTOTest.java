package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoteAccessRemovedEventDTOTest {

  @Test
  void getType_shouldReturnNoteAccessRemovedEvent() {
    // given
    NoteAccessRemovedEventDTO dto =
        new NoteAccessRemovedEventDTO() {
          @Override
          public UUID getId() {
            return null;
          }

          @Override
          public AccessRemovedEventPayloadResponse getPayload() {
            return null;
          }
        };
    // when
    String type = dto.getType();
    // then
    assertEquals("NOTE_ACCESS_REMOVED_EVENT", type);
  }
}
