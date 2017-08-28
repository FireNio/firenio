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
package com.generallycloud.test.others;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author wangkai
 *
 */
public class TestInetAddress {

    public static void main(String[] args) throws Exception {

        InetSocketAddress address = new InetSocketAddress("192.168.1.100", 80);

        InetAddress address2 = address.getAddress();
        
        Field holderField = InetAddress.class.getDeclaredField("holder");

        Object o = holderField.get(address2);

        Field addressField = address2.getClass().getDeclaredField("address");

        int a = addressField.getInt(o);

        System.out.println(a);

    }

}
