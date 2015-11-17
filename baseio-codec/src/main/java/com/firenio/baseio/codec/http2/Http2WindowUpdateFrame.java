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
package com.firenio.baseio.codec.http2;

//FIXME delete
public class Http2WindowUpdateFrame extends Http2Frame {

    private int updateValue;

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_SETTINGS;
    }

    public int getUpdateValue() {
        return updateValue;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    public void setUpdateValue(int updateValue) {
        this.updateValue = updateValue;
    }

}
