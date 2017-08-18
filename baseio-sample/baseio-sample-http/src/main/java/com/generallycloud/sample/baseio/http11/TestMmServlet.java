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
package com.generallycloud.sample.baseio.http11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.UUIDGenerator;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.container.http11.service.HttpFutureAcceptorService;

public class TestMmServlet extends HttpFutureAcceptorService {

    private Map<String, String> mm         = new ConcurrentHashMap<>();

    private Map<String, String> mm_reverse = new ConcurrentHashMap<>();

    private String              url        = "https://www.generallycloud.com/test-mm";

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {

        if (mm.size() > 1024) {
            mm.clear();
        }

        String k = future.getRequestParam("p");

        if (StringUtil.isNullOrBlank(k)) {
            future.write("input your p :)");
            session.flush(future);
            return;
        }

        String v = mm.remove(k);

        if (StringUtil.isNullOrBlank(v)) {
            if (k.length() == 32) {
                future.write("input your p :)");
                session.flush(future);
                return;
            }
            String rk = mm_reverse.get(k);
            if (StringUtil.isNullOrBlank(rk)) {
                rk = UUIDGenerator.random();
                mm.put(rk, k);
                mm_reverse.put(k, rk);
            }
            future.write(url + "?p=" + rk);
        } else {
            mm_reverse.remove(v);
            future.write(v);
        }

        session.flush(future);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        super.initialize(context, config);

        String url = config.getParameter("url");

        if (StringUtil.isNullOrBlank(url)) {
            return;
        }

        this.url = url;
    }

}
