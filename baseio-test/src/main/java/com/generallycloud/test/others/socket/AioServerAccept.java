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
package com.generallycloud.test.others.socket;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class AioServerAccept {

    private static Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        //		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(4));
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress("0.0.0.0", 8013));

        for (;;) {

            Future<AsynchronousSocketChannel> f = server.accept();

            AsynchronousSocketChannel channel = f.get();

            DebugUtil.debug("client:" + channel.toString());

        }
        //		group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

}
