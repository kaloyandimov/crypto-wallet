package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;

public interface CryptoUserStorage {
    CryptoUser get(String username);

    void add(CryptoUser user) throws UserAlreadyExistsException;

    void update(CryptoUser user);
}
