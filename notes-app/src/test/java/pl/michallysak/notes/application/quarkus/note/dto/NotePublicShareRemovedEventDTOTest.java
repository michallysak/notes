package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotePublicShareRemovedEventDTOTest {

  @Test
  void getType_shouldReturnNotePublicShareRemovedEvent() {
    // given
    NotePublicShareRemovedEventDTO dto =
        new NotePublicShareRemovedEventDTO() {
          @Override
          public UUID getId() {
            return null;
          }

          @Override
          public PublicShareRemovedEventPayloadResponse getPayload() {
            return null;
          }
        };
    // when
    String type = dto.getType();
    // then
    assertEquals("NOTE_PUBLIC_SHARE_REMOVED_EVENT", type);
  }
}
