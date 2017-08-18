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

/**
 * <pre>
Name			Code	Initial Value
HEADER_TABLE_SIZE	0x1	4096
ENABLE_PUSH		0x2	1
MAX_CONCURRENT_STREAMS	0x3	(infinite)
INITIAL_WINDOW_SIZE	0x4	65535
MAX_FRAME_SIZE		0x5	16384
MAX_HEADER_LIST_SIZE	0x6	(infinite)
 * </pre>
 * 
 * @author wangkai
 *
 */
public interface Http2SettingsFrame extends Http2Frame {

    public static final int SETTINGS_HEADER_TABLE_SIZE      = 0x1;
    public static final int SETTINGS_ENABLE_PUSH            = 0x2;
    public static final int SETTINGS_MAX_CONCURRENT_STREAMS = 0x3;
    public static final int SETTINGS_INITIAL_WINDOW_SIZE    = 0x4;
    public static final int SETTINGS_MAX_FRAME_SIZE         = 0x5;
    public static final int SETTINGS_MAX_HEADER_LIST_SIZE   = 0x6;

    public abstract long[] getSettings();

}
