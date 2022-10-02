package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.User;

public interface CryptoUserStorage {
    User getUser(String username);

    void add(String username, String password) throws UserAlreadyExistsException;

    void update(User user);
}
