package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.exception.WalletException;
import bg.sofia.uni.fmi.mjt.crypto.storage.AssetStorage;
import bg.sofia.uni.fmi.mjt.crypto.storage.CryptoUserStorage;
import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.DefaultCryptoUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandExecutorTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static Asset bitcoin;
    private static Asset ethereum;

    private CryptoUser user;

    @Mock
    private CryptoUserStorage userStorageMock;

    @Mock
    private AssetStorage assetStorageMock;

    @InjectMocks
    private CommandExecutor commandExecutor;

    @BeforeAll
    static void beforeAll() {
        bitcoin = new Asset("BTC", "Bitcoin", true, 19424.470311714055519056008384);
        ethereum = new Asset("ETH", "Ethereum", true, 1039.3682961935640591450912866);
    }

    @BeforeEach
    void setUp() {
        user = new DefaultCryptoUser(USERNAME, BCrypt.hashpw(PASSWORD, BCrypt.gensalt()));
    }

    @Test
    public void testSignUpWhenLoggedOut() throws UserAlreadyExistsException {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.SIGN_UP, new String[]{USERNAME, PASSWORD});
        String result = commandExecutor.execute(null, command);

        assertEquals("New user created", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, times(1)).add(any());
    }

    @Test
    public void testSignUpWithExistingUser() throws UserAlreadyExistsException {
        when(userStorageMock.get(null)).thenReturn(null);
        doThrow(new UserAlreadyExistsException("Username is taken")).when(userStorageMock).add(any());

        Command command = new Command(Command.SIGN_UP, new String[]{USERNAME, PASSWORD});
        String result = commandExecutor.execute(null, command);

        assertEquals("Username is taken", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(any());
        verify(userStorageMock, times(1)).add(any());
    }

    @Test
    public void testSignUpWithInvalidArgumentCount() throws UserAlreadyExistsException {
        Command command = new Command(Command.SIGN_UP, new String[]{USERNAME});
        String result = commandExecutor.execute(null, command);

        assertEquals("2 arguments expected", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(any());
        verify(userStorageMock, never()).add(any());
    }

    @Test
    public void testSignUpWhenLoggedIn() throws UserAlreadyExistsException {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.SIGN_UP, new String[]{USERNAME, PASSWORD});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Log out first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(any());
        verify(userStorageMock, never()).add(any());
    }

    @Test
    public void testLogInWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.LOG_IN, new String[]{USERNAME, PASSWORD});
        String result = commandExecutor.execute(null, command);

        assertEquals(USERNAME, result, "Usernames should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, times(1)).get(USERNAME);
    }

    @Test
    public void testLogInIncorrectUsername() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.LOG_IN, new String[]{"incorrect", PASSWORD});
        String result = commandExecutor.execute(null, command);

        assertEquals("Incorrect username or password", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, times(1)).get("incorrect");
    }

    @Test
    public void testLogInIncorrectPassword() {
        when(userStorageMock.get(null)).thenReturn(null);
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.LOG_IN, new String[]{USERNAME, "incorrect"});
        String result = commandExecutor.execute(null, command);

        assertEquals("Incorrect username or password", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, times(1)).get(USERNAME);
    }

    @Test
    public void testLogInWithInvalidArgumentCount() throws UserAlreadyExistsException {
        Command command = new Command(Command.LOG_IN, new String[]{"username"});
        String result = commandExecutor.execute(null, command);

        assertEquals("2 arguments expected", result, "Messages should be equal");
        verify(userStorageMock, only()).get(null);
        verify(userStorageMock, never()).add(any());
    }

    @Test
    public void testLogInWhenLoggedIn() throws UserAlreadyExistsException {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.LOG_IN, new String[]{USERNAME, PASSWORD});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Log out first", result, "Messages should be equal");
        verify(userStorageMock, only()).get(any());
        verify(userStorageMock, never()).add(any());
    }

    @Test
    public void testListWhenLoggedIn() throws AssetStorageException {
        List<Asset> assets = List.of(bitcoin, ethereum);

        when(userStorageMock.get(null)).thenReturn(null);
        when(assetStorageMock.getAssets()).thenReturn(assets);

        Command command = new Command(Command.LIST, new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("BTC: 19424,470312\nETH: 1039,368296", result, "Results should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(assetStorageMock, times(1)).getAssets();
    }

    @Test
    public void testListWhenAssetStorageThrowsException() throws AssetStorageException {
        when(assetStorageMock.getAssets()).thenThrow(new AssetStorageException("Failed to retrieve asset list"));

        Command command = new Command(Command.LIST, new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("Failed to retrieve asset list", result, "Messages should be equal");
        verify(assetStorageMock, times(1)).getAssets();
    }

    @Test
    public void testListWhenLoggedOut() throws AssetStorageException {
        List<Asset> assets = List.of(bitcoin, ethereum);
        when(assetStorageMock.getAssets()).thenReturn(assets);

        Command command = new Command(Command.LIST, new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("BTC: 19424,470312\nETH: 1039,368296", result, "Results should be equal");
        verify(assetStorageMock, times(1)).getAssets();
    }

    @Test
    public void testDepositWhenLoggedIn() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.DEPOSIT, new String[]{"100.0"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Money successfully deposited. Current balance: 100.0", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, times(1)).update(user);
    }

    @Test
    public void testDepositWithNegativeAmount() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.DEPOSIT, new String[]{"-50.0"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Money should not be negative", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testDepositWithInvalidAmount() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.DEPOSIT, new String[]{"invalid"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Invalid argument. Only numbers allowed", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testDepositWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.DEPOSIT, new String[]{"1000.0"});
        String result = commandExecutor.execute(null, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testBuyWhenLoggedIn() throws AssetStorageException, NegativeValueException {
        user.getWallet().deposit(100.0);

        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAsset("BTC")).thenReturn(bitcoin);

        Command command = new Command(Command.BUY, new String[]{"BTC", "100.0"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Bitcoin bought. Balance left: 0.0", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, times(1)).update(user);
        verify(assetStorageMock, times(1)).getAsset("BTC");
    }

    @Test
    public void testBuyWithInvalidArguments() {
        when(userStorageMock.get("username")).thenReturn(user);

        Command command = new Command(Command.BUY, new String[]{"BTC"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("2 arguments expected", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testBuyWithNonNumericArgument() {
        when(userStorageMock.get("username")).thenReturn(user);

        Command command = new Command(Command.BUY, new String[]{"BTC", "invalid"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Invalid argument. Only numbers allowed", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testBuyWithInsufficientBalance() throws AssetStorageException {
        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAsset("BTC")).thenReturn(bitcoin);

        Command command = new Command(Command.BUY, new String[]{"BTC", "1000.0"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Not enough money", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
        verify(assetStorageMock, times(1)).getAsset("BTC");
    }

    @Test
    public void testBuyWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.BUY, new String[]{"BTC", "100.0"});
        String result = commandExecutor.execute(null, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, never()).update(any());
    }

    @Test
    public void testSellWhenLoggedIn() throws AssetStorageException, WalletException {
        user.getWallet().deposit(1000.0);
        user.getWallet().buy("BTC", 1000.0, bitcoin.getPrice());

        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAsset("BTC")).thenReturn(bitcoin);

        Command command = new Command(Command.SELL, new String[]{"BTC"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Bitcoin sold. Current balance: 1000.0", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, times(1)).update(user);
        verify(assetStorageMock, times(1)).getAsset("BTC");
    }

    @Test
    public void testSellCommandWithInvalidAsset() throws AssetStorageException {
        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAsset("ETH")).thenThrow(new AssetStorageException("Asset not found"));

        Command command = new Command(Command.SELL, new String[]{"ETH"});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Asset not found", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(userStorageMock, never()).update(any());
        verify(assetStorageMock, times(1)).getAsset("ETH");
    }

    @Test
    public void testSellWhenLoggedOut() throws AssetStorageException {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.SELL, new String[]{"BTC"});
        String result = commandExecutor.execute(null, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(userStorageMock, never()).update(any());
        verify(assetStorageMock, never()).getAsset(any());
    }

    @Test
    public void testSummaryWithNoInvestments() throws AssetStorageException {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.SUMMARY, new String[]{});
        String result = commandExecutor.execute(USERNAME, command);
        String expectedSummary = """
            Balance: 0,00 USD

            No investments""";

        assertEquals(expectedSummary, result, "Summaries should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(assetStorageMock, times(1)).getAssetPrices(user.getWallet().getInvestments());
    }

    @Test
    public void testSummaryWithInvestments() throws AssetStorageException, WalletException {
        user.getWallet().deposit(1000.0);
        user.getWallet().buy("BTC", 500.0, 500.0);

        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAssetPrices(any())).thenReturn(Map.of("BTC", 500.0));

        Command command = new Command(Command.SUMMARY, new String[]{});
        String result = commandExecutor.execute(USERNAME, command);
        String expectedSummary = """
            Balance: 1000,00 USD
            Cash: 500,00 USD
            Crypto: 500,00 USD
            
            Current investments:
            0001,00000000 BTC ($500,00 USD)""";

        assertEquals(expectedSummary, result, "Summaries should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(assetStorageMock, times(1)).getAssetPrices(user.getWallet().getInvestments());
    }

    @Test
    public void testSummaryWhenLoggedOut() throws AssetStorageException {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.SUMMARY, new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
        verify(assetStorageMock, never()).getAssetPrices(any());
    }

    @Test
    public void testTrendsWhenLoggedIn() throws AssetStorageException, WalletException {
        user.getWallet().deposit(1000.0);
        user.getWallet().buy("BTC", 500.0, 500.0);
        user.getWallet().buy("ETH", 300.0, 300.0);

        when(userStorageMock.get(USERNAME)).thenReturn(user);
        when(assetStorageMock.getAssetPrices(any())).thenReturn(Map.of("BTC", 500.0, "ETH", 210.0));

        Command command = new Command(Command.TRENDS, new String[]{});
        String result = commandExecutor.execute(USERNAME, command);
        String expected = "Current trends:\nBTC: +0,00%\nETH: -30,00%";

        assertEquals(expected, result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(assetStorageMock, times(1)).getAssetPrices(user.getWallet().getInvestments());
    }

    @Test
    public void testTrendsCommandWhenNotLoggedIn() throws AssetStorageException {
        Command command = new Command(Command.TRENDS, new String[]{});
        when(userStorageMock.get("username")).thenReturn(null);

        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
        verify(assetStorageMock, never()).getAssetPrices(any());
    }

    @Test
    public void testLogOutWhenLoggedIn() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.LOG_OUT, new String[]{});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Logged out successfully", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
    }

    @Test
    public void testLogoutWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.LOG_OUT, new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("Log in first", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
    }

    @Test
    public void testExitWhenLoggedIn() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command(Command.EXIT, new String[]{});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Goodbye", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
    }

    @Test
    public void testExitWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command(Command.EXIT, new String[]{});

        String result = commandExecutor.execute(null, command);

        assertEquals("Goodbye", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
    }

    @Test
    public void testUnknownCommandWhenLoggedIn() {
        when(userStorageMock.get(USERNAME)).thenReturn(user);

        Command command = new Command("unknown", new String[]{});
        String result = commandExecutor.execute(USERNAME, command);

        assertEquals("Unknown command", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(USERNAME);
    }

    @Test
    public void testUnknownCommandWhenLoggedOut() {
        when(userStorageMock.get(null)).thenReturn(null);

        Command command = new Command("unknown", new String[]{});
        String result = commandExecutor.execute(null, command);

        assertEquals("Unknown command", result, "Messages should be equal");
        verify(userStorageMock, times(1)).get(null);
    }
}
