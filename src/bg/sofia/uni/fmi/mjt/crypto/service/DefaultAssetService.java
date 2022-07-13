package bg.sofia.uni.fmi.mjt.crypto.service;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.BadRequestException;
import bg.sofia.uni.fmi.mjt.crypto.exception.ForbiddenException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NoDataException;
import bg.sofia.uni.fmi.mjt.crypto.exception.TooManyRequestsException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UnauthorizedException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

public class DefaultAssetService implements AssetService {
    private static final String API_AUTH_HEADER = "X-CoinAPI-Key";
    private static final String API_AUTH_KEY = "YOUR_API_KEY_HERE";

    private static final String API_SCHEME = "https";
    private static final String API_HOST = "rest.coinapi.io";
    private static final String API_PATH = "/v1/assets";
    private static final String API_QUERY = null;
    private static final String API_FRAGMENT = null;

    private static final int API_STATUS_CODE_OK = 200;
    private static final int API_STATUS_CODE_BAD_REQUEST = 400;
    private static final int API_STATUS_CODE_UNAUTHORIZED = 401;
    private static final int API_STATUS_CODE_FORBIDDEN = 403;
    private static final int API_STATUS_CODE_TOO_MANY_REQUESTS = 429;
    private static final int API_STATUS_CODE_NO_DATA = 550;

    private static final Gson GSON = buildCustomGson();

    private final HttpClient httpClient;

    public DefaultAssetService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Set<Asset> getAssets() throws AssetServiceException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(API_SCHEME, API_HOST, API_PATH, API_QUERY, API_FRAGMENT);
            HttpRequest request = HttpRequest.newBuilder(uri).header(API_AUTH_HEADER, API_AUTH_KEY).build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode != API_STATUS_CODE_OK) {
                throwCorrespondingException(statusCode);
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new AssetServiceException("Could not fetch assets", e);
        }

        Type setType = new TypeToken<Set<Asset>>() { }.getType();
        return GSON.fromJson(response.body(), setType);
    }

    private static Gson buildCustomGson() {
        GsonBuilder builder = new GsonBuilder();

        JsonDeserializer<Boolean> booleanDeserializer = (jsonElement, t, c) -> jsonElement.getAsInt() == 1;
        builder.registerTypeAdapter(boolean.class, booleanDeserializer);

        return builder.create();
    }

    private void throwCorrespondingException(int statusCode) throws AssetServiceException {
        throw switch (statusCode) {
            case API_STATUS_CODE_BAD_REQUEST -> new BadRequestException("Invalid request");
            case API_STATUS_CODE_UNAUTHORIZED -> new UnauthorizedException("Authentication error");
            case API_STATUS_CODE_FORBIDDEN -> new ForbiddenException("Not enough permissions");
            case API_STATUS_CODE_TOO_MANY_REQUESTS -> new TooManyRequestsException("Too many requests");
            case API_STATUS_CODE_NO_DATA -> new NoDataException("Data not found");
            default -> new AssetServiceException("Unexpected error");
        };
    }
}
