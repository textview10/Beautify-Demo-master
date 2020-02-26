package com.megvii.beautify.model.http;

import com.megvii.beautify.app.Constant;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by xiejiantao on 2017/7/18.
 */

public class RetrofitHelper {

    public static Retrofit retrofit = null;
    public static OkHttpClient client = null;

    /**
     * 获取接口服务实例
     *
     * @return
     */
    public static <T> T getAPIService(Class<T> clazz) {
        if (retrofit == null) {
            synchronized (RetrofitHelper.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .client(new OkHttpClient())
                            .baseUrl(Constant.BASE_URL)
                            .build();
                }
            }
        }
        return retrofit.create(clazz);
    }

}
