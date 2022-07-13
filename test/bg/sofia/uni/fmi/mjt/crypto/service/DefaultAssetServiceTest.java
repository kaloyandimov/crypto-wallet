package bg.sofia.uni.fmi.mjt.crypto.service;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.BadRequestException;
import bg.sofia.uni.fmi.mjt.crypto.exception.ForbiddenException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NoDataException;
import bg.sofia.uni.fmi.mjt.crypto.exception.TooManyRequestsException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAssetServiceTest {
    private static final String EXPECTED_EXCEPTION_FOR_CODE = "%s expected for status code %d";

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

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        Mockito.lenient()
            .when(serviceHttpClientMock.send(Mockito.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(serviceResponseMock);
    }

    @Test
    public void testGetAssetsSuccess() throws AssetServiceException {
        when(serviceResponseMock.statusCode()).thenReturn(200);
        when(serviceResponseMock.body()).thenReturn(assetsJson);

        Set<Asset> expected = Set.of(bitcoin, ethereum);
        Set<Asset> actual = service.getAssets();

        assertTrue(actual.containsAll(expected), "Actual set should contain all the expected elements");
        assertTrue(expected.containsAll(actual), "Expected set should contain all the actual elements");
    }

    @Test
    public void testGetAssetsThrowsBadRequestException() {
        when(serviceResponseMock.statusCode()).thenReturn(400);
        assertThrows(BadRequestException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("BadRequestException", 400));
    }

    @Test
    public void testGetAssetsThrowsUnauthorizedException() {
        when(serviceResponseMock.statusCode()).thenReturn(401);
        assertThrows(UnauthorizedException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("UnauthorizedException", 401));
    }

    @Test
    public void testGetAssetsThrowsForbiddenException() {
        when(serviceResponseMock.statusCode()).thenReturn(403);
        assertThrows(ForbiddenException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("ForbiddenException", 403));
    }

    @Test
    public void testGetAssetsThrowsTooManyRequestsException() {
        when(serviceResponseMock.statusCode()).thenReturn(429);
        assertThrows(TooManyRequestsException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("TooManyRequestsException", 429));
    }

    @Test
    public void testGetAssetsThrowsNoDataException() {
        when(serviceResponseMock.statusCode()).thenReturn(550);
        assertThrows(NoDataException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("NoDataException", 550));
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForUnknownStatusCode() {
        when(serviceResponseMock.statusCode()).thenReturn(666);
        assertThrows(AssetServiceException.class, () -> service.getAssets(),
            EXPECTED_EXCEPTION_FOR_CODE.formatted("AssetServiceException", 666));
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForIOException() throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenThrow(IOException.class);
        assertThrows(AssetServiceException.class, () -> service.getAssets(),
            "AssetServiceException expected in place of IOException");
    }

    @Test
    public void testGetAssetsThrowsAssetServiceExceptionForInterruptedException()
        throws IOException, InterruptedException {
        when(serviceHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenThrow(InterruptedException.class);
        assertThrows(AssetServiceException.class, () -> service.getAssets(),
            "AssetServiceException expected in place of InterruptedException");
    }
}
