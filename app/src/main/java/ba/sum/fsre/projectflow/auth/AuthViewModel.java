package ba.sum.fsre.projectflow.auth;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public LiveData<Boolean> getAuthSuccess() {
        return authSuccess;
    }

    public LiveData<Boolean> getProfileCompleted() {
        return profileCompleted;
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
                            
                            if (response.body().user != null && response.body().user.id != null) {
                                tm.saveUserId(response.body().user.id);
                                checkProfileCompletion(context, response.body().user.id);
                            } else {
                                authSuccess.postValue(true);
                                profileCompleted.postValue(true);
                            }
                        } else {
                            authSuccess.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
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
                            authSuccess.postValue(true);
                        } else {
                            authSuccess.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        authSuccess.postValue(false);
                    }
                });
    }

    public void logout(Context context) {
        TokenManager tm = new TokenManager(context);
        tm.clearTokens();
        authSuccess.postValue(false);
    }
}
