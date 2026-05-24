package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotePermissionsSetEventDTOTest {

  @Test
  void getType_shouldReturnNotePermissionsSetEvent() {
    // given
    NotePermissionsSetEventDTO dto =
        new NotePermissionsSetEventDTO() {
          @Override
          public UUID getId() {
            return null;
          }

          @Override
          public PermissionEventPayloadResponse getPayload() {
            return null;
          }
        };
    // when
    String type = dto.getType();
    // then
    assertEquals("NOTE_PERMISSIONS_SET_EVENT", type);
  }
}
