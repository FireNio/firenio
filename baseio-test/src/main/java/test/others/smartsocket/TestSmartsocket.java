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
package test.others.smartsocket;

import java.io.IOException;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.decode.Http11Request;
import org.smartboot.http.server.decode.HttpRequestProtocol;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;

import com.alibaba.fastjson.JSONObject;

/**
 * @author wangkai
 *
 */
public class TestSmartsocket {

    static byte[] body = "Hello, World!".getBytes();

    public static void main(String[] args) {
        System.setProperty("smart-socket.server.pageSize", (5 * 1024 * 1024) + "");
        System.setProperty("smart-socket.session.writeChunkSize", (1024 * 8) + "");
        HttpMessageProcessor processor = new HttpMessageProcessor(
                System.getProperty("webapps.dir", "./"));
        processor.route("/plaintext", new HttpHandle() {

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.setContentLength(body.length);
                response.setContentType("text/plain; charset=UTF-8");
                response.getOutputStream().write(body);
            }
        });
        processor.route("/json", new HttpHandle() {

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                byte[] b = JSONObject.toJSONBytes(new Message("Hello, World!"));
                response.setContentLength(b.length);
                response.setContentType("application/json");
                response.getOutputStream().write(b);
            }
        });
        http(processor);
        //        https(processor);
    }

    public static void http(MessageProcessor<Http11Request> processor) {
        // 定义服务器接受的消息类型以及各类消息对应的处理器
        AioQuickServer<Http11Request> server = new AioQuickServer<>(8080, new HttpRequestProtocol(),
                processor);
        server.setReadBufferSize(1024 * 8);
        server.setThreadNum((int) (Runtime.getRuntime().availableProcessors() * 1.5));
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Message {
        private String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
