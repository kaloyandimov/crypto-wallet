package bg.sofia.uni.fmi.mjt.crypto.exception;

public class TooManyRequestsException extends AssetServiceException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
