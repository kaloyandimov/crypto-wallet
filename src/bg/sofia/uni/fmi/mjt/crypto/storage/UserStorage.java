package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.User;

public interface UserStorage {
    User getUser(String username);

    void add(User user) throws UserAlreadyExistsException;

    void update(User user);
}
