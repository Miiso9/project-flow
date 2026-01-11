package ba.sum.fsre.projectflow.network;

import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.LoginRequest;
import ba.sum.fsre.projectflow.model.RegisterRequest;
import ba.sum.fsre.projectflow.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @GET("rest/v1/users")
    Call<java.util.List<User>> getUserProfile(@Query("id") String filter, @Query("select") String select);

    @PATCH("rest/v1/users")
    Call<java.util.List<User>> updateUserProfile(@Query("id") String filter, @Body User user);
}
