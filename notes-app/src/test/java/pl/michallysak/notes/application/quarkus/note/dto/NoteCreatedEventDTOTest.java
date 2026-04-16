package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class NoteCreatedEventDTOTest {

  @Test
  void getType_shouldReturnNoteCreatedEvent() {
    // given
    NoteCreatedEventDTO dto =
        new NoteCreatedEventDTO() {
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
    assertEquals("NOTE_CREATED_EVENT", type);
  }
}

