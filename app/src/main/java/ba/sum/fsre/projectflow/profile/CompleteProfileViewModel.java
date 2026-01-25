package ba.sum.fsre.projectflow.profile;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ba.sum.fsre.projectflow.model.User;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompleteProfileViewModel extends ViewModel {

    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // NEW: LiveData to hold the fetched user profile
    private final MutableLiveData<User> userProfile = new MutableLiveData<>();

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // NEW: Getter for the user profile
    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public void updateProfile(Context context, String userId, String phone, String city, String dateOfBirth, String gender) {
        SupabaseApi api = RetrofitClient
                .getClient(context)
                .create(SupabaseApi.class);

        User user = new User();
        user.phone = phone;
        user.city = city;
        user.date_of_birth = dateOfBirth;
        user.gender = gender;
        user.profile_completed = true;

        api.updateUserProfile("eq." + userId, user)
                .enqueue(new Callback<java.util.List<User>>() {
                    @Override
                    public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                        if (response.isSuccessful()) {
                            updateSuccess.postValue(true);
                        } else {
                            // Try to parse the error message if possible, otherwise generic
                            updateSuccess.postValue(false);
                            errorMessage.postValue("Failed to update profile. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                        updateSuccess.postValue(false);
                        errorMessage.postValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void getCurrentUser(Context context, String userId) {
        SupabaseApi api = RetrofitClient
                .getClient(context)
                .create(SupabaseApi.class);

        api.getUserProfile("eq." + userId, "*")
                .enqueue(new Callback<java.util.List<User>>() {
                    @Override
                    public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Post the fetched user to the LiveData
                            userProfile.postValue(response.body().get(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                        errorMessage.postValue("Failed to load current profile: " + t.getMessage());
                    }
                });
    }
}