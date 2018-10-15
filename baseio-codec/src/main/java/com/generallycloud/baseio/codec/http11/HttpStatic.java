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
package com.generallycloud.baseio.codec.http11;

/**
 * @author wangkai
 *
 */
public final class HttpStatic {

    public static final String plain               = "text/plain";
    public static final byte[] plain_bytes         = plain.getBytes();

    public static final String plain_gbk           = "text/plain;charset=gbk";
    public static final byte[] plain_gbk_bytes     = plain_gbk.getBytes();

    public static final String plain_utf8          = "text/plain;charset=utf-8";
    public static final byte[] plain_utf8_bytes    = plain_utf8.getBytes();

    public static final String server_baseio       = "baseio/0.0.1";
    public static final byte[] server_baseio_bytes = server_baseio.getBytes();

    public static final String html_utf8           = "text/html;charset=utf-8";
    public static final byte[] html_utf8_bytes     = html_utf8.getBytes();

    public static final String keep_alive          = "keep-alive";
    public static final byte[] keep_alive_bytes    = keep_alive.getBytes();

    public static final String upgrade             = "Upgrade";
    public static final byte[] upgrade_bytes       = upgrade.getBytes();

    public static final String websocket           = "WebSocket";
    public static final byte[] websocket_bytes     = websocket.getBytes();

    //    public static final String = "";
    //    public static final byte[]  _bytes = "".getBytes();

}
