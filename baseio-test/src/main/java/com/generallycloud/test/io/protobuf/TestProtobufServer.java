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

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobuf.ProtobufUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest;

public class TestProtobufServer {

    public static void main(String[] args) throws Exception {

        ProtobufUtil protobufUtil = new ProtobufUtil();

        protobufUtil.regist(SearchRequest.getDefaultInstance());

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {

                ProtobaseFuture f = (ProtobaseFuture) future;

                SearchRequest req = (SearchRequest) protobufUtil.getMessage(f);

                String message = "yes server already accept your message:\n" + req;

                System.out.println(message);

                SearchRequest res = SearchRequest.newBuilder().mergeFrom(req)
                        .setQuery("query_______").build();

                protobufUtil.writeProtobuf(res.getClass().getName(), res, f);

                session.flush(future);
            }
        };

        SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));

        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

        context.addSessionEventListener(new LoggerSocketSEListener());

        //		context.addSessionEventListener(new SessionAliveSEListener());

        context.setIoEventHandleAdaptor(eventHandleAdaptor);

        //		context.setBeatFutureFactory(new NIOBeatFutureFactory());

        context.setProtocolFactory(new ProtobaseProtocolFactory());

        acceptor.bind();
    }
}
