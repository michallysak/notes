package pl.michallysak.notes.auth.domain;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;

import static org.junit.jupiter.api.Assertions.*;

class PBKDF2PasswordCredentialTest {
    @Test
    void constructor_and_getters_shouldWork() {
        // given
        PBKDF2HashedPassword hashed = getPbkdf2HashedPassword();
        // when
        PBKDF2PasswordCredential credential = new PBKDF2PasswordCredential(hashed);
        // then
        assertNotNull(credential.getId());
        assertNotNull(credential.getCreatedAt());
        assertEquals(hashed, credential.getHashedPassword());
    }

    @Test
    void equals_and_hashCode_shouldWork() {
        // given
        PBKDF2HashedPassword hashed = getPbkdf2HashedPassword();
        // when
        PBKDF2PasswordCredential cred1 = new PBKDF2PasswordCredential(hashed);
        PBKDF2PasswordCredential cred2 = new PBKDF2PasswordCredential(hashed);
        // then
        assertNotEquals(cred1, cred2);
        assertNotEquals(cred1.hashCode(), cred2.hashCode());
    }

    private PBKDF2HashedPassword getPbkdf2HashedPassword() {
        byte[] hash = {1,2,3};
        byte[] salt = {4,5,6};
        int iterations = 1000;
        return new PBKDF2HashedPassword(hash, salt, iterations);
    }
}
