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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAssetServiceTest {
    private static Asset bitcoin;
    private static Asset ethereum;
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
        assertThrowsAppropriateExceptionForStatusCode(BadRequestException.class, 400);
    }

    @Test
    public void testGetAssetsThrowsUnauthorizedException() throws IOException, InterruptedException {
        assertThrowsAppropriateExceptionForStatusCode(UnauthorizedException.class, 401);
    }

    @Test
    public void testGetAssetsThrowsForbiddenException() throws IOException, InterruptedException {
        assertThrowsAppropriateExceptionForStatusCode(ForbiddenException.class, 403);
    }

    @Test
    public void testGetAssetsThrowsTooManyRequestsException() throws IOException, InterruptedException {
        assertThrowsAppropriateExceptionForStatusCode(TooManyRequestsException.class, 429);
    }

    @Test
    public void testGetAssetsThrowsNoDataException() throws IOException, InterruptedException {
        assertThrowsAppropriateExceptionForStatusCode(NoDataException.class, 550);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForUnknownStatusCode()
        throws IOException, InterruptedException {
        assertThrowsAppropriateExceptionForStatusCode(AssetServiceException.class, 666);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForIOException() throws IOException, InterruptedException {
        assertThrowsAssetServiceExceptionInPlaceOf(IOException.class);
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForInterruptedException()
        throws IOException, InterruptedException {
        assertThrowsAssetServiceExceptionInPlaceOf(InterruptedException.class);
    }

    private void assertThrowsAppropriateExceptionForStatusCode(Class<? extends AssetServiceException> exceptionClass, int statusCode)
        throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
        when(serviceResponseMock.statusCode()).thenReturn(statusCode);

        assertThrows(exceptionClass, () -> service.getAssets(),
            exceptionClass.getSimpleName() + " expected for status code " + statusCode);

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock).statusCode();
        verify(serviceResponseMock, never()).body();
    }

    private void assertThrowsAssetServiceExceptionInPlaceOf(Class<? extends Exception> exceptionClass)
        throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenThrow(exceptionClass);

        assertThrows(AssetServiceException.class, () -> service.getAssets(),
            "AssetServiceException expected in place of " + exceptionClass.getSimpleName());

        verify(serviceHttpClientMock).send(any(HttpRequest.class), any());
        verify(serviceResponseMock, never()).statusCode();
        verify(serviceResponseMock, never()).body();
    }
}
