package pl.michallysak.notes.application.quarkus.helpers;

import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.resource.NoteResourceRestTestClient;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserDtoRequestUtils;
import pl.michallysak.notes.application.quarkus.user.resource.UserResourceRestTestClient;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.user.repository.UserRepository;

public class BaseIT {

  protected static final String EMAIL_1 = "user1@test.pl";
  protected static final String EMAIL_2 = "user2@test.pl";
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Inject UserRepository userRepository;
  @Inject NoteRepository noteRepository;

  private final UserResourceRestTestClient userResourceTestClient =
      UserResourceRestTestClient.noAuth();

  @BeforeEach
  void setUp() {
    noteRepository.deleteNotes();
    userRepository.deleteUsers();
  }

  protected String createUser(String email) {
    RegisterUserRequest registerUserRequest =
        UserDtoRequestUtils.getRegisterUserRequestBuilder(email).build();
    return userResourceTestClient
        .registerUser(toJsonString(registerUserRequest))
        .then()
        .statusCode(201)
        .extract()
        .path("token");
  }

  protected String createNote(String token, CreateNoteRequest createNoteRequest) {
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    return noteResourceTestClient
        .createNote(toJsonString(createNoteRequest))
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }
}
