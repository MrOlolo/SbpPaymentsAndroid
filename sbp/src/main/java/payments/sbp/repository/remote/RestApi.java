package payments.sbp.repository.remote;

import androidx.annotation.NonNull;

import payments.sbp.repository.remote.response.NspkResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RestApi {
    @GET("/.well-known/assetlinks.json")
    @NonNull
    Call<NspkResponse.Data[]> loadBankAppsAssets();
    @GET("/proxyapp/c2bmembers.json")
    @NonNull
    Call<NspkResponse> loadBankAppsDictionary();

}
