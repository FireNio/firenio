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
package com.generallycloud.sample.baseio.protobase;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestListenSimpleServlet extends ProtobaseFutureAcceptorService {

    public static final String SERVICE_NAME = TestListenSimpleServlet.class.getSimpleName();

    @Override
    protected void doAccept(SocketSession session, ProtobaseFuture future) throws Exception {

        String test = future.getReadText();

        if (StringUtil.isNullOrBlank(test)) {
            test = "test";
        }

        future.write(test);
        future.write("$");
        session.flush(future);

        for (int i = 0; i < 5; i++) {

            ProtobaseFuture f = new ProtobaseFutureImpl(session.getContext(), future.getFutureId(),
                    future.getFutureName());

            f.write(test);
            f.write("$");

            session.flush(f);
        }

    }

}
