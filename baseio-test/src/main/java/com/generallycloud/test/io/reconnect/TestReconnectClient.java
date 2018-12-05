/*
 * Copyright 2015 The Baseio Project
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
package com.generallycloud.test.io.reconnect;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.ReConnector;
import com.generallycloud.baseio.protocol.Frame;

public class TestReconnectClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {

            }
        };

        ChannelConnector context = new ChannelConnector(8300);

        ReConnector connector = new ReConnector(context);

        connector.setRetryTime(5000);

        context.setIoEventHandle(eventHandleAdaptor);

        context.addChannelEventListener(new LoggerChannelOpenListener());

        context.setProtocolCodec(new FixedLengthCodec());

        //		context.addChannelEventListener(new CloseConnectorSEListener(connector.getRealConnector()));

        connector.connect();

        int count = 99999;
        for (int i = 0; ; i++) {
            Util.sleep(1000);
            if (i > count) {
                break;
            }
        }

        Util.close(connector);
    }
}
