package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.DefaultCryptoUser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandValidatorTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final CryptoUser TEST_USER = new DefaultCryptoUser(USERNAME, PASSWORD);

    private final CommandValidator commandValidator = new CommandValidator();

    @Test
    public void testValidateSignUp() {
        Command command = new Command(Command.SIGN_UP, new String[]{"username", "password"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateSignUpWithUserLoggedIn() {
        Command command = new Command(Command.SIGN_UP, new String[]{"username", "password"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log out first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateSignUpWithIncorrectArgumentCount() {
        Command command = new Command(Command.SIGN_UP, new String[]{"username"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("2 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateLogIn() {
        Command command = new Command(Command.LOG_IN, new String[]{"username", "password"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateLogInWithUserLoggedIn() {
        Command command = new Command(Command.LOG_IN, new String[]{"username", "password"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log out first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateLogInWithIncorrectArgumentCount() {
        Command command = new Command(Command.LOG_IN, new String[]{"username"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("2 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateList() {
        Command command = new Command(Command.LIST, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateListWithUserLoggedIn() {
        Command command = new Command(Command.LIST, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateListWithIncorrectArgumentCount() {
        Command command = new Command(Command.LIST, new String[]{"username"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("0 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateDeposit() {
        Command command = new Command(Command.DEPOSIT, new String[]{"100"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateDepositWithUserNotLoggedIn() {
        Command command = new Command(Command.DEPOSIT, new String[]{"100"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateDepositWithIncorrectArgumentCount() {
        Command command = new Command(Command.DEPOSIT, new String[]{"100", "extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("1 argument expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateBuy() {
        Command command = new Command(Command.BUY, new String[]{"BTC", "100"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateBuyWithUserNotLoggedIn() {
        Command command = new Command(Command.BUY, new String[]{"BTC", "100"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateBuyWithIncorrectArgumentCount() {
        Command command = new Command(Command.BUY, new String[]{"BTC"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("2 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateSell() {
        Command command = new Command(Command.SELL, new String[]{"BTC"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateSellWithUserNotLoggedIn() {
        Command command = new Command(Command.SELL, new String[]{"BTC"});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateSellWithIncorrectArgumentCount() {
        Command command = new Command(Command.SELL, new String[]{"BTC", "extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("1 argument expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateSummary() {
        Command command = new Command(Command.SUMMARY, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateSummaryWithUserNotLoggedIn() {
        Command command = new Command(Command.SUMMARY, new String[]{});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateSummaryWithIncorrectArgumentCount() {
        Command command = new Command(Command.SUMMARY, new String[]{"extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("0 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateTrends() {
        Command command = new Command(Command.TRENDS, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateTrendsWithUserNotLoggedIn() {
        Command command = new Command(Command.TRENDS, new String[]{});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateTrendsWithIncorrectArgumentCount() {
        Command command = new Command(Command.TRENDS, new String[]{"extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("0 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateLogOut() {
        Command command = new Command(Command.LOG_OUT, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateLogOutWithUserNotLoggedIn() {
        Command command = new Command(Command.LOG_OUT, new String[]{});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("Log in first", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateLogOutWithIncorrectArgumentCount() {
        Command command = new Command(Command.LOG_OUT, new String[]{"extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("0 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateExit() {
        Command command = new Command(Command.EXIT, new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateExitWithUserNotLoggedIn() {
        Command command = new Command(Command.EXIT, new String[]{});

        Optional<String> validationResult = commandValidator.validate(null, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateExitWithIncorrectArgumentCount() {
        Command command = new Command(Command.EXIT, new String[]{"extraArgument"});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isPresent(), "Command should be invalid");
        assertEquals("0 arguments expected", validationResult.get(), "Messages should match");
    }

    @Test
    public void testValidateUnknownCommand() {
        Command command = new Command("unknown", new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateUnknownCommandWithUserNotLoggedIn() {
        Command command = new Command("unknown", new String[]{});

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Command should be valid");
    }

    @Test
    public void testValidateEmptyCommand() {
        Command command = Command.empty();

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Empty command should be valid");
    }

    @Test
    public void testValidateEmptyCommandWithUserNotLoggedIn() {
        Command command = Command.empty();

        Optional<String> validationResult = commandValidator.validate(TEST_USER, command);

        assertTrue(validationResult.isEmpty(), "Empty command should be valid");
    }
}
