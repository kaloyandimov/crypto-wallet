package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.DefaultCryptoUser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultCryptoUserStorage implements CryptoUserStorage {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final Map<String, CryptoUser> users;
    private final String filePath;

    public DefaultCryptoUserStorage(String filePath) {
        this.users = new HashMap<>();
        this.filePath = filePath;

        load();
    }

    @Override
    public CryptoUser get(String username) {
        return users.get(username);
    }

    @Override
    public void add(CryptoUser user) throws UserAlreadyExistsException {
        if (users.containsKey(user.getUsername())) {
            throw new UserAlreadyExistsException("Username is taken");
        }

        users.put(user.getUsername(), user);
    }

    @Override
    public void update(CryptoUser user) {
        users.replace(user.getUsername(), user);

        persist();
    }

    private void persist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (CryptoUser user : users.values()) {
                writer.write(user + LINE_SEPARATOR);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not persist users", exception);
        }
    }

    private void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                CryptoUser user = DefaultCryptoUser.of(line);

                users.put(user.getUsername(), user);
            }
        } catch (FileNotFoundException exception) {
            //
        } catch (IOException exception) {
            throw new RuntimeException("Could not retrieve users", exception);
        }
    }
}
