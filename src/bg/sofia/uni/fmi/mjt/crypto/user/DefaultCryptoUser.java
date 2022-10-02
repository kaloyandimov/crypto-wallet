package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.CryptoWallet;
import bg.sofia.uni.fmi.mjt.crypto.wallet.DefaultCryptoWallet;

import java.util.Objects;

public class DefaultCryptoUser implements CryptoUser {
    private static final String DELIMITER_FIELD = ";";

    private static final int USERNAME = 0;
    private static final int PASSWORD = 1;
    private static final int WALLET = 2;
    private static final int NUMBER_OF_FIELDS = 3;

    private final String username;
    private final String password;
    private final CryptoWallet wallet;

    public DefaultCryptoUser(String username, String password) {
        this(username, password, new DefaultCryptoWallet());
    }

    public DefaultCryptoUser(String username, String password, CryptoWallet wallet) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
    }

    public static CryptoUser of(String line) {
        String[] tokens = line.split(DELIMITER_FIELD, NUMBER_OF_FIELDS);

        String username = tokens[USERNAME];
        String password = tokens[PASSWORD];
        CryptoWallet wallet = DefaultCryptoWallet.of(tokens[WALLET]);

        return new DefaultCryptoUser(username, password, wallet);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public CryptoWallet getWallet() {
        return wallet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultCryptoUser that = (DefaultCryptoUser) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + DELIMITER_FIELD + password + DELIMITER_FIELD + wallet;
    }
}
