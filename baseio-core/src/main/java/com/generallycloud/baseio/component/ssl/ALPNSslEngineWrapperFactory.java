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
package com.generallycloud.baseio.component.ssl;

import javax.net.ssl.SSLEngine;

/**
 * @author wangkai
 *
 */
public class ALPNSslEngineWrapperFactory implements SslEngineWrapperFactory {

    public ALPNSslEngineWrapperFactory() {
        if (!JdkAlpnSslEngine.isAvailable()) {
            throw new RuntimeException("ALPN unsupported. Is your classpatch configured correctly?"
                    + "\n See http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-starting；"
                    + "\n http://www.cnblogs.com/gifisan/p/6245207.html");
        }
    }

    @Override
    public SSLEngine wrapSslEngine(SSLEngine engine,
            JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
        return new JdkAlpnSslEngine(engine, applicationNegotiator, isServer);
    }

}
