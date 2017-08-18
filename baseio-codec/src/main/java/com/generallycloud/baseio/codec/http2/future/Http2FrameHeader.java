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
package com.generallycloud.baseio.codec.http2.future;

import com.generallycloud.baseio.protocol.Future;

/**
 * <pre>
		Frame Type	Code	Section
		DATA		0x0	Section 6.1
		HEADERS		0x1	Section 6.2
		PRIORITY	0x2	Section 6.3
		RST_STREAM	0x3	Section 6.4
		SETTINGS	0x4	Section 6.5
		PUSH_PROMISE	0x5	Section 6.6
		PING		0x6	Section 6.7
		GOAWAY		0x7	Section 6.8
		WINDOW_UPDATE	0x8	Section 6.9
		CONTINUATION	0x9	Section 6.10
 * </pre>
 * 
 * @author wangkai
 *
 */

public interface Http2FrameHeader extends Future {

    public abstract byte getFlags();

    public abstract int getStreamIdentifier();

    public abstract Http2Frame getFrame();

    public abstract Http2FrameType getHttp2FrameType();
}
