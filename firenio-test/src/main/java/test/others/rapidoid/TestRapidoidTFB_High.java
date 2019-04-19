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

import org.rapidoid.config.Conf;
import org.rapidoid.http.MediaType;
import org.rapidoid.setup.App;
import org.rapidoid.setup.On;

/**
 * @author wangkai
 */
public class TestRapidoidTFB_High {

    public static void main(String[] args) {
        App.run(args);

        Conf.HTTP.set("maxPipeline", 128);
        Conf.HTTP.set("timeout", 0);
        Conf.HTTP.sub("mandatoryHeaders").set("connection", false);

        On.port(8080);

        setupSimpleHandlers();
    }

    private static void setupSimpleHandlers() {
        On.get("/plaintext").managed(false).contentType(MediaType.TEXT_PLAIN).serve("Hello, world!");
        On.get("/json").managed(false).json(() -> new Message("Hello, world!"));
    }

}
