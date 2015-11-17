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
package com.firenio.baseio.codec.http11;

import static com.firenio.baseio.codec.http11.HttpHeader.Connection;
import static com.firenio.baseio.codec.http11.HttpHeader.Content_Type;
import static com.firenio.baseio.codec.http11.HttpHeader.Sec_WebSocket_Accept;
import static com.firenio.baseio.codec.http11.HttpHeader.Sec_WebSocket_Key;
import static com.firenio.baseio.codec.http11.HttpHeader.Upgrade;
import static com.firenio.baseio.codec.http11.HttpStatic.upgrade_bytes;
import static com.firenio.baseio.codec.http11.HttpStatic.websocket_bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.collection.IntMap;
import com.firenio.baseio.common.BASE64Util;
import com.firenio.baseio.common.SHAUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class HttpFrame extends HttpFrameLite {

    private List<Cookie>        cookieList;
    private Map<String, String> cookies;
    private boolean             isForm;
    private IntMap<String>      request_headers = new IntMap<>(16);
    private int                 version;

    public void addCookie(Cookie cookie) {
        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }
        cookieList.add(cookie);
    }

    public String getBoundary() {
        if (isForm) {
            return HttpCodec.parseBoundary(getRequestHeader(Content_Type.getId()));
        }
        return null;
    }

    public String getCookie(String name) {
        if (cookies == null) {
            return null;
        }
        return cookies.get(name);
    }

    public List<Cookie> getCookieList() {
        return cookieList;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    HttpHeader getHeader(String name) {
        HttpHeader header = HttpHeader.ALL.get(name);
        if (header == null) {
            return HttpHeader.ALL.get(name.toLowerCase());
        }
        return header;
    }

    public String getHost() {
        return getRequestHeader(HttpHeader.Host);
    }

    public String getRequestHeader(HttpHeader name) {
        return request_headers.get(name.getId());
    }

    public String getRequestHeader(int name) {
        return request_headers.get(name);
    }

    public IntMap<String> getRequestHeaders() {
        return request_headers;
    }

    public HttpVersion getVersion() {
        return HttpVersion.getMethod(version);
    }

    public int getVersionId() {
        return version;
    }

    public boolean isForm() {
        return isForm;
    }

    @Override
    public HttpFrame reset() {
        this.version = HttpVersion.OTHER.getId();
        this.isForm = false;
        this.clear(cookieList);
        this.clear(cookies);
        this.request_headers.clear();
        super.reset();
        return this;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    void setReadHeader(String name, String value) {
        HttpHeader header = getHeader(name);
        if (header != null) {
            request_headers.put(header.getId(), value);
        }
    }

    public void setRequestHeader(HttpHeader header, String value) {
        this.request_headers.put(header.getId(), value);
    }

    public void setRequestHeaders(IntMap<String> requestHeaders) {
        this.request_headers = requestHeaders;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean updateWebSocketProtocol(final Channel ch) throws Exception {
        String Sec_WebSocket_Key_Value = getRequestHeader(Sec_WebSocket_Key);
        if (!Util.isNullOrBlank(Sec_WebSocket_Key_Value)) {
            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key_Value
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = SHAUtil.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = BASE64Util.byteArrayToBase64(key_array);
            setStatus(HttpStatus.C101);
            setResponseHeader(Connection, upgrade_bytes);
            setResponseHeader(Upgrade, websocket_bytes);
            setResponseHeader(Sec_WebSocket_Accept, acceptKey.getBytes());
            ch.setAttribute(WebSocketCodec.CH_KEY_FRAME_NAME, getFrameName());
            ByteBuf buf = ch.encode(this);
            ch.setCodec(WebSocketCodec.PROTOCOL_ID);
            ch.writeAndFlush(buf);
            return true;
        }
        return false;
    }

}
