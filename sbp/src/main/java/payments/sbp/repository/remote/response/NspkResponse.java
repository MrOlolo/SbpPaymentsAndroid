package payments.sbp.repository.remote.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class NspkResponse {
    public class AppInfo {
        @Nullable
        public String comment;
        @Nullable
        @SerializedName("package_name")
       public String packageName;

    }

    public class Data {
        public AppInfo target;
    }
    public class AppSchema{
        @Nullable
        public String bankName;
        @Nullable
        public String logoURL;
    }
    AppSchema[] dictionary;
}
