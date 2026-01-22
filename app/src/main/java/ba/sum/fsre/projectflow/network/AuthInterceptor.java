package ba.sum.fsre.projectflow.network;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import ba.sum.fsre.projectflow.auth.AuthActivity;
import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.RefreshTokenRequest;
import ba.sum.fsre.projectflow.storage.TokenManager;
import okhttp3.Interceptor;
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

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String accessToken = tokenManager.getToken();

        Request request = original.newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Response response = chain.proceed(request);

        if (response.code() == 401) {
            response.close();

            boolean refreshed = refreshToken();

            if (refreshed) {
                String newToken = tokenManager.getToken();

                Request retry = original.newBuilder()
                        .addHeader("Authorization", "Bearer " + newToken)
                        .build();

                return chain.proceed(retry);
            } else {
                forceLogout();
            }
        }

        return response;
    }

    private boolean refreshToken() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi api = retrofit.create(SupabaseApi.class);

            Call<AuthResponse> call = api.refreshToken(
                    new RefreshTokenRequest(tokenManager.getRefreshToken())
            );

            retrofit2.Response<AuthResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveToken(response.body().accessToken);
                tokenManager.saveRefreshToken(response.body().refreshToken);
                RetrofitClient.resetClient();
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
