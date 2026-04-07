package pl.michallysak.notes.application.quarkus.note.domain;

import io.quarkus.runtime.configuration.ConfigurationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteBeans {
  private final Logger logger;

  @Produces
  @ApplicationScoped
  public NoteRepository noteRepository() {
    Optional<String> persistenceOptional =
        ConfigProvider.getConfig().getOptionalValue("persistence", String.class);

    if (persistenceOptional.isEmpty()) {
      logger.info("Persistence type not provided, use in-memory");
      return new InMemoryNoteRepository();
    }

    String persistence = persistenceOptional.get();
    if (persistence.contains("in-memory")) {
      logger.info("Using persistence type in-memory");
      return new InMemoryNoteRepository();
    }

    throw new ConfigurationException("Unsupported persistence type: \"%s\"".formatted(persistence));
  }

  @Produces
  @ApplicationScoped
  public NoteService noteService(
      NoteRepository noteRepository, DomainEventPublisher eventPublisher) {
    return new NoteServiceImpl(noteRepository, eventPublisher);
  }

  @Produces
  @ApplicationScoped
  public DomainEventPublisher domainEventPublisher() {
    // No-op publisher; replace with real implementation if needed
    return events -> {};
  }
}
