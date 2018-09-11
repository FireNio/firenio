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
package com.generallycloud.baseio.container.http11;

import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.codec.http11.HttpStatus;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.DefaultOnRedeployAcceptor;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class HttpOnRedeployAcceptor extends DefaultOnRedeployAcceptor {

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        if (frame instanceof HttpFrame) {
            HttpFrame hf = (HttpFrame) frame;
            hf.setStatus(HttpStatus.C503);
        }
        super.accept(ch, frame);
    }

}
