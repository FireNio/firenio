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

import static com.generallycloud.baseio.common.MathUtil.binaryString2HexString;
import static com.generallycloud.baseio.common.MathUtil.byte2BinaryString;

public class TestMathUtil {

    public static void main(String[] args) {

        //		int time = 999999;

        final byte[] bb = new byte[10];

        //		long value = 11111111112L;

        //		long2Byte(bb, value, 0);

        //		ITestHandle.doTest(new ITest() {
        //			public void test(int i) throws Exception {
        //				MathUtil.byte2Long(bb, 0);
        //			}
        //		}, time, "Byte2Long");

        //		System.out.println(byte2Long(bb, 0));

        //		System.out.println(bytes2HexString(new byte[] { 125, -22, -25, 89, 19, 90 }));

        //		System.out.println(byte2BinaryString((byte) -127));
        //		System.out.println(byte2BinaryString((byte) -128));
        //		System.out.println(byte2BinaryString((byte) -2));
        //		System.out.println(byte2BinaryString((byte) -1));

        //		System.out.println(binaryString2HexString("00111111"));
        //		System.out.println(binaryString2HexString("01111111"));
        System.out.println(binaryString2HexString("01110000"));
        System.out.println(binaryString2HexString("01010000"));
        //0x50=01010000,0x70=01110000
        int v = Integer.MAX_VALUE >> 1;

        System.out.println(v);
        System.out.println(Integer.toBinaryString(v));
        System.out.println(byte2BinaryString((byte) 0x3F));
        System.out.println((Integer.MAX_VALUE + 5) & 0x7fffffff);

        System.out.println();

    }
}
