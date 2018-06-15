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
package com.generallycloud.test.io.protobuf;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobuf.ProtobufUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest;

public class TestProtobufServer {

    public static void main(String[] args) throws Exception {

        ProtobufUtil protobufUtil = new ProtobufUtil();

        protobufUtil.regist(SearchRequest.getDefaultInstance());

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {

                ProtobaseFuture f = (ProtobaseFuture) future;

                SearchRequest req = (SearchRequest) protobufUtil.getMessage(f);

                String message = "yes server already accept your message:\n" + req;

                System.out.println(message);

                SearchRequest res = SearchRequest.newBuilder().mergeFrom(req)
                        .setQuery("query_______").build();

                protobufUtil.writeProtobuf(res.getClass().getName(), res, f);

                channel.flush(future);
            }
        };

        ChannelContext context = new ChannelContext(new Configuration(8300));

        ChannelAcceptor acceptor = new ChannelAcceptor(context);

        context.addChannelEventListener(new LoggerChannelOpenListener());

        //		context.addChannelEventListener(new ChannelAliveSEListener());

        context.setIoEventHandle(eventHandleAdaptor);

        //		context.setBeatFutureFactory(new NIOBeatFutureFactory());

        context.setProtocolCodec(new ProtobaseCodec());

        acceptor.bind();
    }
}
