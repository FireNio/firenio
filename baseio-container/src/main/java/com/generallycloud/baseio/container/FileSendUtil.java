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
package com.generallycloud.baseio.container;

import java.io.File;
import java.io.FileInputStream;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.component.SocketSession;

public class FileSendUtil {

    public void sendFile(SocketSession session, String serviceName, File file, int cacheSize)
            throws Exception {

        FileInputStream inputStream = new FileInputStream(file);

        int available = inputStream.available();

        int time = (available + cacheSize) / cacheSize - 1;

        byte[] cache = new byte[cacheSize];

        JSONObject json = new JSONObject();
        json.put(FileReceiveUtil.FILE_NAME, file.getName());
        json.put(FileReceiveUtil.IS_END, false);

        String jsonString = json.toJSONString();

        for (int i = 0; i < time; i++) {

            FileUtil.readInputStream(inputStream, cache);

            ProtobaseFuture f = new ProtobaseFutureImpl(session.getContext(), serviceName);

            f.write(jsonString);

            f.writeBinary(cache);

            session.flush(f);
        }

        int r = FileUtil.readInputStream(inputStream, cache);

        json.put(FileReceiveUtil.IS_END, true);

        ProtobaseFuture f = new ProtobaseFutureImpl(session.getContext(), serviceName);

        f.write(json.toJSONString());

        f.writeBinary(cache, 0, r);

        session.flush(f);

        CloseUtil.close(inputStream);
    }
}
