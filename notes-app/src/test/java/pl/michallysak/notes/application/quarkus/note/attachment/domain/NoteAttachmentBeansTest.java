package pl.michallysak.notes.application.quarkus.note.attachment.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.attachment.repository.InMemoryNoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.InMemoryNoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentService;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentServiceImpl;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidatorImpl;
import pl.michallysak.notes.note.service.NoteService;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentBeansTest {

  @InjectMocks NoteAttachmentBeans beans;

  @Test
  void noteAttachmentValidator_shouldReturnImpl() {
    // when
    NoteAttachmentValidator noteAttachmentValidator = beans.noteAttachmentValidator();
    // then
    assertInstanceOf(NoteAttachmentValidatorImpl.class, noteAttachmentValidator);
  }

  @Test
  void noteAttachmentMetaRepository_shouldReturnInMemory() {
    // when
    NoteAttachmentMetaRepository noteAttachmentMetaRepository =
        beans.noteAttachmentMetaRepository();
    // then
    assertInstanceOf(InMemoryNoteAttachmentMetaRepository.class, noteAttachmentMetaRepository);
  }

  @Test
  void noteAttachmentContentRepository_shouldReturnInMemory() {
    // when
    NoteAttachmentContentRepository noteAttachmentContentRepository =
        beans.noteAttachmentContentRepository();
    // then
    assertInstanceOf(
        InMemoryNoteAttachmentContentRepository.class, noteAttachmentContentRepository);
  }

  @Test
  void noteAttachmentService_shouldReturnServiceImpl() {
    // given
    NoteAttachmentMetaRepository noteAttachmentMetaRepository =
        mock(NoteAttachmentMetaRepository.class);
    NoteAttachmentContentRepository noteAttachmentContentRepository =
        mock(NoteAttachmentContentRepository.class);
    NoteAttachmentValidator noteAttachmentValidator = mock(NoteAttachmentValidator.class);
    NoteService noteService = mock(NoteService.class);
    // when
    NoteAttachmentService noteAttachmentService =
        beans.noteAttachmentService(
            noteAttachmentMetaRepository,
            noteAttachmentContentRepository,
            noteAttachmentValidator,
            noteService);
    // then
    assertInstanceOf(NoteAttachmentServiceImpl.class, noteAttachmentService);
  }
}
