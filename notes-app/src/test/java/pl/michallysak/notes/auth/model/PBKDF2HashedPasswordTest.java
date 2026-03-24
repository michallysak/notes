package pl.michallysak.notes.auth.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PBKDF2HashedPasswordTest {
    @Test
    void constructor_and_getters_shouldWork() {
        // given
        byte[] hash = {1,2,3};
        byte[] salt = {4,5,6};
        int iterations = 1000;
        // when
        PBKDF2HashedPassword hp = new PBKDF2HashedPassword(hash, salt, iterations);
        // then
        assertArrayEquals(hash, hp.getHash());
        assertArrayEquals(salt, hp.getSalt());
        assertEquals(iterations, hp.getIterations());
    }

    @Test
    void equals_and_hashCode_shouldWork() {
        // given
        byte[] hash = {1,2,3};
        byte[] salt = {4,5,6};
        int iterations = 1000;
        // when
        PBKDF2HashedPassword hp1 = new PBKDF2HashedPassword(hash, salt, iterations);
        PBKDF2HashedPassword hp2 = new PBKDF2HashedPassword(hash, salt, iterations);
        // then
        assertEquals(hp1, hp2);
        assertEquals(hp1.hashCode(), hp2.hashCode());
    }
}
