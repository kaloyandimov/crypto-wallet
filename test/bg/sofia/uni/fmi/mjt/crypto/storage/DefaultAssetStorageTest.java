package bg.sofia.uni.fmi.mjt.crypto.storage;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnknownAssetException;
import bg.sofia.uni.fmi.mjt.crypto.service.AssetService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAssetStorageTest {
    private static Asset bitcoin;
    private static Asset ethereum;
    private static Set<Asset> bitcoinSet;

    private final AssetService serviceMock = Mockito.mock(AssetService.class);
    private final DefaultAssetStorage storage = new DefaultAssetStorage(serviceMock);

    @BeforeAll
    public static void setUpTestCase() {
        bitcoin = new Asset("BTC", "Bitcoin", true, 19424.470311714055519056008384);
        ethereum = new Asset("ETH", "Ethereum", true, 1039.3682961935640591450912866);
        bitcoinSet = Set.of(bitcoin);
    }

    @Test
    void testGetAssetSuccess() throws AssetServiceException, AssetStorageException {
        when(serviceMock.getAssets()).thenReturn(bitcoinSet);

        assertEquals(bitcoin, storage.getAsset("BTC"), "Storage should contain bitcoin");

        verify(serviceMock).getAssets();
    }

    @Test
    void testGetAssetSuccessNegativeValues() throws AssetServiceException, AssetStorageException {
        when(serviceMock.getAssets()).thenReturn(bitcoinSet);

        storage.setUpdateInterval(-1);
        storage.setAssetLimit(-1);

        assertEquals(30, storage.getUpdateInterval(), "Update interval should be set to default");
        assertEquals(150, storage.getAssetLimit(), "Asset limit should be set to default");
        assertEquals(bitcoin, storage.getAsset("BTC"), "Storage should contain bitcoin");

        verify(serviceMock).getAssets();
    }

    @Test
    void testGetAssetSuccessAfterUpdate() throws AssetServiceException, AssetStorageException {
        when(serviceMock.getAssets()).thenReturn(bitcoinSet).thenReturn(Set.of(ethereum));

        storage.setUpdateInterval(0);

        assertEquals(bitcoin, storage.getAsset("BTC"), "Storage should contain bitcoin before update");
        assertEquals(ethereum, storage.getAsset("ETH"), "Storage should contain ethereum after update");

        verify(serviceMock, times(2)).getAssets();
    }

    @Test
    void testGetAssetStorageChangeLimit() throws AssetServiceException, AssetStorageException {
        when(serviceMock.getAssets()).thenReturn(Set.of(bitcoin, ethereum));

        storage.setAssetLimit(1);

        assertEquals(bitcoin, storage.getAsset("BTC"), "Storage should contain bitcoin");
        assertThrows(UnknownAssetException.class, () -> storage.getAsset("ETH"), "UnknownAssetException expected");

        verify(serviceMock).getAssets();
    }

    @Test
    void testGetAssetThrowsUnknownAssetException() throws AssetServiceException {
        when(serviceMock.getAssets()).thenReturn(bitcoinSet);

        assertThrows(UnknownAssetException.class, () -> storage.getAsset("ANY"), "UnknownAssetException expected");

        verify(serviceMock).getAssets();
    }

    @Test
    void testGetAssetThrowsAssetStorageExceptionForAssetServiceException() throws AssetServiceException {
        when(serviceMock.getAssets()).thenThrow(AssetServiceException.class);

        assertThrows(AssetStorageException.class, () -> storage.getAsset("BTC"), "AssetStorageException expected");

        verify(serviceMock).getAssets();
    }
}
