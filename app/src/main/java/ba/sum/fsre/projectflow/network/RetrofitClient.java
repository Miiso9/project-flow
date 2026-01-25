package ba.sum.fsre.projectflow.network;

import android.content.Context;

import ba.sum.fsre.projectflow.BuildConfig;
import ba.sum.fsre.projectflow.storage.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static Context appContext;

    static String url = BuildConfig.SUPABASE_URL;
    static String key = BuildConfig.SUPABASE_ANON_KEY;

    public static void resetClient() {
        retrofit = null;
    }

    public static Retrofit getClient(Context context) {
        appContext = context.getApplicationContext();

        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("apikey", key)
                                    .addHeader("Prefer", "return=representation")
                                    .build()
                    ))
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(
                            new HttpLoggingInterceptor()
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(url + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}