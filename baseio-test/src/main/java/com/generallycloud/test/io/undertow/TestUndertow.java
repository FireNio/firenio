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
package com.generallycloud.test.io.undertow;

import com.generallycloud.baseio.common.Encoding;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * @author wangkai
 *
 */
public class TestUndertow {

    public static void main(final String[] args) {
        FormParserFactory.Builder builder = FormParserFactory.builder();
        builder.setDefaultCharset(Encoding.UTF8.name());
        FormParserFactory formParserFactory = builder.build();
        Undertow server = Undertow.builder().addHttpListener(8087, "0.0.0.0")
                .setHandler(new HttpHandler() { //设置HttpHandler回调方法  

                    private boolean hasBody(HttpServerExchange exchange) {
                        int length = (int) exchange.getRequestContentLength();
                        if (length == 0) {
                            return false; // if body is empty, skip reading
                        }

                        HttpString method = exchange.getRequestMethod();
                        return Methods.POST.equals(method) || Methods.PUT.equals(method)
                                || Methods.PATCH.equals(method);
                    }

                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        if (hasBody(exchange)) { // parse body early, not process until body is read (e.g. for chunked), to save one blocking thread during read
                            FormDataParser parser = formParserFactory.createParser(exchange);
                            if (parser == null) {
                                return;
                            }
                            FormData data = parser.parseBlocking();
                            System.out.println(data);
                        }
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Hello World");
                    }
                }).setIoThreads(2).build();
        server.start();
    }

}
