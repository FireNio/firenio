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
package test.others.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.net.InetAddress;
import java.util.Date;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * @author wangkai
 *
 */
public class TestPut {

    @SuppressWarnings("resource")
    public static void test() throws Exception {

        Settings esSettings = Settings.builder()
                //.put("cluster.name", "utan-es") //设置ES实例的名称
                // .put("client.transport.sniff", false) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();

        /**
         * 这里的连接方式指的是没有安装x-pack插件,如果安装了x-pack则参考{@link ElasticsearchXPackClient}
         * 1. java客户端的方式是以tcp协议在9300端口上进行通信
         * 2. http客户端的方式是以http协议在9200端口上进行通信
         */
        TransportClient client = new PreBuiltTransportClient(esSettings).addTransportAddress(
                new TransportAddress(InetAddress.getByName("localhost"), 9300));

        System.out.println("ElasticsearchClient 连接成功");

        String index = "twitter";

        IndexResponse putResponse = client.prepareIndex(index, "tweet", "1")
                .setSource(jsonBuilder().startObject().field("user", "kimchy")
                        .field("postDate", new Date()).field("message", "trying out Elasticsearch")
                        .endObject())
                .get();

        // Index name
        String _index = putResponse.getIndex();
        // Type name
        String _type = putResponse.getType();
        // Document ID (generated or not)
        String _id = putResponse.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long _version = putResponse.getVersion();
        // status has stored current instance statement.
        RestStatus status = putResponse.status();

        System.out.println(_index);
        System.out.println(_type);
        System.out.println(_id);
        System.out.println(_version);
        System.out.println(status);

        GetRequest getRequest = new GetRequest("twitter", _type, _id);

        GetResponse getResponse = client.get(getRequest).get();

        System.out.println(getResponse.getSource());

        client.close();
    }

    public static void main(String[] args) throws Exception {
        test();
    }

    static String newString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(i % 10);
        }
        return sb.toString();
    }

}
