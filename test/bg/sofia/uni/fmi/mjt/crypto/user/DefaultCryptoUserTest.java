package bg.sofia.uni.fmi.mjt.crypto.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoUserTest {
    @Test
    void testOfNewUser() {
        String userString = "username;password;0.0;;";
        CryptoUser user1 = DefaultCryptoUser.of(userString);
        CryptoUser user2 = new DefaultCryptoUser("username", "PASSWORD");

        assertEquals(userString, user1.toString(), "User string representations should match");
        assertEquals("username", user1.getUsername(), "Usernames should match");
        assertEquals("password", user1.getPassword(), "Passwords should match");
        assertEquals("0.0;;", user1.getWallet().toString(), "Wallet string representations should match");
        assertEquals(user1, user2, "Users should match");
    }
}
