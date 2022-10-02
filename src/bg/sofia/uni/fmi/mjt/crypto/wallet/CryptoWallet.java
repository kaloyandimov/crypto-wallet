package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.exception.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;

import java.util.List;
import java.util.Map;

public interface CryptoWallet extends Wallet {
    double getCryptoBalance(Map<String, Double> assetPrices);

    List<String> getInvestments();

    void buy(String assetId, double moneyToInvest, double assetPrice)
        throws NegativeValueException, InsufficientResourcesException;

    void sell(String assetId, double assetPrice) throws AssetNotFoundException;

    String getSummary(Map<String, Double> assetPrices);

    String getTrends(Map<String, Double> assetPrices);
}
