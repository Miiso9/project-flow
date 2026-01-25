package ba.sum.fsre.projectflow.network;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import ba.sum.fsre.projectflow.BuildConfig;
import ba.sum.fsre.projectflow.auth.AuthActivity;
import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.RefreshTokenRequest;
import ba.sum.fsre.projectflow.storage.TokenManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(this.context);
    }

    String url = BuildConfig.SUPABASE_URL;
    String key = BuildConfig.SUPABASE_ANON_KEY;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        if (original.header("No-Authentication") != null) {
            return chain.proceed(original.newBuilder()
                    .removeHeader("No-Authentication")
                    .build());
        }
        String accessToken = tokenManager.getToken();
        Request.Builder builder = original.newBuilder();

        if (accessToken != null) {
            builder.addHeader("Authorization", "Bearer " + accessToken);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401) {
            synchronized (this) {

                String latestToken = tokenManager.getToken();

                boolean alreadyRefreshed = latestToken != null && !latestToken.equals(accessToken);

                if (alreadyRefreshed || refreshToken()) {
                    response.close();

                    Request retry = original.newBuilder()
                            .header("Authorization", "Bearer " + tokenManager.getToken())
                            .build();
                    return chain.proceed(retry);
                } else {
                    forceLogout();
                }
            }
        }

        return response;
    }

    private boolean refreshToken() {
        try {
            OkHttpClient cleanClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("apikey", key)
                                    .build()
                    ))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url + "/")
                    .client(cleanClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi api = retrofit.create(SupabaseApi.class);

            String refreshToken = tokenManager.getRefreshToken();
            if (refreshToken == null) return false;

            Call<AuthResponse> call = api.refreshToken(new RefreshTokenRequest(refreshToken));

            retrofit2.Response<AuthResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveToken(response.body().accessToken);
                tokenManager.saveRefreshToken(response.body().refreshToken);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void forceLogout() {
        tokenManager.clearTokens();
        Intent intent = new Intent(context, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}