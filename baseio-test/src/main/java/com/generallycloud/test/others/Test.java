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

import java.io.IOException;

import com.generallycloud.baseio.common.MathUtil;

public class Test {

    public static final byte PROTOCOL_RESPONSE = 1;
    public static final byte PROTOCOL_PUSH     = 2;
    public static final byte PROTOCOL_BRODCAST = 3;

    public static void main(String[] args) throws IOException {

        byte b = 127;

        System.out.println(MathUtil.byte2BinaryString(b));
        System.out.println(MathUtil.byte2BinaryString((byte) (b & 0x3f)));

        System.out.println(MathUtil.byte2BinaryString((byte) -1));
        System.out.println(MathUtil.byte2BinaryString((byte) -2));

        System.out.println(Integer.MAX_VALUE >> 3);

        System.out.println(MathUtil.binaryString2HexString("00100000"));

        //test branch   tes22222

    }
}
