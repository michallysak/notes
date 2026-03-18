package pl.michallysak.notes.application.quarkus.note.domain;

import io.quarkus.runtime.configuration.ConfigurationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteBeans {

    private static final UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final Logger logger;

    @Produces
    @Dependent
    @Named("authorId")
    public UUID authorId() {
        return AUTHOR_ID;
    }

    @Produces
    @ApplicationScoped
    public NoteRepository noteRepository() {
        Optional<String> persistenceOptional = ConfigProvider.getConfig()
                .getOptionalValue("persistence", String.class);

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
    public NoteService noteService(NoteRepository noteRepository) {
        return new NoteServiceImpl(noteRepository);
    }

}
