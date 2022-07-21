package bg.sofia.uni.fmi.mjt.crypto.exception;

public class UserAlreadyExistsException extends UserStorageException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
