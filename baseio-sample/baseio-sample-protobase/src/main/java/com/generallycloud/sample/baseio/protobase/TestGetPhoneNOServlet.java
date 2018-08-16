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

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.ProtobaseFrameAcceptorService;

public class TestGetPhoneNOServlet extends ProtobaseFrameAcceptorService {

    public static final String SERVICE_NAME = TestGetPhoneNOServlet.class.getSimpleName();

    private String[]           NOS          = { "13811112222", "18599991111", "18599991111",
            "13811112222" };

    private int                index        = 0;

    @Override
    protected void doAccept(NioSocketChannel ch, ParamedProtobaseFrame frame) throws Exception {
        String phone = NOS[index++];
        if (index == 4) {
            index = 0;
        }
        frame.put("phone", phone);
        ch.flush(frame);
    }

}
