package ba.sum.fsre.projectflow.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREFS = "auth_prefs";
    private static final String TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_ID = "user_id";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void saveRefreshToken(String token) {
        prefs.edit().putString(REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        prefs.edit()
                .remove(TOKEN)
                .remove(REFRESH_TOKEN)
                .remove(USER_ID)
                .apply();
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(USER_ID, null);
    }

    public boolean hasValidSession() {
        return getToken() != null && getRefreshToken() != null;
    }

}
