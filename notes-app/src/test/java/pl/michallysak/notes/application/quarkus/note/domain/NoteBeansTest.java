package pl.michallysak.notes.application.quarkus.note.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.runtime.configuration.ConfigurationException;
import jakarta.enterprise.inject.Instance;
import java.util.Optional;
import java.util.function.Consumer;
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
import pl.michallysak.notes.application.quarkus.note.persistence.PanacheNoteRepository;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.note.validator.NoteValidatorImpl;

@ExtendWith(MockitoExtension.class)
class NoteBeansTest {

  private static final String PERSISTENCE = "persistence";

  @Mock Logger logger;

  @Mock PanacheNoteRepository panacheNoteRepository;
  @Mock Instance<PanacheNoteRepository> panacheNoteRepositoryInstance;
  @Mock NoteValidator noteValidator;

  @InjectMocks NoteBeans noteBeans;

  @Test
  void noteRepository_shouldReturnInMemoryNoteRepository_whenGetPersistenceReturnEmptyString() {
    // when
    NoteRepository noteRepository = noteBeans.noteRepository(panacheNoteRepositoryInstance);
    // then
    assertNotNull(noteRepository);
    assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
    verifyNoInteractions(panacheNoteRepositoryInstance);
  }

  @Test
  void noteRepository_shouldReturnInMemoryNoteRepository_whenConfigInMemory() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config, "in-memory");
          // when
          NoteRepository noteRepository = noteBeans.noteRepository(panacheNoteRepositoryInstance);
          // then
          assertNotNull(noteRepository);
          assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
          verifyNoInteractions(panacheNoteRepositoryInstance);
        });
  }

  @Test
  void noteRepository_shouldReturnInMemoryNoteRepository_whenNoConfig() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config);
          // when
          NoteRepository noteRepository = noteBeans.noteRepository(panacheNoteRepositoryInstance);
          // then
          assertNotNull(noteRepository);
          assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
          verifyNoInteractions(panacheNoteRepositoryInstance);
        });
  }

  @Test
  void noteRepository_shouldThrow_whenGetPersistenceReturnUnsupportedPersistence() {
    withMockedConfigProvider(
        (config) -> {
          // given
          String value = "xyz";
          mockPersistenceConfig(config, value);
          // when
          Executable executable = () -> noteBeans.noteRepository(panacheNoteRepositoryInstance);
          // then
          ConfigurationException exception = assertThrows(ConfigurationException.class, executable);
          assertEquals(
              "Unsupported persistence type: \"%s\"".formatted(value), exception.getMessage());
          verifyNoInteractions(panacheNoteRepositoryInstance);
        });
  }

  @Test
  void noteService_shouldReturnNoteServiceImpl() {
    // given
    NoteRepository noteRepository = mock(NoteRepository.class);
    DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);
    // when
    NoteService noteService = noteBeans.noteService(noteRepository, eventPublisher, noteValidator);
    // then
    assertNotNull(noteService);
    assertInstanceOf(NoteServiceImpl.class, noteService);
  }

  @Test
  void noteValidator_shouldReturnNoteValidatorImpl() {
    // when
    NoteValidator noteValidator = noteBeans.noteValidator();
    // then
    assertNotNull(noteValidator);
    assertInstanceOf(NoteValidatorImpl.class, noteValidator);
  }

  @Test
  void noteRepository_shouldReturnPanacheNoteRepository_whenConfigPanache() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config, "sql");
          when(panacheNoteRepositoryInstance.get()).thenReturn(panacheNoteRepository);
          // when
          NoteRepository noteRepository = noteBeans.noteRepository(panacheNoteRepositoryInstance);
          // then
          assertNotNull(noteRepository);
          assertSame(panacheNoteRepository, noteRepository);
          verify(panacheNoteRepositoryInstance).get();
        });
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
