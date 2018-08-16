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
import com.generallycloud.baseio.codec.protobase.ProtobaseFrame;
import com.generallycloud.baseio.codec.protobuf.ProtobufUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest;
import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest.Corpus;
import com.google.protobuf.ByteString;

public class TestProtobufClient {

    public static void main(String[] args) throws Exception {

        ProtobufUtil protobufUtil = new ProtobufUtil();

        protobufUtil.regist(SearchRequest.getDefaultInstance());

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {

                ProtobaseFrame f = (ProtobaseFrame) frame;

                SearchRequest res = (SearchRequest) protobufUtil.getMessage(f);

                System.out.println();
                System.out.println("________" + res);
                System.out.println();
            }
        };

        ChannelContext context = new ChannelContext(8300);

        ChannelConnector connector = new ChannelConnector(context);

        context.setIoEventHandle(eventHandleAdaptor);

        context.addChannelEventListener(new LoggerChannelOpenListener());

        //		context.addChannelEventListener(new ChannelActiveSEListener());

        //		context.setBeatFrameFactory(new FLBeatFrameFactory());

        context.setProtocolCodec(new ProtobaseCodec());

        NioSocketChannel channel = connector.connect();

        ProtobaseFrame f = new ProtobaseFrame();

        ByteString byteString = ByteString.copyFrom("222".getBytes());

        SearchRequest request = SearchRequest.newBuilder().setCorpus(Corpus.IMAGES)
                .setPageNumber(100).setQuery("test").setQueryBytes(byteString).setResultPerPage(-1)
                .build();

        protobufUtil.writeProtobuf(request, f);

        channel.flush(f);

        ThreadUtil.sleep(100);

        CloseUtil.close(connector);

    }
}
