package bg.sofia.uni.fmi.mjt.crypto.service;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.BadRequestException;
import bg.sofia.uni.fmi.mjt.crypto.exception.ForbiddenException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NoDataException;
import bg.sofia.uni.fmi.mjt.crypto.exception.TooManyRequestsException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAssetServiceTest {
    private static Asset bitcoin;
    private static Asset ethereum;

    private static String bitcoinJson;
    private static String ethereumJson;
    private static String assetsJson;

    @Mock
    private HttpClient serviceHttpClientMock;

    @Mock
    private HttpResponse<String> serviceResponseMock;

    @InjectMocks
    private DefaultAssetService service;

    @BeforeAll
    public static void setUpTestCase() {
        bitcoin = new Asset("BTC", "Bitcoin", true, 19424.470311714055519056008384);
        ethereum = new Asset("ETH", "Ethereum", true, 1039.3682961935640591450912866);
        bitcoinJson = """
            {
                "asset_id": "BTC",
                "name": "Bitcoin",
                "type_is_crypto": 1,
                "price_usd": 19424.470311714055519056008384
            }
            """;
        ethereumJson = """
            {
                "asset_id": "ETH",
                "name": "Ethereum",
                "type_is_crypto": 1,
                "price_usd": 1039.3682961935640591450912866
            }
            """;
        assetsJson = """
            [
                {
                    "asset_id": "BTC",
                    "name": "Bitcoin",
                    "type_is_crypto": 1,
                    "price_usd": 19424.470311714055519056008384
                },
                {
                    "asset_id": "ETH",
                    "name": "Ethereum",
                    "type_is_crypto": 1,
                    "price_usd": 1039.3682961935640591450912866
                }
            ]
            """;
    }

    @Test
    public void testGetAssetSuccess() throws AssetServiceException, IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
        when(serviceResponseMock.statusCode()).thenReturn(200);
        when(serviceResponseMock.body()).thenReturn(bitcoinJson);

        Asset actual = service.getAsset("BTC");

        assertEquals(bitcoin, actual, "Bitcoins do not match");

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock).statusCode();
        verify(serviceResponseMock).body();
    }

    @Test
    public void testGetAssetTwiceSuccess() throws AssetServiceException, IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
        when(serviceResponseMock.statusCode()).thenReturn(200);
        when(serviceResponseMock.body()).thenReturn(bitcoinJson, ethereumJson);

        Asset actualBitcoin = service.getAsset("BTC");
        Asset actualEthereum = service.getAsset("ETH");

        assertEquals(bitcoin, actualBitcoin, "Actual bitcoin should match the expected bitcoin");
        assertEquals(ethereum, actualEthereum, "Actual ethereum should match the expected ethereum");

        verify(serviceHttpClientMock, times(2)).send(any(HttpRequest.class), any());
        verify(serviceResponseMock, times(2)).statusCode();
        verify(serviceResponseMock, times(2)).body();
    }

    @Test
    public void testGetAssetThrowsBadRequestException() throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(BadRequestException.class, 400);
    }

    @Test
    public void testGetAssetThrowsUnauthorizedException() throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(UnauthorizedException.class, 401);
    }

    @Test
    public void testGetAssetThrowsForbiddenException() throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(ForbiddenException.class, 403);
    }

    @Test
    public void testGetAssetThrowsTooManyRequestsException() throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(TooManyRequestsException.class, 429);
    }

    @Test
    public void testGetAssetThrowsNoDataException() throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(NoDataException.class, 550);
    }

    @Test
    public void testGetAssetThrowsAssetServiceExceptionForUnknownStatusCode()
        throws IOException, InterruptedException {
        assertGetAssetThrowsAppropriateExceptionForStatusCode(AssetServiceException.class, 666);
    }

    @Test
    public void testGetAssetThrowsAssetServiceExceptionForIOException() throws IOException, InterruptedException {
        assertGetAssetThrowsAssetServiceExceptionInPlaceOf(IOException.class);
    }

    @Test
    public void testGetAssetThrowsAssetServiceExceptionForInterruptedException()
        throws IOException, InterruptedException {
        assertGetAssetThrowsAssetServiceExceptionInPlaceOf(InterruptedException.class);
    }

    @Test
    public void testGetAssetsSuccess() throws AssetServiceException, IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
        when(serviceResponseMock.statusCode()).thenReturn(200);
        when(serviceResponseMock.body()).thenReturn(assetsJson);

        Set<Asset> expected = Set.of(bitcoin, ethereum);
        Set<Asset> actual = service.getAssets();

        assertTrue(actual.containsAll(expected), "Actual set should contain all the expected elements");
        assertTrue(expected.containsAll(actual), "Expected set should contain all the actual elements");

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock).statusCode();
        verify(serviceResponseMock).body();
    }

    @Test
    public void testGetAssetsThrowsBadRequestException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(BadRequestException.class, 400);
    }

    @Test
    public void testGetAssetsThrowsUnauthorizedException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(UnauthorizedException.class, 401);
    }

    @Test
    public void testGetAssetsThrowsForbiddenException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(ForbiddenException.class, 403);
    }

    @Test
    public void testGetAssetsThrowsTooManyRequestsException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(TooManyRequestsException.class, 429);
    }

    @Test
    public void testGetAssetsThrowsNoDataException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(NoDataException.class, 550);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForUnknownStatusCode()
        throws IOException, InterruptedException {
        assertGetAssetsThrowsAppropriateExceptionForStatusCode(AssetServiceException.class, 666);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForIOException() throws IOException, InterruptedException {
        assertGetAssetsThrowsAssetServiceExceptionInPlaceOf(IOException.class);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForInterruptedException()
        throws IOException, InterruptedException {
        assertGetAssetsThrowsAssetServiceExceptionInPlaceOf(InterruptedException.class);
    }

    private void assertFunctionThrowsAppropriateExceptionForStatusCode(Class<? extends AssetServiceException> exceptionClass, int statusCode, Executable executable)
        throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
        when(serviceResponseMock.statusCode()).thenReturn(statusCode);

        assertThrows(exceptionClass, executable,
            exceptionClass.getSimpleName() + " expected for status code " + statusCode);

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock).statusCode();
        verify(serviceResponseMock, never()).body();
    }

    private void assertFunctionThrowsAssetServiceExceptionInPlaceOf(Class<? extends Exception> exceptionClass, Executable executable)
        throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenThrow(exceptionClass);

        assertThrows(AssetServiceException.class, executable,
            "AssetServiceException expected in place of " + exceptionClass.getSimpleName());

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock, never()).statusCode();
        verify(serviceResponseMock, never()).body();
    }

    private void assertGetAssetThrowsAppropriateExceptionForStatusCode(Class<? extends AssetServiceException> exceptionClass, int statusCode)
        throws IOException, InterruptedException {
        assertFunctionThrowsAppropriateExceptionForStatusCode(exceptionClass, statusCode, () -> service.getAsset("any"));
    }

    private void assertGetAssetThrowsAssetServiceExceptionInPlaceOf(Class<? extends Exception> exceptionClass)
        throws IOException, InterruptedException {
        assertFunctionThrowsAssetServiceExceptionInPlaceOf(exceptionClass, () -> service.getAsset("any"));
    }

    private void assertGetAssetsThrowsAppropriateExceptionForStatusCode(Class<? extends AssetServiceException> exceptionClass, int statusCode)
        throws IOException, InterruptedException {
        assertFunctionThrowsAppropriateExceptionForStatusCode(exceptionClass, statusCode, () -> service.getAssets());
    }

    private void assertGetAssetsThrowsAssetServiceExceptionInPlaceOf(Class<? extends Exception> exceptionClass)
        throws IOException, InterruptedException {
        assertFunctionThrowsAssetServiceExceptionInPlaceOf(exceptionClass, () -> service.getAssets());
    }
}
