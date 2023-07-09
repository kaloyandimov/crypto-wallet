package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.exception.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCryptoWalletTest {
    private static final double DELTA = 0.000001;

    private static String EMPTY_SUMMARY;
    private static String SUMMARY;
    private static String TRENDS;

    private CryptoWallet wallet;
    private Map<String, Double> assetPrices;

    @BeforeAll
    static void beforeAll() {
        EMPTY_SUMMARY = """
            Balance: 0,00 USD
            
            No investments""";
        SUMMARY = """
            Balance: 1000,00 USD
            Cash: 200,00 USD
            Crypto: 800,00 USD
            
            Current investments:
            0000,01000000 BTC ($500,00 USD)
            0000,10000000 ETH ($300,00 USD)""";
        TRENDS = """
            Current trends:
            BTC: +100,00%
            ETH: +300,00%""";
    }

    @BeforeEach
    void setUp() {
        wallet = new DefaultCryptoWallet();
        assetPrices = new HashMap<>();

        assetPrices.put("BTC", 50000.0);
        assetPrices.put("ETH", 3000.0);
        assetPrices.put("LTC", 150.0);
    }

    @Test
    void testOf() {
        String walletString = "1000.0;BTC=0.1;BTC=500.0";
        CryptoWallet wallet = DefaultCryptoWallet.of(walletString);

        assertEquals(walletString, wallet.toString(), "Wallet string representations should match");
    }

    @Test
    void testDeposit() throws NegativeValueException {
        assertEquals(0.0, wallet.getBalance(), DELTA, "Balance should be 0.0");

        wallet.deposit(1000.0);

        assertEquals(1000.0, wallet.getBalance(), DELTA, "Balance should be 1000.0");
    }

    @Test
    void testDepositNegativeValue() {
        assertThrows(NegativeValueException.class, () -> wallet.deposit(-100.0), "NegativeValueException expected");
    }

    @Test
    void testWithdraw() throws NegativeValueException, InsufficientResourcesException {
        wallet.deposit(1000.0);
        wallet.withdraw(500.0);

        assertEquals(500.0, wallet.getBalance(), DELTA, "Balance should be 500.0 after withdraw");
    }

    @Test
    void testWithdrawNegativeValue() {
        assertThrows(NegativeValueException.class, () -> wallet.withdraw(-100.0), "NegativeValueException expected");
    }

    @Test
    void testWithdrawInsufficientResources() {
        assertThrows(InsufficientResourcesException.class, () -> wallet.withdraw(500.0), "InsufficientResourcesException expected");
    }

    @Test
    void testBuy() throws NegativeValueException, InsufficientResourcesException {
        wallet.deposit(1000.0);
        wallet.buy("BTC", 500.0, assetPrices.get("BTC"));

        assertEquals(500.0, wallet.getBalance(), DELTA, "Balance should be 500.0");
        assertEquals(500.0, wallet.getCryptoBalance(assetPrices), DELTA, "Crypto balance should be 500.0");
        assertTrue(wallet.getInvestments().contains("BTC"), "Investments should include BTC");
    }

    @Test
    void testSell() throws AssetNotFoundException, NegativeValueException, InsufficientResourcesException {
        wallet.deposit(1000.0);
        wallet.buy("BTC", 1000.0, assetPrices.get("BTC"));
        wallet.sell("BTC", assetPrices.get("BTC"));

        assertEquals(1000.0, wallet.getBalance(), DELTA, "Balance should be 1000.0");
        assertEquals(0.0, wallet.getCryptoBalance(assetPrices), DELTA, "Crypto balance should be 0.0");
        assertFalse(wallet.getInvestments().contains("BTC"), "Investments should not contain BTC");
    }

    @Test
    void testSellAssetNotFound() {
        assertThrows(AssetNotFoundException.class, () -> wallet.sell("BTC", assetPrices.get("BTC")));
    }

    @Test
    void testGetSummaryNoInvestments() {
        assertEquals(EMPTY_SUMMARY, wallet.getSummary(assetPrices), "Summaries should match");
    }

    @Test
    void testGetSummary() throws NegativeValueException, InsufficientResourcesException {
        wallet.deposit(1000.0);
        wallet.buy("BTC", 500.0, assetPrices.get("BTC"));
        wallet.buy("ETH", 300.0, assetPrices.get("ETH"));

        assertEquals(SUMMARY, wallet.getSummary(assetPrices), "Summaries should match");
    }

    @Test
    void testGetTrendsNoInvestments() {
        assertEquals("No investments", wallet.getTrends(assetPrices), "Trends should match");
    }

    @Test
    void testGetTrends() throws NegativeValueException, InsufficientResourcesException {
        wallet.deposit(1700.0);

        wallet.buy("BTC", 1000.0, assetPrices.get("BTC"));
        wallet.buy("ETH", 500.0, assetPrices.get("ETH"));

        Map<String, Double> newPrices = new HashMap<>();
        newPrices.put("BTC", 100000.0);
        newPrices.put("ETH", 12000.0);

        assertEquals(TRENDS, wallet.getTrends(newPrices), "Trends should match");
    }
}
