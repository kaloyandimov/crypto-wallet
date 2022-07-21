package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.Wallet;

import java.util.Objects;

public class User {
    private static final String DELIMITER_FIELD = ";";

    private static final int USERNAME = 0;
    private static final int PASSWORD = 1;
    private static final int WALLET = 2;
    private static final int NUMBER_OF_FIELDS = 3;

    private final String username;
    private final String password;
    private final Wallet wallet;

    public User(String username, String password) {
        this(username, password, new Wallet());
    }

    public User(String username, String password, Wallet wallet) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
    }

    public static User of(String line) {
        String[] tokens = line.split(DELIMITER_FIELD, NUMBER_OF_FIELDS);

        String username = tokens[USERNAME];
        String password = tokens[PASSWORD];
        Wallet wallet = Wallet.of(tokens[WALLET]);

        return new User(username, password, wallet);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Wallet getWallet() {
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

        User that = (User) o;
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
