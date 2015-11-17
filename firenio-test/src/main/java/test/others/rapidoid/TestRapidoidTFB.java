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
package test.others.rapidoid;

import org.rapidoid.buffer.Buf;
import org.rapidoid.config.Conf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.HttpUtils;
import org.rapidoid.http.MediaType;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;
import org.rapidoid.setup.App;

/**
 * @author wangkai
 *
 */
public class TestRapidoidTFB extends AbstractHttpServer {

    private static final byte[] HELLO_WORLD   = "Hello, World!".getBytes();

    private static final byte[] URI_JSON      = "/json".getBytes();

    private static final byte[] URI_PLAINTEXT = "/plaintext".getBytes();

    public TestRapidoidTFB() {
        super("X", "", "", false);
    }

    @Override
    protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper data) {

        if (data.isGet.value) {
            if (matches(buf, data.path, URI_PLAINTEXT)) {
                return ok(ctx, true, HELLO_WORLD, MediaType.TEXT_PLAIN);

            } else if (matches(buf, data.path, URI_JSON)) {
                return serializeToJson(HttpUtils.noReq(), ctx, data.isKeepAlive.value,
                        new Message("Hello, World!"));
            }
        }

        return HttpStatus.NOT_FOUND;
    }

    public static void main(String[] args) {

        App.run(args);

        Conf.HTTP.set("maxPipeline", 128);
        Conf.HTTP.set("timeout", 0);

        new TestRapidoidTFB().listen(8080);

    }

}
