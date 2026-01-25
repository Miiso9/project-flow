package ba.sum.fsre.projectflow.auth;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.LoginRequest;
import ba.sum.fsre.projectflow.model.RegisterRequest;
import ba.sum.fsre.projectflow.model.User;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.storage.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<Boolean> authSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileCompleted = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();

    public LiveData<Boolean> getAuthSuccess() {
        return authSuccess;
    }

    public LiveData<Boolean> getProfileCompleted() {
        return profileCompleted;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public void login(Context context, String email, String password) {

        SupabaseApi api = RetrofitClient
                .getClient(context)
                .create(SupabaseApi.class);

        api.login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TokenManager tm = new TokenManager(context);
                            tm.saveToken(response.body().accessToken);
                            tm.saveRefreshToken(response.body().refreshToken);

                            RetrofitClient.resetClient();

                            if (response.body().user != null && response.body().user.id != null) {
                                tm.saveUserId(response.body().user.id);
                                checkProfileCompletion(context, response.body().user.id);
                            } else {
                                authSuccess.postValue(true);
                                profileCompleted.postValue(true);
                            }
                        } else {
                            String errorMsg = parseError(response);
                            authError.postValue(errorMsg);
                            authSuccess.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        authError.postValue("Network connection failed: " + t.getMessage());
                        authSuccess.postValue(false);
                    }
                });
    }

    private void checkProfileCompletion(Context context, String userId) {
        SupabaseApi api = RetrofitClient
                .getClient(context)
                .create(SupabaseApi.class);

        api.getUserProfile("eq." + userId, "*")
                .enqueue(new Callback<java.util.List<User>>() {
                    @Override
                    public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            User user = response.body().get(0);
                            boolean isCompleted = user.profile_completed != null && user.profile_completed;
                            authSuccess.postValue(true);
                            profileCompleted.postValue(isCompleted);
                        } else {
                            authSuccess.postValue(true);
                            profileCompleted.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                        authSuccess.postValue(true);
                        profileCompleted.postValue(false);
                    }
                });
    }

    public void register(
            Context context,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        SupabaseApi api = RetrofitClient
                .getClient(context)
                .create(SupabaseApi.class);

        api.register(new RegisterRequest(email, password, firstName, lastName))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TokenManager tm = new TokenManager(context);
                            tm.saveToken(response.body().accessToken);
                            tm.saveRefreshToken(response.body().refreshToken);

                            if (response.body().user != null && response.body().user.id != null) {
                                tm.saveUserId(response.body().user.id);
                            }

                            RetrofitClient.resetClient();

                            authSuccess.postValue(true);
                            profileCompleted.postValue(false);
                        } else {
                            String errorMsg = parseError(response);
                            authError.postValue(errorMsg);
                            authSuccess.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        authError.postValue("Network connection failed");
                        authSuccess.postValue(false);
                    }
                });
    }

    public void logout(Context context) {
        TokenManager tm = new TokenManager(context);
        tm.clearTokens();
        authSuccess.postValue(false);
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBodyString = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorBodyString);

                if (jsonObject.has("msg")) {
                    return jsonObject.getString("msg");
                } else if (jsonObject.has("error_description")) {
                    return jsonObject.getString("error_description");
                } else if (jsonObject.has("message")) {
                    return jsonObject.getString("message");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "An unknown error occurred.";
    }
}