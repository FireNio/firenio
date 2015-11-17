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

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangkai
 *
 */
public class Test6 {
    
    
    
    public static void main(String[] args) throws Exception {
        int count = 1024 * 8;
        List<Socket> ss = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ss.add(new Socket("192.168.1.103", 8080));
        }
        System.out.println(".............");
        Thread.sleep(99999);
    }

}
