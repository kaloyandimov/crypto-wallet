package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnknownAssetException;
import bg.sofia.uni.fmi.mjt.crypto.service.AssetService;
import bg.sofia.uni.fmi.mjt.crypto.service.DefaultAssetService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
    public Asset getAsset(String id) throws AssetStorageException {
        requireUpToDate();

        if (!assets.containsKey(id)) {
            throw new UnknownAssetException("Unknown asset: " + id);
        }

        return assets.get(id);
    }

    @Override
    public List<Asset> getAssets() throws AssetStorageException {
        requireUpToDate();

        return List.copyOf(assets.values());
    }

    @Override
    public Double getAssetPrice(String id) throws AssetStorageException {
        requireUpToDate();

        Asset asset = assets.get(id);
        return asset != null ? asset.getPrice() : 0.0;
    }

    @Override
    public Map<String, Double> getAssetPrices(List<String> ids) throws AssetStorageException {
        requireUpToDate();

        return ids.stream()
            .collect(Collectors.toMap(Function.identity(), id -> {
                try {
                    return getAssetPrice(id);
                } catch (AssetStorageException e) {
                    return 0.0;
                }
            }));
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
