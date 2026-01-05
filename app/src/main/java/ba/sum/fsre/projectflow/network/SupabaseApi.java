package ba.sum.fsre.projectflow.network;

import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.LoginRequest;
import ba.sum.fsre.projectflow.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SupabaseApi {

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);
}
