package pl.michallysak.notes.application.quarkus.note.domain;

import io.quarkus.runtime.configuration.ConfigurationException;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteBeansTest {

    private static final String PERSISTENCE = "persistence";

    @Mock
    Logger logger;

    @InjectMocks
    NoteBeans noteBeans;

    @Test
    void currentUserProvider_shouldReturnInstanceOfNoAuthCurrentUserProvider() {
        // when
        CurrentUserProvider currentUserProvider = noteBeans.currentUserProvider();
        // then
        assertInstanceOf(NoAuthCurrentUserProvider.class, currentUserProvider);
    }

    @Test
    void noteRepository_shouldReturnInMemoryNoteRepository_whenGetPersistenceReturnEmptyString() {
        // when
        NoteRepository noteRepository = noteBeans.noteRepository();
        // then
        assertNotNull(noteRepository);
        assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
    }

    @Test
    void noteRepository_shouldReturnInMemoryNoteRepository_whenConfigInMemory() {
        withMockedConfigProvider((config) -> {
            // given
            mockPersistenceConfig(config, "in-memory");
            // when
            NoteRepository noteRepository = noteBeans.noteRepository();
            // then
            assertNotNull(noteRepository);
            assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
        });
    }

    @Test
    void noteRepository_shouldReturnInMemoryNoteRepository_whenNoConfig() {
        withMockedConfigProvider((config) -> {
            // given
            mockPersistenceConfig(config);
            // when
            NoteRepository noteRepository = noteBeans.noteRepository();
            // then
            assertNotNull(noteRepository);
            assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
        });
    }

    @Test
    void noteRepository_shouldThrow_whenGetPersistenceReturnUnsupportedPersistence() {
        withMockedConfigProvider((config) -> {
            // given
            String value = "xyz";
            mockPersistenceConfig(config, value);
            // when
            Executable executable = () -> noteBeans.noteRepository();
            // then
            ConfigurationException exception = assertThrows(ConfigurationException.class, executable);
            assertEquals("Unsupported persistence type: \"%s\"".formatted(value), exception.getMessage());
        });
    }

    @Test
    void noteService_shouldReturnNoteServiceImpl() {
        // given
        NoteRepository noteRepository = mock(NoteRepository.class);
        // when
        NoteService noteService = noteBeans.noteService(noteRepository);
        // then
        assertNotNull(noteService);
        assertInstanceOf(NoteServiceImpl.class, noteService);
    }

    private static void mockPersistenceConfig(Config config, String value) {
        when(config.getOptionalValue(PERSISTENCE, String.class)).thenReturn(Optional.ofNullable(value));
    }

    private static void mockPersistenceConfig(Config config) {
        when(config.getOptionalValue(PERSISTENCE, String.class)).thenReturn(Optional.empty());
    }

    private static void withMockedConfigProvider(Consumer<Config> consumer) {
        try (MockedStatic<ConfigProvider> configProviderMock = mockStatic(ConfigProvider.class)) {
            Config config = mock(Config.class);
            configProviderMock.when(ConfigProvider::getConfig).thenReturn(config);
            consumer.accept(config);
        }
    }

}
