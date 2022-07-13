package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnknownAssetException;
import bg.sofia.uni.fmi.mjt.crypto.service.AssetService;
import bg.sofia.uni.fmi.mjt.crypto.service.DefaultAssetService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultAssetStorage implements AssetStorage {
    private static final int DEFAULT_UPDATE_INTERVAL = 30;
    private static final int DEFAULT_ASSET_LIMIT = 150;

    private final AssetService assetService;

    private Map<String, Asset> assets;
    private LocalDateTime lastUpdate;
    private int updateInterval;
    private int assetLimit;

    public DefaultAssetStorage() {
        this(new DefaultAssetService(), DEFAULT_UPDATE_INTERVAL, DEFAULT_ASSET_LIMIT);
    }

    public DefaultAssetStorage(AssetService assetService) {
        this(assetService, DEFAULT_UPDATE_INTERVAL, DEFAULT_ASSET_LIMIT);
    }

    public DefaultAssetStorage(AssetService assetService, int updateInterval, int assetLimit) {
        this.assetService = assetService;
        setUpdateInterval(updateInterval);
        setAssetLimit(assetLimit);
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval >= 0 ? updateInterval : DEFAULT_UPDATE_INTERVAL;
    }

    public int getAssetLimit() {
        return assetLimit;
    }

    public void setAssetLimit(int assetLimit) {
        this.assetLimit = assetLimit >= 0 ? assetLimit : DEFAULT_ASSET_LIMIT;
    }

    @Override
    public Asset getAsset(String assetId) throws AssetStorageException {
        requireUpToDate();

        if (!assets.containsKey(assetId)) {
            throw new UnknownAssetException("Unknown asset: " + assetId);
        }

        return assets.get(assetId);
    }

    private void requireUpToDate() throws AssetStorageException {
        if (!isUpToDate()) {
            update();
        }
    }

    private boolean isUpToDate() {
        return lastUpdate != null && lastUpdate.plusMinutes(updateInterval).isAfter(LocalDateTime.now());
    }

    private void update() throws AssetStorageException {
        try {
            assets = assetService.getAssets().stream()
                .filter(Asset::isCrypto)
                .sorted(Comparator.comparingDouble(Asset::getPrice).reversed())
                .limit(assetLimit)
                .collect(Collectors.toMap(Asset::getId, Function.identity()));
            lastUpdate = LocalDateTime.now();
        } catch (AssetServiceException e) {
            throw new AssetStorageException("Could not load data", e);
        }
    }
}
