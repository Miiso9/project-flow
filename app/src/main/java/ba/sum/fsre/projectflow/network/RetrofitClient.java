package ba.sum.fsre.projectflow.network;

import android.content.Context;

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
                                    .build()
                    ))
                    .addInterceptor(
                            new HttpLoggingInterceptor()
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}

