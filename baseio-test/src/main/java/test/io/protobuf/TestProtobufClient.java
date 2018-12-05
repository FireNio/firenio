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
package test.io.protobuf;

import com.firenio.baseio.codec.protobase.ProtobaseCodec;
import com.firenio.baseio.codec.protobase.ProtobaseFrame;
import com.firenio.baseio.codec.protobuf.ProtobufUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.protocol.Frame;
import com.google.protobuf.ByteString;

import test.io.protobuf.TestProtoBufBean.SearchRequest;
import test.io.protobuf.TestProtoBufBean.SearchRequest.Corpus;

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

        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new ProtobaseCodec());
        NioSocketChannel channel = context.connect();
        ProtobaseFrame f = new ProtobaseFrame();
        ByteString byteString = ByteString.copyFrom("222".getBytes());
        SearchRequest request = SearchRequest.newBuilder().setCorpus(Corpus.IMAGES)
                .setPageNumber(100).setQuery("test").setQueryBytes(byteString).setResultPerPage(-1)
                .build();
        protobufUtil.writeProtobuf(request, f);
        channel.flush(f);
        Util.sleep(100);
        Util.close(context);
    }
    
}
