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
package com.generallycloud.baseio.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.NamedFuture;

public class SimpleIoEventHandle extends IoEventHandleAdaptor {

    private Map<String, OnFutureWrapper> listeners = new HashMap<>();

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        NamedFuture f = (NamedFuture) future;

        OnFutureWrapper onReadFuture = listeners.get(f.getFutureName());

        if (onReadFuture != null) {
            onReadFuture.onResponse(session, f);
        }
    }

    public void listen(String serviceName, OnFuture onReadFuture) throws IOException {

        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IOException("empty service name");
        }

        OnFutureWrapper wrapper = listeners.get(serviceName);

        if (wrapper == null) {

            wrapper = new OnFutureWrapper();

            listeners.put(serviceName, wrapper);
        }

        if (onReadFuture == null) {
            return;
        }

        wrapper.setListener(onReadFuture);
    }

    public OnFutureWrapper getOnReadFutureWrapper(String serviceName) {
        return listeners.get(serviceName);
    }

    public void putOnReadFutureWrapper(String serviceName, OnFutureWrapper wrapper) {
        listeners.put(serviceName, wrapper);
    }
}
