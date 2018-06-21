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
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.container.protobase.FixedChannel;
import com.generallycloud.baseio.container.protobase.SimpleIoEventHandle;
import com.generallycloud.baseio.protocol.Future;

public class Test404 {

    public static void main(String[] args) throws Exception {

        String serviceKey = "22";
        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandle);
        context.setProtocolCodec(new ParamedProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        FixedChannel channel = new FixedChannel(connector.connect());
        ProtobaseFuture future = channel.request(serviceKey, null);
        System.out.println(future.getReadText());
        Future future1 = new ProtobaseFuture().setPing();
        channel.getChannel().flush(future1);
        CloseUtil.close(connector);
    }
    
}
