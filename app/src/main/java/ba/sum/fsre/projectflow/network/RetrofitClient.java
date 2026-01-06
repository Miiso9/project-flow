package ba.sum.fsre.projectflow.network;

import android.content.Context;

import ba.sum.fsre.projectflow.storage.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {

        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("apikey", Constants.ANON_KEY)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=representation")
                                    .build()
                    ))
                    .addInterceptor(new AuthInterceptor(new TokenManager(context)))
                    .addInterceptor(
                            new HttpLoggingInterceptor()
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}

