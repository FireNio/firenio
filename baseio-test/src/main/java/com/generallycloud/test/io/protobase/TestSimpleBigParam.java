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

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.container.protobase.FixedChannel;
import com.generallycloud.baseio.container.protobase.SimpleIoEventHandle;

public class TestSimpleBigParam {

    public static void main(String[] args) throws Exception {

        String serviceKey = "TestSimpleServlet";
        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandle);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        FixedChannel channel = new FixedChannel(connector.connect());
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < 600000; i++) {
            params.put(String.valueOf(i), "网易科技腾讯科技阿里巴巴");
        }
        ParamedProtobaseFuture future = channel.request(serviceKey, params);
        FileUtil.writeByCls(TestSimpleBigParam.class.getName(), future.getReadText());
        System.out.println("处理完成");
        CloseUtil.close(connector);
    }

}
