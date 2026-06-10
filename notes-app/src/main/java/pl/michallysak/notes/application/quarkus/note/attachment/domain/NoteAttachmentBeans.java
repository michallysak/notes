package pl.michallysak.notes.application.quarkus.note.attachment.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.attachment.repository.InMemoryNoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.InMemoryNoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentService;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentServiceImpl;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidatorImpl;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteAttachmentBeans {
  @Produces
  @ApplicationScoped
  public NoteAttachmentValidator noteAttachmentValidator() {
    return new NoteAttachmentValidatorImpl();
  }

  @Produces
  @ApplicationScoped
  public NoteAttachmentMetaRepository noteAttachmentMetaRepository() {
    return new InMemoryNoteAttachmentMetaRepository();
  }

  @Produces
  @ApplicationScoped
  public NoteAttachmentContentRepository noteAttachmentContentRepository() {
    return new InMemoryNoteAttachmentContentRepository();
  }

  @Produces
  @ApplicationScoped
  public NoteAttachmentService noteAttachmentService(
      NoteAttachmentMetaRepository metaRepository,
      NoteAttachmentContentRepository contentRepository,
      NoteAttachmentValidator validator) {
    return new NoteAttachmentServiceImpl(metaRepository, contentRepository, validator);
  }
}
