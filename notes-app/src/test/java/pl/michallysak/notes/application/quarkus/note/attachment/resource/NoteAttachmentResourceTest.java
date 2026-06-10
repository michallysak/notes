package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.AttachmentContent;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.NoteAttachmentController;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.common.exception.ValidationException;

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
  @SuppressWarnings("resource")
  void getAttachmentOrContent_shouldThrow_whenAcceptNull() {
    // given
    UUID id = UUID.randomUUID();
    // when
    Executable executable = () -> resource.routeAttachmentOrContent(id, null);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  @SuppressWarnings("resource")
  void getAttachmentOrContent_shouldReturnBinary_whenAcceptsOctetStream() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentContent content = mock(AttachmentContent.class);
    when(controller.downloadAttachmentContent(id)).thenReturn(content);
    // when
    resource.routeAttachmentOrContent(id, MediaType.APPLICATION_OCTET_STREAM);
    // then
    verify(controller).downloadAttachmentContent(id);
  }

  @Test
  void getAttachmentOrContent_public_shouldDelegate_whenAcceptsOctetStream() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentContent content = mock(AttachmentContent.class);
    when(controller.downloadAttachmentContent(id)).thenReturn(content);
    // when
    resource.getAttachmentOrContent(id, MediaType.APPLICATION_OCTET_STREAM);
    // then
    verify(controller).downloadAttachmentContent(id);
  }

  @Test
  @SuppressWarnings("resource")
  void getAttachmentOrContent_shouldReturnJson_whenAcceptsJson() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentResponse meta = mock(AttachmentResponse.class);
    when(controller.getAttachment(id)).thenReturn(meta);
    // when
    resource.routeAttachmentOrContent(id, MediaType.APPLICATION_JSON);
    // then
    verify(controller).getAttachment(id);
  }

  @Test
  void getAttachmentOrContent_public_shouldDelegate_whenAcceptsJson() {
    // given
    UUID id = UUID.randomUUID();
    AttachmentResponse meta = mock(AttachmentResponse.class);
    when(controller.getAttachment(id)).thenReturn(meta);
    // when
    resource.getAttachmentOrContent(id, MediaType.APPLICATION_JSON);
    // then
    verify(controller).getAttachment(id);
  }

  @Test
  @SuppressWarnings("resource")
  void getAttachmentOrContent_shouldThrow_whenUnsupportedAccept() {
    // given
    UUID id = UUID.randomUUID();
    String accept = "text/plain";
    // when
    Executable executable = () -> resource.routeAttachmentOrContent(id, accept);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void getAttachmentsForNote_shouldDelegate() {
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
