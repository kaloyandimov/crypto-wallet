package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.exception.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultCryptoWallet implements CryptoWallet {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String DELIMITER_FIELD = ";";
    private static final String DELIMITER_ENTRIES = ",";
    private static final String DELIMITER_ENTRY = "=";

    private static final String FORMAT_BALANCE = "%s: %.2f USD";
    private static final String FORMAT_INVESTMENT = "%013.8f %s ($%.2f USD)";
    private static final String FORMAT_TREND = "%s: %+.2f%%";

    private static final int BALANCE = 0;
    private static final int INVESTMENTS = 1;
    private static final int ENTRY_PRICES = 2;
    private static final int NUMBER_OF_FIELDS = 3;

    private static final int ONE_HUNDRED = 100;

    private final Map<String, Double> investments;
    private final Map<String, Double> entryPrices;

    private double balance;

    public DefaultCryptoWallet() {
        this(0.0, new HashMap<>(), new HashMap<>());
    }

    public DefaultCryptoWallet(double balance, Map<String, Double> investments, Map<String, Double> entryPrices) {
        this.balance = balance;
        this.investments = investments;
        this.entryPrices = entryPrices;
    }

    public static CryptoWallet of(String line) {
        final String[] tokens = line.split(DELIMITER_FIELD, NUMBER_OF_FIELDS);

        double balance = Double.parseDouble(tokens[BALANCE]);
        Map<String, Double> investments = parseMap(tokens[INVESTMENTS]);
        Map<String, Double> entryPrices = parseMap(tokens[ENTRY_PRICES]);

        return new DefaultCryptoWallet(balance, investments, entryPrices);
    }

    @Override
    public void deposit(double money) throws NegativeValueException {
        assertNonNegative(money);

        balance += money;
    }

    @Override
    public void withdraw(double money) throws NegativeValueException, InsufficientResourcesException {
        assertNonNegative(money);

        if (balance < money) {
            throw new InsufficientResourcesException("Not enough money");
        }

        balance -= money;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public double getCryptoBalance(Map<String, Double> assetPrices) {
        return investments.keySet().stream()
            .mapToDouble(key -> investments.get(key) * assetPrices.get(key))
            .sum();
    }

    @Override
    public List<String> getInvestments() {
        return List.copyOf(investments.keySet());
    }

    @Override
    public void buy(String assetId, double moneyToInvest, double assetPrice)
        throws NegativeValueException, InsufficientResourcesException {
        double quantity = moneyToInvest / assetPrice;

        withdraw(moneyToInvest);

        entryPrices.putIfAbsent(assetId, assetPrice);
        investments.merge(assetId, quantity, Double::sum);
    }

    @Override
    public void sell(String assetId, double assetPrice) throws AssetNotFoundException {
        if (!investments.containsKey(assetId)) {
            throw new AssetNotFoundException("Asset not found in wallet");
        }

        balance += investments.get(assetId) * assetPrice;

        investments.remove(assetId);
        entryPrices.remove(assetId);
    }

    @Override
    public String getSummary(Map<String, Double> assetPrices) {
        if (investments.isEmpty()) {
            return FORMAT_BALANCE.formatted("Balance", balance)
                + LINE_SEPARATOR
                + LINE_SEPARATOR
                + "No investments";
        }

        double cryptoBalance = getCryptoBalance(assetPrices);

        return FORMAT_BALANCE.formatted("Balance", balance + cryptoBalance)
            + LINE_SEPARATOR
            + FORMAT_BALANCE.formatted("Cash", balance)
            + LINE_SEPARATOR
            + FORMAT_BALANCE.formatted("Crypto", cryptoBalance)
            + LINE_SEPARATOR
            + LINE_SEPARATOR
            + "Current investments:"
            + LINE_SEPARATOR
            + investments.keySet().stream()
            .map(key -> investmentToString(key, assetPrices.get(key)))
            .collect(Collectors.joining(LINE_SEPARATOR));
    }

    @Override
    public String getTrends(Map<String, Double> assetPrices) {
        if (investments.isEmpty()) {
            return "No investments";
        }

        return "Current trends:"
            + LINE_SEPARATOR
            + investments.keySet().stream()
            .map(key -> trendToString(key, assetPrices.get(key)))
            .collect(Collectors.joining(LINE_SEPARATOR));
    }

    @Override
    public String toString() {
        return balance + DELIMITER_FIELD + mapToString(investments) + DELIMITER_FIELD + mapToString(entryPrices);
    }

    private static Map.Entry<String, Double> parseEntry(String line) {
        final String[] tokens = line.split(DELIMITER_ENTRY);

        return Map.entry(tokens[0], Double.parseDouble(tokens[1]));
    }

    private static Map<String, Double> parseMap(String line) {
        if (line.isBlank()) {
            return Collections.emptyMap();
        }

        return Arrays.stream(line.split(DELIMITER_ENTRIES))
            .map(DefaultCryptoWallet::parseEntry)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String investmentToString(String assetId, double price) {
        double quantity = investments.get(assetId);
        double value = price * quantity;

        return FORMAT_INVESTMENT.formatted(quantity, assetId, value);
    }

    private String trendToString(String assetId, double price) {
        double entryPrice = entryPrices.get(assetId);
        double percentageChange = (price - entryPrice) / entryPrice * ONE_HUNDRED;

        return FORMAT_TREND.formatted(assetId, percentageChange);
    }

    private static <K, V> String mapToString(Map<K, V> map) {
        return map.entrySet().stream()
            .map(e -> e.getKey() + DELIMITER_ENTRY + e.getValue())
            .collect(Collectors.joining(DELIMITER_ENTRIES));
    }

    private void assertNonNegative(double number) throws NegativeValueException {
        if (number < 0) {
            throw new NegativeValueException("Money should not be negative");
        }
    }
}
