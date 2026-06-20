package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.AttachmentContent;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.NoteAttachmentController;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentResourceTest {

  @Mock NoteAttachmentController controller;
  @InjectMocks NoteAttachmentResource resource;

  @Test
  void createAttachment_shouldDelegateToController() {
    // given
    CreateAttachmentMultipartForm form = mock(CreateAttachmentMultipartForm.class);
    AttachmentResponse expected = mock(AttachmentResponse.class);
    when(controller.createAttachment(form)).thenReturn(expected);
    // when
    resource.createAttachment(form);
    // then
    verify(controller).createAttachment(form);
  }

  @Test
  void getAttachmentContent_public_shouldDelegate() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentContent content = mock(AttachmentContent.class);
    when(controller.downloadAttachmentContent(id)).thenReturn(content);
    // when
    resource.getAttachmentContent(id);
    // then
    verify(controller).downloadAttachmentContent(id);
  }

  @Test
  void getAttachmentMetadata_public_shouldDelegate() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentResponse meta = mock(AttachmentResponse.class);
    when(controller.getAttachment(id)).thenReturn(meta);
    // when
    resource.getAttachmentMetadata(id);
    // then
    verify(controller).getAttachment(id);
  }

  @Test
  void getAttachmentsForNote_shouldDelegateMetadata() {
    // given
    UUID noteId = UUID.randomUUID();
    AttachmentResponse attachmentResponse =
        AttachmentResponse.builder().id(UUID.randomUUID()).build();
    when(controller.getAttachmentsForNote(noteId)).thenReturn(List.of(attachmentResponse));
    // when
    resource.getAttachmentsForNote(noteId);
    // then
    verify(controller).getAttachmentsForNote(noteId);
  }

  @Test
  void deleteAttachment_shouldDelegate() {
    // given
    UUID id = UUID.randomUUID();
    doNothing().when(controller).deleteAttachment(id);
    // when
    resource.deleteAttachment(id);
    // then
    verify(controller).deleteAttachment(id);
  }
}
