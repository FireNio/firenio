/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.http2;

//FIXME delete
public class Http2SettingsFrame extends Http2Frame {

    public static final int SETTINGS_ENABLE_PUSH            = 0x2;
    public static final int SETTINGS_HEADER_TABLE_SIZE      = 0x1;
    public static final int SETTINGS_INITIAL_WINDOW_SIZE    = 0x4;
    public static final int SETTINGS_MAX_CONCURRENT_STREAMS = 0x3;
    public static final int SETTINGS_MAX_FRAME_SIZE         = 0x5;
    public static final int SETTINGS_MAX_HEADER_LIST_SIZE   = 0x6;

    private long[] settings;

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_SETTINGS;
    }

    public long[] getSettings() {
        return settings;
    }

    public void setSettings(long[] settings) {
        this.settings = settings;
    }

}
