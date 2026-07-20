package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotePublicShareUpsertedEventDTOTest {

  @Test
  void getType_shouldReturnNotePublicShareUpsertedEvent() {
    // given
    NotePublicShareUpsertedEventDTO dto =
        new NotePublicShareUpsertedEventDTO() {
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
    assertEquals("NOTE_PUBLIC_SHARE_UPSERTED_EVENT", type);
  }
}
