package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;

import java.util.List;
import java.util.Map;

public interface AssetStorage {
    Asset getAsset(String id) throws AssetStorageException;

    List<Asset> getAssets() throws AssetStorageException;

    Double getAssetPrice(String id) throws AssetStorageException;

    Map<String, Double> getAssetPrices(List<String> ids) throws AssetStorageException;
}
