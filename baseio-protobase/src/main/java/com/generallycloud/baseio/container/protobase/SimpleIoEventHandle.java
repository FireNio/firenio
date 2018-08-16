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
package com.generallycloud.baseio.container.protobase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.NamedFrame;

public class SimpleIoEventHandle extends IoEventHandle {

    private Map<String, OnFrameWrapper> listeners = new HashMap<>();

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        NamedFrame f = (NamedFrame) frame;
        OnFrameWrapper onReadFrame = listeners.get(f.getFrameName());
        if (onReadFrame != null) {
            onReadFrame.onResponse(ch, f);
        }
    }

    public void listen(String serviceName, OnFrame onReadFrame) throws IOException {
        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IOException("empty service name");
        }
        OnFrameWrapper wrapper = listeners.get(serviceName);
        if (wrapper == null) {
            wrapper = new OnFrameWrapper();
            listeners.put(serviceName, wrapper);
        }
        if (onReadFrame == null) {
            return;
        }
        wrapper.setListener(onReadFrame);
    }

    public OnFrameWrapper getOnReadFrameWrapper(String serviceName) {
        return listeners.get(serviceName);
    }

    public void putOnReadFrameWrapper(String serviceName, OnFrameWrapper wrapper) {
        listeners.put(serviceName, wrapper);
    }
}
