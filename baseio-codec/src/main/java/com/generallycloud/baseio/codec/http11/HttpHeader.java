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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class HttpHeader {

    //指定客户端能够接收的内容类型  Accept: text/plain, text/html
    public static final String Low_Accept               = "accept";
    //浏览器可以接受的字符编码集。  Accept-Charset: iso-8859-5
    public static final String Low_Accept_Charset       = "accept-charset";
    //指定浏览器可以支持的web服务器返回内容压缩编码类型。 Accept-Encoding: compress, gzip
    public static final String Low_Accept_Encoding      = "accept-encoding";
    //浏览器可接受的语言   Accept-Language: en,zh
    public static final String Low_Accept_Language      = "accept-language";
    //可以请求网页实体的一个或者多个子范围字段    Accept-Ranges: bytes
    public static final String Low_Accept_Ranges        = "accept-ranges";
    //HTTP授权的授权证书 Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    public static final String Low_Authorization        = "authorization";
    //指定请求和响应遵循的缓存机制  Cache-Control: no-cache
    public static final String Low_Cache_Control        = "cache-control";
    //表示是否需要持久连接。（HTTP 1.1默认进行持久连接）   Connection: close
    public static final String Low_Connection           = "connection";
    //HTTP请求发送时，会把保存在该请求域名下的所有cookie值一起发送给web服务器。 Cookie: $Version=1; Skin=new;
    public static final String Low_Cookie               = "cookie";
    //请求的内容长度 Content-Length: 348
    public static final String Low_Content_Length       = "content-length";
    //请求的与实体对应的MIME信息 Content-Type: application/x-www-form-urlencoded
    public static final String Low_Content_Type         = "content-type";
    //请求发送的日期和时间  Date: Tue, 15 Nov 2010 08:12:31 GMT
    public static final String Low_Date                 = "date";
    //请求的特定的服务器行为 Expect: 100-continue
    public static final String Low_Expect               = "expect";
    //发出请求的用户的Email   From: user@email.com
    public static final String Low_From                 = "from";
    //指定请求的服务器的域名和端口号 Host: www.domain.com
    public static final String Low_Host                 = "host";
    //只有请求内容与实体相匹配才有效 If-Match: “737060cd8c284d8af7ad3082f209582d”
    public static final String Low_If_Match             = "if-match";
    //如果请求的部分在指定时间之后被修改则请求成功，未被修改则返回304代码 If-Modified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    public static final String Low_If_Modified_Since    = "if-modified-since";
    //如果内容未改变返回304代码，参数为服务器先前发送的Etag，与服务器回应的Etag比较判断是否改变  If-None-Match: “737060cd8c284d8af7ad3082f209582d”
    public static final String Low_If_None_Match        = "if-none-match";
    //如果实体未改变，服务器发送客户端丢失的部分，否则发送整个实体。参数也为Etag If-Range: “737060cd8c284d8af7ad3082f209582d”
    public static final String Low_If_Range             = "if-range";
    //只在实体在指定时间之后未被修改才请求成功    If-Unmodified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    public static final String Low_If_Unmodified_Since  = "if-unmodified-since";
    //限制信息通过代理和网关传送的时间    Max-Forwards: 10
    public static final String Low_Max_Forwards         = "max-forwards";
    //用来包含实现特定的指令 Pragma: no-cache
    public static final String Low_Pragma               = "pragma";
    //连接到代理的授权证书  Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    public static final String Low_Proxy_Authorization  = "proxy-authorization";
    //只请求实体的一部分，指定范围  Range: bytes=500-999
    public static final String Low_Range                = "range";
    //先前网页的地址，当前请求网页紧随其后,即来路  Referer: http://www.domain.com/archives/71.html
    public static final String Low_Referer              = "referer";
    //客户端愿意接受的传输编码，并通知服务器接受接受尾加头信息    TE: trailers,deflate;q=0.5
    public static final String Low_TE                   = "te";
    //向服务器指定某种传输协议以便服务器进行转换（如果支持） Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11
    public static final String Low_Upgrade              = "upgrade";
    //User-Agent的内容包含发出请求的用户信息    User-Agent: Mozilla/5.0 (Linux; X11)
    public static final String Low_User_Agent           = "user-agent";
    //通知中间网关或代理服务器地址，通信协议 Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
    public static final String Low_Via                  = "via";
    //关于消息实体的警告信息 Warn: 199 Miscellaneous warning
    public static final String Low_Warning              = "warning";
    public static final String Low_Sec_WebSocket_Key    = "sec-websocket-key";
    //表明服务器是否支持指定范围请求及哪种类型的分段请求   Accept-Ranges: bytes
    public static final String Accept_Ranges            = "Accept-Ranges";
    public static final byte[] Accept_Ranges_Bytes      = "Accept-Ranges".getBytes();
    //从原始服务器到代理缓存形成的估算时间（以秒计，非负）  Age: 12
    public static final String Age                      = "Age";
    public static final byte[] Age_Bytes                = "Age".getBytes();
    //对某网络资源的有效的请求行为，不允许则返回405    Allow: GET, HEAD
    public static final String Allow                    = "Allow";
    public static final byte[] Allow_Bytes              = "Allow".getBytes();
    //告诉所有的缓存机制是否可以缓存及哪种类型    Cache-Control: no-cache
    public static final String Cache_Control            = "Cache-Control";
    public static final byte[] Cache_Control_Bytes      = "Cache-Control".getBytes();
    //表示是否需要持久连接。（HTTP 1.1默认进行持久连接）   Connection: close
    public static final String Connection               = "Connection";
    public static final byte[] Connection_Bytes         = "Connection".getBytes();
    //web服务器支持的返回内容压缩编码类型。    Content-Encoding: gzip
    public static final String Content_Encoding         = "Content-Encoding";
    public static final byte[] Content_Encoding_Bytes   = "Content-Encoding".getBytes();
    //响应体的语言  Content-Language: en,zh
    public static final String Content_Language         = "Content-Language";
    public static final byte[] Content_Language_Bytes   = "Content-Language".getBytes();
    //响应体的长度  Content-Length: 348
    public static final String Content_Length           = "Content-Length";
    public static final byte[] Content_Length_Bytes     = "Content-Length".getBytes();
    //请求资源可替代的备用的另一地址 Content-Location: /index.htm
    public static final String Content_Location         = "Content-Location";
    public static final byte[] Content_Location_Bytes   = "Content-Location".getBytes();
    //返回资源的MD5校验值 Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
    public static final String Content_MD5              = "Content-MD5";
    public static final byte[] Content_MD5_Bytes        = "Content-MD5".getBytes();
    //在整个返回体中本部分的字节位置 Content-Range: bytes 21010-47021/47022
    public static final String Content_Range            = "Content-Range";
    public static final byte[] Content_Range_Bytes      = "Content-Range".getBytes();
    //返回内容的MIME类型 Content-Type: text/html; charset=utf-8
    public static final String Content_Type             = "Content-Type";
    public static final byte[] Content_Type_Bytes       = "Content-Type".getBytes();
    //原始服务器消息发出的时间    Date: Tue, 15 Nov 2010 08:12:31 GMT
    public static final String Date                     = "Date";
    public static final byte[] Date_Bytes               = "Date".getBytes();
    //请求变量的实体标签的当前值   ETag: “737060cd8c284d8af7ad3082f209582d”
    public static final String ETag                     = "ETag";
    public static final byte[] ETag_Bytes               = "ETag".getBytes();
    //响应过期的日期和时间  Expires: Thu, 01 Dec 2010 16:00:00 GMT
    public static final String Expires                  = "Expires";
    public static final byte[] Expires_Bytes            = "Expires".getBytes();
    //请求资源的最后修改时间 Last-Modified: Tue, 15 Nov 2010 12:45:26 GMT
    public static final String Last_Modified            = "Last-Modified";
    public static final byte[] Last_Modified_Bytes      = "Last-Modified".getBytes();
    //用来重定向接收方到非请求URL的位置来完成请求或标识新的资源  Location: http://www.domain.com/archives/94.html
    public static final String Location                 = "Location";
    public static final byte[] Location_Bytes           = "Location".getBytes();
    //包括实现特定的指令，它可应用到响应链上的任何接收方   Pragma: no-cache
    public static final String Pragma                   = "Pragma";
    public static final byte[] Pragma_Bytes             = "Pragma".getBytes();
    //它指出认证方案和可应用到代理的该URL上的参数 Proxy-Authenticate: Basic
    public static final String Proxy_Authenticate       = "Proxy-Authenticate";
    public static final byte[] Proxy_Authenticate_Bytes = "Proxy-Authenticate".getBytes();
    //应用于重定向或一个新的资源被创造，在5秒之后重定向（由网景提出，被大部分浏览器支持）  Refresh: 5; url=http://www.domain.com/archives/94.html
    public static final String Refresh                  = "Refresh";
    public static final byte[] Refresh_Bytes            = "Refresh".getBytes();
    //如果实体暂时不可取，通知客户端在指定时间之后再次尝试  Retry-After: 120
    public static final String Retry_After              = "Retry-After";
    public static final byte[] Retry_After_Bytes        = "Retry-After".getBytes();
    //web服务器软件名称  Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
    public static final String Server                   = "Server";
    public static final byte[] Server_Bytes             = "Server".getBytes();
    //设置Http Cookie   Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
    public static final String Set_Cookie               = "Set-Cookie";
    public static final byte[] Set_Cookie_Bytes         = "Set-Cookie".getBytes();
    //指出头域在分块传输编码的尾部存在    Trailer: Max-Forwards
    public static final String Trailer                  = "Trailer";
    public static final byte[] Trailer_Bytes            = "Trailer".getBytes();
    //文件传输编码  Transfer-Encoding:chunked
    public static final String Transfer_Encoding        = "Transfer-Encoding";
    public static final byte[] Transfer_Encoding_Bytes  = "Transfer-Encoding".getBytes();
    //告诉下游代理是使用缓存响应还是从原始服务器请求 Vary: *
    //向服务器指定某种传输协议以便服务器进行转换（如果支持） Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11
    public static final String              Upgrade                    = "Upgrade";
    public static final byte[]              Upgrade_Bytes              = "Upgrade".getBytes();
    public static final String              Vary                       = "Vary";
    public static final byte[]              Vary_Bytes                 = "Vary".getBytes();
    //告知代理客户端响应是通过哪里发送的   Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
    public static final String              Via                        = "Via";
    public static final byte[]              Via_Bytes                  = "Via".getBytes();
    //警告实体可能存在的问题 Warning: 199 Miscellaneous warning
    public static final String              Warning                    = "Warning";
    public static final byte[]              Warning_Bytes              = "Warning".getBytes();
    public static final String              Sec_WebSocket_Accept       = "Sec-WebSocket-Accept";
    public static final byte[]              Sec_WebSocket_Accept_Bytes = "Sec-WebSocket-Accept"
            .getBytes();
    //表明客户端请求实体应该使用的授权方案  WWW-Authenticate: Basic
    public static final String              WWW_Authenticate           = "WWW-Authenticate";
    public static final byte[]              WWW_Authenticate_Bytes     = "WWW-Authenticate"
            .getBytes();

    public static final Map<String, String> LOW_MAPPING                = new HashMap<>();

    public static final Map<String, byte[]> LOW_MAPPING_BYTES          = new HashMap<>();

    static {
        try {
            Field[] fs = HttpHeader.class.getDeclaredFields();
            for (Field f : fs) {
                if (f.getType() == String.class) {
                    String name = f.getName();
                    if (name.startsWith("Low_")) {
                        String key = name.substring(4).replace("_", "-");
                        String value = (String) f.get(null);
                        LOW_MAPPING.put(key, value);
                        LOW_MAPPING.put(key.toLowerCase(), value);
                        LOW_MAPPING_BYTES.put(key, value.getBytes());
                        LOW_MAPPING_BYTES.put(key.toLowerCase(), value.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getBytes(String header) {
        return LOW_MAPPING_BYTES.get(header);
    }

}
