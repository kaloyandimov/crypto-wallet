package bg.sofia.uni.fmi.mjt.crypto.service;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;

import java.util.Set;

public interface AssetService {
    Asset getAsset(String id) throws AssetServiceException;

    Set<Asset> getAssets() throws AssetServiceException;
}
