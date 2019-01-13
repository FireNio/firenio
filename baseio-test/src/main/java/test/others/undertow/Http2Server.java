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
package test.others.undertow;

import static io.undertow.Handlers.predicate;
import static io.undertow.Handlers.resource;
import static io.undertow.predicate.Predicates.secure;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.component.SslContextBuilder;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.LearningPushHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * @author wangkai
 *
 */
public class Http2Server {

    private static final char[] STORE_PASSWORD = "password".toCharArray();

    private static SSLContext createSSLContext() throws Exception {
        InputStream key = FileUtil.readInputStreamByCls("l.key");
        InputStream cert = FileUtil.readInputStreamByCls("l.crt");
        return SslContextBuilder.forServer().keyManager(key, cert, "").build().getSSLContext();
    }

    private static KeyStore loadKeyStore(String name) throws Exception {
        String storeLoc = System.getProperty(name);
        final InputStream stream;
        if (storeLoc == null) {
            stream = Http2Server.class.getResourceAsStream(name);
        } else {
            stream = Files.newInputStream(Paths.get(storeLoc));
        }

        if (stream == null) {
            throw new RuntimeException("Could not load keystore");
        }
        try (InputStream is = stream) {
            KeyStore loadedKeystore = KeyStore.getInstance("JKS");
            loadedKeystore.load(is, password(name));
            return loadedKeystore;
        }
    }

    public static void main(final String[] args) throws Exception {
        String version = System.getProperty("java.version");
        System.out.println("Java version " + version);
        if (version.charAt(0) == '1' && Integer.parseInt(version.charAt(2) + "") < 8) {
            System.out.println("This example requires Java 1.8 or later");
            System.out.println(
                    "The HTTP2 spec requires certain cyphers that are not present in older JVM's");
            System.out.println("See section 9.2.2 of the HTTP2 specification for details");
            System.exit(1);
        }
        String bindAddress = System.getProperty("bind.address", "localhost");
        SSLContext sslContext = createSSLContext();
        Undertow server = Undertow.builder().setServerOption(UndertowOptions.ENABLE_HTTP2, false)
                .addHttpListener(8080, bindAddress).addHttpsListener(443, bindAddress, sslContext)
                .setHandler(
                        new SessionAttachmentHandler(
                                new LearningPushHandler(100,
                                        -1, Handlers.header(predicate(
                                                secure(), resource(new PathResourceManager(
                                                        Paths.get(System.getProperty(
                                                                "example.directory", System
                                                                        .getProperty("user.home"))),
                                                        100)).setDirectoryListingEnabled(true),
                                                new HttpHandler() {
                                                    @Override
                                                    public void handleRequest(
                                                            HttpServerExchange exchange)
                                                            throws Exception {
                                                        exchange.getResponseHeaders().add(
                                                                Headers.LOCATION,
                                                                "https://" + exchange.getHostName()
                                                                        + ":"
                                                                        + (exchange.getHostPort()
                                                                                + 363)
                                                                        + exchange
                                                                                .getRelativePath());
                                                        exchange.setStatusCode(
                                                                StatusCodes.TEMPORARY_REDIRECT);
                                                    }
                                                }), "x-undertow-transport",
                                                ExchangeAttributes.transportProtocol())),
                                new InMemorySessionManager("test"), new SessionCookieConfig()))
                .build();

        server.start();

        //        SSLContext clientSslContext = createSSLContext(loadKeyStore("client.keystore"), loadKeyStore("client.truststore"));
        //        LoadBalancingProxyClient proxy = new LoadBalancingProxyClient()
        //                .addHost(new URI("https://localhost:8443"), null, new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY, clientSslContext), OptionMap.create(UndertowOptions.ENABLE_HTTP2, true))
        //                .setConnectionsPerThread(20);

        //        Undertow reverseProxy = Undertow.builder()
        //                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
        //                .addHttpListener(8081, bindAddress)
        //                .addHttpsListener(8444, bindAddress, sslContext)
        //                .setHandler(ProxyHandler.builder().setProxyClient(proxy).setMaxRequestTime( 30000).build())
        //                .build();
        //        reverseProxy.start();

    }

    static char[] password(String name) {
        String pw = System.getProperty(name + ".password");
        return pw != null ? pw.toCharArray() : STORE_PASSWORD;
    }

}
