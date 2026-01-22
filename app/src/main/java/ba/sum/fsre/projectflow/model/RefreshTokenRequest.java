package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {
    @SerializedName("refresh_token")
    public String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
