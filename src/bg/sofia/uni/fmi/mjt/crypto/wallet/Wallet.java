package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Wallet {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String DELIMITER_FIELD = ";";
    private static final String DELIMITER_ENTRIES = ",";
    private static final String DELIMITER_ENTRY = "=";

    private static final String FORMAT_BALANCE = "%s %.2f USD%n";
    private static final String FORMAT_INVESTMENT = "%013.8f %s ($%.2f USD)%n";
    private static final String FORMAT_STATISTICS = "%s: %+.2f%%%n";

    private static final int BALANCE = 0;
    private static final int INVESTMENTS = 1;
    private static final int ENTRY_PRICES = 2;
    private static final int NUMBER_OF_FIELDS = 3;

    private static final int ONE_HUNDRED = 100;

    private final Map<String, Double> investments;
    private final Map<String, Double> entryPrices;

    private double balance;

    public Wallet() {
        this(0.0, new HashMap<>(), new HashMap<>());
    }

    public Wallet(double balance, Map<String, Double> investments, Map<String, Double> entryPrices) {
        this.balance = balance;
        this.investments = investments;
        this.entryPrices = entryPrices;
    }

    public static Wallet of(String line) {
        String[] tokens = line.split(DELIMITER_FIELD, NUMBER_OF_FIELDS);

        double balance = Double.parseDouble(tokens[BALANCE]);
        Map<String, Double> investments = parseMap(tokens[INVESTMENTS]);
        Map<String, Double> initialPrices = parseMap(tokens[ENTRY_PRICES]);

        return new Wallet(balance, investments, initialPrices);
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double money) {
        assertNonNegative(money);

        balance += money;
    }

    public void withdraw(double money) throws InsufficientResourcesException {
        assertNonNegative(money);

        if (balance < money) {
            throw new InsufficientResourcesException("Not enough money");
        }

        balance -= money;
    }

    public void buy(String assetId, double moneyToInvest, double assetPrice) throws InsufficientResourcesException {
        double quantity = moneyToInvest / assetPrice;

        withdraw(moneyToInvest);
        entryPrices.putIfAbsent(assetId, assetPrice);
        investments.merge(assetId, quantity, Double::sum);
    }

    public void sell(String assetId, double assetPrice) {
        if (!investments.containsKey(assetId)) {
            return;
        }

        double profit = investments.get(assetId) * assetPrice;

        deposit(profit);
        investments.remove(assetId);
        entryPrices.remove(assetId);
    }

    public String getSummary(Map<String, Double> assetPrices) {
        StringBuilder summary = new StringBuilder();
        double cryptoBalance = 0;

        for (Map.Entry<String, Double> investment : investments.entrySet()) {
            String id = investment.getKey();
            double price = assetPrices.get(id);
            double quantity = investment.getValue();
            double value = price * quantity;
            cryptoBalance += value;

            summary.append(FORMAT_INVESTMENT.formatted(quantity, id, value));
        }

        return FORMAT_BALANCE.formatted("Balance:", balance + cryptoBalance)
            + FORMAT_BALANCE.formatted("Cash:", balance)
            + FORMAT_BALANCE.formatted("Crypto:", cryptoBalance)
            + LINE_SEPARATOR
            + "Current investments:"
            + LINE_SEPARATOR
            + summary;
    }

    public String getStatistics(Map<String, Double> assetPrices) {
        StringBuilder statistics = new StringBuilder("Current trends:" + LINE_SEPARATOR);

        for (Map.Entry<String, Double> investment : investments.entrySet()) {
            String id = investment.getKey();
            double price = assetPrices.get(id);
            double entryPrice = entryPrices.get(id);
            double percentageChange = (price - entryPrice) / entryPrice * ONE_HUNDRED;

            statistics.append(FORMAT_STATISTICS.formatted(id, percentageChange));
        }

        return statistics.toString();
    }

    @Override
    public String toString() {
        return balance + DELIMITER_FIELD + mapToString(investments) + DELIMITER_FIELD + mapToString(entryPrices);
    }

    private static Map<String, Double> parseMap(String line) {
        Map<String, Double> map = new HashMap<>();

        if (line.isBlank()) {
            return map;
        }

        String[] entries = line.split(DELIMITER_ENTRIES);

        for (String entry : entries) {
            String[] tokens = entry.split(DELIMITER_ENTRY);
            map.put(tokens[0], Double.parseDouble(tokens[1]));
        }

        return map;
    }

    private static <K, V> String mapToString(Map<K, V> map) {
        return map.entrySet().stream()
            .map(e -> e.getKey() + DELIMITER_ENTRY + e.getValue())
            .collect(Collectors.joining(DELIMITER_ENTRIES));
    }

    private void assertNonNegative(double number) {
        if (number < 0) {
            throw new IllegalArgumentException("Money should not be negative");
        }
    }
}
