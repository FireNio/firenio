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
package com.generallycloud.test.io;

import java.util.List;

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.MessageFormatter;

/**
 * @author wangkai
 *
 */
public class Test {
    
    
    public static void main(String[] args) throws Exception {
        

        
        test();
        
        
    }
    
    static void test() throws Exception{
        
        List<String> ls = FileUtil.readLines(FileUtil.readInputStreamByCls("test.txt"),Encoding.UTF8);
        boolean req = true;
        for(String l : ls){
            l = l.trim();
            if (l.length() == 0) {
                req = false;
                continue;
            }
            int idx = l.indexOf(" ");
            String key = l.substring(0,idx);
            String desc = l.substring(idx+1);
            String key1;
            String key2;
            if (req) {
                key1 = "Req_" + key.replace("-", "_");
                key2 = key.toLowerCase();
            }else{
                key1 = key.replace("-", "_");
                key2 = key;
            }
            String  s = MessageFormatter.format("public static final String {} = \"{}\";", key1, key2);
            System.out.println("//"+desc.trim());
            System.out.println(s);
            
            
        }
    }

}
