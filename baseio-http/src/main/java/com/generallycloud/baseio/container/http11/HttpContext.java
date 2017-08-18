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
package com.generallycloud.baseio.container.http11;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.codec.http11.future.WebSocketSEListener;
import com.generallycloud.baseio.container.AbstractPluginContext;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;

public class HttpContext extends AbstractPluginContext {

    private static HttpContext instance           = null;

    private HttpSessionManager httpSessionManager = null;

    public static HttpContext getInstance() {
        return instance;
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {

        if (enableHttpSession(config)) {
            LifeCycleUtil.stop(httpSessionManager);
        }

        super.destroy(context, config);
    }

    public HttpSessionManager getHttpSessionManager() {
        return httpSessionManager;
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        super.initialize(context, config);

        instance = this;

        if (enableHttpSession(config)) {
            httpSessionManager = new DefaultHttpSessionManager();
            httpSessionManager.startup("HTTPSession-Manager");
        } else {
            httpSessionManager = new FakeHttpSessionManager();
        }

        context.getChannelContext().addSessionEventListener(new WebSocketSEListener());
    }

    private boolean enableHttpSession(Configuration config) {
        return config.getBooleanParameter("enable-http-session");
    }

}
