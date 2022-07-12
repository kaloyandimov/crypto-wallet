package bg.sofia.uni.fmi.mjt.crypto.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Asset {
    @SerializedName("asset_id")
    private final String id;
    @SerializedName("name")
    private final String name;
    @SerializedName("type_is_crypto")
    private final boolean isCrypto;
    @SerializedName("price_usd")
    private final double price;

    public Asset(String id, String name, boolean isCrypto, double price) {
        this.id = id;
        this.name = name;
        this.isCrypto = isCrypto;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCrypto() {
        return isCrypto;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Asset asset = (Asset) o;
        return Objects.equals(id, asset.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
