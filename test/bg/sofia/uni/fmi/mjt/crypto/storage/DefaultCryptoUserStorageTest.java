package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.DefaultCryptoUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCryptoUserStorageTest {
    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASSWORD = "password";
    private static final CryptoUser TEST_USER = new DefaultCryptoUser(TEST_USERNAME, TEST_PASSWORD);

    @TempDir
    Path tempDir;

    private CryptoUserStorage userStorage;

    @BeforeEach
    void setUp() {
        Path tempFile = tempDir.resolve("users.txt");
        userStorage = new DefaultCryptoUserStorage(tempFile.toString());
    }

    @Test
    void testGetExistingUser() throws UserAlreadyExistsException {
        userStorage.add(TEST_USER);

        assertNotNull(userStorage.get(TEST_USERNAME), "User should exist");
    }

    @Test
    void testGetNonexistentUser() {
        assertNull(userStorage.get("any"), "User should not exist");
    }

    @Test
    void testAddNewUser() {
        assertDoesNotThrow(() -> userStorage.add(TEST_USER), "New user should be added");

        CryptoUser retrievedUser = userStorage.get(TEST_USERNAME);

        assertEquals(TEST_USERNAME, retrievedUser.getUsername(), "Usernames should match");
        assertEquals(TEST_PASSWORD, retrievedUser.getPassword(), "Passwords should match");
    }

    @Test
    void testAddExistingUser() {
        assertDoesNotThrow(() -> userStorage.add(TEST_USER), "New user should be added");
        assertThrows(UserAlreadyExistsException.class, () -> userStorage.add(TEST_USER),
            "UserAlreadyExistsException expected when username is already taken");
    }

    @Test
    void testUpdateUser() throws NegativeValueException {
        assertDoesNotThrow(() -> userStorage.add(TEST_USER), "New user should be added");

        CryptoUser user = userStorage.get(TEST_USERNAME);
        user.getWallet().deposit(200);

        assertDoesNotThrow(() -> userStorage.update(user), "User should be updated successfully");

        CryptoUser updatedUser = userStorage.get(TEST_USERNAME);

        assertEquals(user, updatedUser, "References should be the same");
        assertEquals(200.0, updatedUser.getWallet().getBalance(), 0.00001,
            "Money should be deposited successfully");
    }

    @Test
    void testLoadPersistedData() throws IOException {
        Path tempFile = tempDir.resolve("users.txt");

        Files.writeString(tempFile, TEST_USER.toString());

        userStorage = new DefaultCryptoUserStorage(tempFile.toString());

        CryptoUser user = userStorage.get(TEST_USERNAME);

        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_PASSWORD, user.getPassword());
    }
}
