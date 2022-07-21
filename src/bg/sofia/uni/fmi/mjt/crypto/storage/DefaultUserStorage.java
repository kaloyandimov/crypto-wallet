package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DefaultUserStorage implements UserStorage {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String DELIMITER_FIELD = ";";

    private static final Path TEMPORARY_FILE_PATH = Path.of("temporary-user-database.csv");

    private final Path filePath;

    public DefaultUserStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public User getUser(String username) {
        User user = null;

        try (var bufferedReader = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            while (user == null && (line = bufferedReader.readLine()) != null) {
                String currentUsername = line.substring(0, line.indexOf(DELIMITER_FIELD));

                if (currentUsername.equals(username)) {
                    user = User.of(line);
                }
            }

        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Could not read file", exception);
        } catch (IOException exception) {
            throw new RuntimeException("Could not retrieve user from storage", exception);
        }

        return user;
    }

    public void add(User user) throws UserAlreadyExistsException {
        if (isUsernameTaken(user.getUsername())) {
            throw new UserAlreadyExistsException("Username is taken");
        }

        try (var bufferedWriter = new BufferedWriter(new FileWriter(filePath.toString(), true))) {
            bufferedWriter.write(user + LINE_SEPARATOR);
        } catch (IOException exception) {
            throw new RuntimeException("Could not write user to file", exception);
        }
    }

    public void update(User user) {
        try (var bufferedReader = new BufferedReader(new FileReader(filePath.toString()));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_PATH.toString()))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String currentUsername = line.substring(0, line.indexOf(DELIMITER_FIELD));

                if (currentUsername.equals(user.getUsername())) {
                    bufferedWriter.write(user + LINE_SEPARATOR);
                } else {
                    bufferedWriter.write(line + LINE_SEPARATOR);
                }
            }

            Files.move(TEMPORARY_FILE_PATH, filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Could not read file", exception);
        } catch (IOException exception) {
            throw new RuntimeException("Could not update user", exception);
        }
    }

    private boolean isUsernameTaken(String username) {
        return getUser(username) != null;
    }
}
