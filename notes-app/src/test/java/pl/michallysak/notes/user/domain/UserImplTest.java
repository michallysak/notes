package pl.michallysak.notes.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.domain.TestCredential;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.validator.UserValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserImplTest {
    
    @Mock
    private UserValidator userValidator;

    @Test
    void constructor_shouldThrowNullPointerException_whenNullUserValidator() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        // when
        Executable executable = () -> new UserImpl(emailPasswordCreateUser, null);
        // then
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    void constructor_shouldSetFieldsCorrectly_whenValidInput() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        // when
        UserImpl user = new UserImpl(emailPasswordCreateUser, userValidator);
        // then
        assertNotNull(user.getId());
        assertEquals(emailPasswordCreateUser.email(), user.getEmail());
    }

    @Test
    void getCredentials_shouldReturnEmpty_whenNoCredentials() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        UserImpl user = new UserImpl(emailPasswordCreateUser, userValidator);
        // when
        List<TestCredential> creds = user.getCredentials(TestCredential.class);
        // then
        assertTrue(creds.isEmpty());
    }

    @Test
    void addCredential_shouldWorkCorrectly() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        UserImpl user = new UserImpl(emailPasswordCreateUser, userValidator);
        TestCredential credential = new TestCredential();
        // when
        user.addCredential(credential);
        List<TestCredential> creds = user.getCredentials(TestCredential.class);
        // then
        assertEquals(1, creds.size());
        assertEquals(credential, creds.getFirst());
    }

    @Test
    void deleteCredentials_shouldRemoveMatchingCredentials() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        UserImpl user = new UserImpl(emailPasswordCreateUser, userValidator);
        TestCredential credential1 = new TestCredential();
        TestCredential credential2 = new TestCredential();
        user.addCredential(credential1);
        user.addCredential(credential2);
        // when
        user.deleteCredentials(TestCredential.class);
        // then
        List<TestCredential> creds = user.getCredentials(TestCredential.class);
        assertTrue(creds.isEmpty());
    }

    @Test
    void getLatestCredential_shouldReturnLatest() throws InterruptedException {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        UserImpl user = new UserImpl(emailPasswordCreateUser, userValidator);
        TestCredential cred1 = new TestCredential();
        Thread.sleep(10);
        TestCredential cred2 = new TestCredential();
        user.addCredential(cred1);
        user.addCredential(cred2);
        // when
        Optional<TestCredential> latest = user.getLatestCredential(TestCredential.class);
        // then
        assertTrue(latest.isPresent());
        assertEquals(cred2, latest.get());
    }

}

