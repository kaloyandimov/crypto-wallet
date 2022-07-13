package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;

public interface AssetStorage {
    Asset getAsset(String assetId) throws AssetStorageException;
}
