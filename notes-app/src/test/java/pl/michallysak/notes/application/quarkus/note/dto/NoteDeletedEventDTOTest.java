package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoteDeletedEventDTOTest {

  @Test
  void getType_shouldReturnNoteDeletedEvent() {
    // given
    NoteDeletedEventDTO dto =
        new NoteDeletedEventDTO() {
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
    assertEquals("NOTE_DELETED_EVENT", type);
  }
}
