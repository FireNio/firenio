/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.test.others;

import java.io.IOException;

import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author wangkai
 *
 */
public class TestOkHttp {

    private static Logger logger = LoggerFactory.getLogger(TestOkHttp.class);

    public static void main(String[] args) throws IOException {

        getAsynHttp();

    }

    static void test2() throws Exception {

        OkHttpClient client = new OkHttpClient();

        String url = "https://localhost:443/test";
        url = "https://www.baidu.com";

        Request request = new Request.Builder().url(url).get().build();

        Call call = client.newCall(request);

        Response response = call.execute();

        if (response.isSuccessful()) {
            String result = response.body().string();
            System.out.println(result);
        }
    }

    static void getAsynHttp() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url("https://www.aliyun.com/");
        //可以省略，默认是GET请求
        requestBuilder.method("GET", null);
        Request request = requestBuilder.build();
        Call mcall = mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    logger.info("cache---{}" + str);
                } else {
                    response.body().string();
                    String str = response.networkResponse().toString();
                    logger.info("network---{}" + str);
                }
            }
        });
    }
}
