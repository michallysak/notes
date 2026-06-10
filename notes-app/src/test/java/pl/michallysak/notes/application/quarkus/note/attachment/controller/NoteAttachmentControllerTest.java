package pl.michallysak.notes.application.quarkus.note.attachment.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.application.quarkus.note.attachment.mapper.AttachmentMapper;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentService;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentControllerTest {

  private static final UUID USER_ID = UUID.randomUUID();

  @Mock NoteAttachmentService noteAttachmentService;
  @Mock CurrentUserProvider currentUserProvider;
  @Mock AttachmentMapper attachmentMapper;
  @InjectMocks NoteAttachmentController controller;

  @Test
  void createAttachment_shouldReturnMappedResponse_whenContentNull() {
    // given
    CreateAttachmentMultipartForm form = mock(CreateAttachmentMultipartForm.class);
    when(form.getFile()).thenReturn(null);
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    CreateNoteAttachmentMeta createMeta = buildCreateMeta(null, 0L);
    when(attachmentMapper.mapToCreateMeta(eq(form), eq(USER_ID))).thenReturn(createMeta);
    NoteAttachmentMetaValue meta = buildMeta(UUID.randomUUID(), null, 0L);
    when(noteAttachmentService.createAttachmentMeta(any())).thenReturn(meta);
    // when
    Executable executable = () -> controller.createAttachment(form);
    // then
    assertThrows(IllegalArgumentException.class, executable);
    verify(noteAttachmentService).createAttachmentMeta(any());
    verify(noteAttachmentService, never()).uploadAttachmentContent(any(), any(), any());
  }

  @Test
  void createAttachment_shouldUploadContentAndRefreshMeta_whenContentPresent() {
    // given
    CreateAttachmentMultipartForm form = mock(CreateAttachmentMultipartForm.class);
    byte[] content = "data".getBytes();
    when(form.getFile()).thenReturn(content);
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    CreateNoteAttachmentMeta createMeta = buildCreateMeta(null, content.length);
    when(attachmentMapper.mapToCreateMeta(eq(form), eq(USER_ID))).thenReturn(createMeta);
    UUID metaId = UUID.randomUUID();
    NoteAttachmentMetaValue meta = buildMeta(metaId, null, content.length);
    AttachmentResponse response = mock(AttachmentResponse.class);
    when(noteAttachmentService.createAttachmentMeta(any())).thenReturn(meta);
    doNothing().when(noteAttachmentService).uploadAttachmentContent(eq(metaId), eq(USER_ID), any());
    when(attachmentMapper.mapToResponse(meta)).thenReturn(response);
    // when
    AttachmentResponse result = controller.createAttachment(form);
    // then
    assertEquals(response, result);
    verify(noteAttachmentService).uploadAttachmentContent(eq(metaId), eq(USER_ID), any());
  }

  @Test
  void getAttachment_shouldReturnMappedResponse() {
    // given
    UUID id = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    NoteAttachmentMetaValue meta =
        NoteAttachmentMetaValue.builder().id(id).authorId(USER_ID).build();
    AttachmentResponse response = mock(AttachmentResponse.class);
    when(noteAttachmentService.getAttachmentMeta(id, USER_ID)).thenReturn(Optional.of(meta));
    when(attachmentMapper.mapToResponse(meta)).thenReturn(response);
    // when
    AttachmentResponse result = controller.getAttachment(id);
    // then
    assertEquals(response, result);
  }

  @Test
  void getAttachmentsForNote_shouldReturnMappedList() {
    // given
    UUID noteId = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    NoteAttachmentMetaValue meta =
        NoteAttachmentMetaValue.builder().id(UUID.randomUUID()).authorId(USER_ID).build();
    AttachmentResponse response = mock(AttachmentResponse.class);
    when(noteAttachmentService.getAttachmentMetasForNote(noteId, USER_ID))
        .thenReturn(List.of(meta));
    when(attachmentMapper.mapToResponse(meta)).thenReturn(response);
    // when
    List<AttachmentResponse> result = controller.getAttachmentsForNote(noteId);
    // then
    assertEquals(1, result.size());
    assertEquals(response, result.getFirst());
  }

  @Test
  void deleteAttachment_shouldDelegate() {
    // given
    UUID id = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    // when
    controller.deleteAttachment(id);
    // then
    verify(noteAttachmentService).deleteAttachmentMeta(id, USER_ID);
  }

  @Test
  void downloadAttachmentContent_shouldReturnContentAndMeta() {
    // given
    UUID id = UUID.randomUUID();
    byte[] content = "hello".getBytes();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    NoteAttachmentMetaValue meta = buildMetaForDownload(id);
    when(noteAttachmentService.getAttachmentMeta(id, USER_ID)).thenReturn(Optional.of(meta));
    when(noteAttachmentService.downloadAttachmentContent(id, USER_ID))
        .thenReturn(NoteAttachmentContentValue.of(content));
    // when
    AttachmentContent result = controller.downloadAttachmentContent(id);
    // then
    assertArrayEquals(content, result.value());
    assertEquals("text/plain", result.contentType());
    assertEquals("file.txt", result.fileName());
  }

  @Test
  void createAttachment_shouldParseNoteIdString_andPassToService() {
    // given
    CreateAttachmentMultipartForm form = mock(CreateAttachmentMultipartForm.class);
    UUID expectedNoteId = UUID.randomUUID();
    when(form.getFile()).thenReturn(null);
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);

    CreateNoteAttachmentMeta createMeta = buildCreateMeta(expectedNoteId, 0L);
    when(attachmentMapper.mapToCreateMeta(eq(form), eq(USER_ID))).thenReturn(createMeta);

    NoteAttachmentMetaValue meta = buildMeta(UUID.randomUUID(), expectedNoteId, 0L);
    when(noteAttachmentService.createAttachmentMeta(any())).thenReturn(meta);

    // when
    Executable executable = () -> controller.createAttachment(form);
    // then
    assertThrows(IllegalArgumentException.class, executable);
    verify(noteAttachmentService)
        .createAttachmentMeta(argThat(arg -> expectedNoteId.equals(arg.noteId())));
  }

  @Test
  void getAttachment_shouldThrow_whenNotFound() {
    // given
    UUID id = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    when(noteAttachmentService.getAttachmentMeta(eq(id), eq(USER_ID))).thenReturn(Optional.empty());
    // when
    Executable executable = () -> controller.getAttachment(id);
    // then
    assertThrows(IllegalArgumentException.class, executable);
  }

  @Test
  void downloadAttachmentContent_shouldThrow_whenMetaNotFound() {
    // given
    UUID id = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
    when(noteAttachmentService.getAttachmentMeta(eq(id), eq(USER_ID))).thenReturn(Optional.empty());
    // when
    Executable executable = () -> controller.downloadAttachmentContent(id);
    // then
    assertThrows(IllegalArgumentException.class, executable);
  }

  private CreateNoteAttachmentMeta buildCreateMeta(UUID noteId, long size) {
    return CreateNoteAttachmentMeta.builder()
        .noteId(noteId)
        .authorId(USER_ID)
        .fileName("file.txt")
        .contentType("text/plain")
        .size(size)
        .build();
  }

  private NoteAttachmentMetaValue buildMeta(UUID id, UUID noteId, long size) {
    return NoteAttachmentMetaValue.builder()
        .id(id)
        .noteId(noteId)
        .authorId(USER_ID)
        .fileName("file.txt")
        .contentType("text/plain")
        .size(size)
        .created(null)
        .build();
  }

  private NoteAttachmentMetaValue buildMetaForDownload(UUID id) {
    return NoteAttachmentMetaValue.builder()
        .id(id)
        .authorId(USER_ID)
        .contentType("text/plain")
        .fileName("file.txt")
        .build();
  }
}
