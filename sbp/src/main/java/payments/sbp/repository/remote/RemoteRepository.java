package payments.sbp.repository.remote;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import payments.sbp.BuildConfig;
import payments.sbp.repository.Repository;
import payments.sbp.repository.remote.response.NspkResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RemoteRepository implements Repository {
    private static RemoteRepository instance;

    public static RemoteRepository getInstance() {
        if (instance == null) {
            instance = new RemoteRepository();
        }
        return instance;
    }

    private RestApi api;

    private RemoteRepository() {
        api = prepareRestApi();
    }

    private RestApi prepareRestApi() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
//TODO: add caching here
        Gson gson = new GsonBuilder()
                .setLenient()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();


        return new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(BuildConfig.NSPK_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(RestApi.class);
    }

    @Override
    public void loadAppPackages(PackagesLoadListener listener) {
        Call<NspkResponse.Data[]> call = getInstance().api.loadBankAppsAssets();
        call.enqueue(new Callback<NspkResponse.Data[]>() {
            @Override
            public void onResponse(Call<NspkResponse.Data[]> call, Response<NspkResponse.Data[]> response) {
                if (listener != null && response != null && response.body() != null) {
                    List<String> result = new ArrayList<>();
                    for (NspkResponse.Data data : response.body()) {
                        result.add(data.target.packageName);
                    }
                    if (!result.isEmpty())
                        listener.onPackages(result);
                }
            }

            @Override
            public void onFailure(Call<NspkResponse.Data[]> call, Throwable t) {
//something went wrong: will be used default packages list
            }
        });
    }
}
