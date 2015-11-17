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
package test.io;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author wangkai
 *
 */
public class TestSocket {
    
    public static void main(String[] args) throws Exception {
        
//        String host = "fe80::a793:9577:4396:8ca6";
//        host = "192.168.133.134";
//        
//        
//        InetSocketAddress add = new InetSocketAddress(host, 8080);
//        
//        Socket s = new Socket();
//        
//        s.connect(add);
//        
//        System.out.println(s);
        
        InetAddress add = InetAddress.getByName("www.baidu.com");
        System.out.println(add);
        add = InetAddress.getByName("192.168.131.134");
        System.out.println(add);
        
        
        
        
    }

}
