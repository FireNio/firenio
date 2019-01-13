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
package test.others;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;

import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public class TestSocketChannel {
    
    
    public static void main(String[] args) throws Exception {
        
        SocketChannel ch = SocketChannel.open();
        Field ff = ch.getClass().getDeclaredField("fd");
        if (ff != null) {
            Util.trySetAccessible(ff);
            Object fo = ff.get(ch);
            Field f2 = fo.getClass().getDeclaredField("fd");
            if (f2 != null) {
                Util.trySetAccessible(f2);
                Object f2o = f2.get(fo);
                System.out.println(f2o);
            }
            
        }
        
        
        
    }

}
