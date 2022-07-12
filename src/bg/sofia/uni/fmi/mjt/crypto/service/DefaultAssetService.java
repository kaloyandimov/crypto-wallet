package bg.sofia.uni.fmi.mjt.crypto.service;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetServiceException;
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
    private static final String API_SCHEME = "https";
    private static final String API_HOST = "rest.coinapi.io";
    private static final String API_PATH = "/v1/assets";
    private static final String API_QUERY = null;
    private static final String API_FRAGMENT = null;

    private static final String API_AUTHORIZATION_HEADER = "X-CoinAPI-Key";
    private static final String API_AUTHORIZATION_KEY = "YOUR_API_KEY_HERE";

    private static final Gson GSON = buildCustomGson();

    private final HttpClient httpClient;
    private final String apiKey;

    public DefaultAssetService(HttpClient httpClient) {
        this(httpClient, API_AUTHORIZATION_KEY);
    }

    public DefaultAssetService(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }

    @Override
    public Set<Asset> getAssets() throws AssetServiceException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(API_SCHEME, API_HOST, API_PATH, API_QUERY, API_FRAGMENT);
            HttpRequest request = HttpRequest.newBuilder(uri).header(API_AUTHORIZATION_HEADER, apiKey).build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
}
