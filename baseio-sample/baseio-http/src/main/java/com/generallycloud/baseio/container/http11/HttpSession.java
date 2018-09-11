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

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public interface HttpSession {

    void active(NioSocketChannel ioSession);

    void flush(Frame frame) throws IOException;

    long getCreateTime();

    NioSocketChannel getChannel();

    long getLastAccessTime();

    String getSessionId();

    boolean isValidate();

    Charset getEncoding();

    HttpFrameAcceptor getContext();

}
