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

import com.firenio.buffer.ByteBuf;
import com.firenio.common.ByteUtil;

/**
 * @author: wangkai
 **/
public class TestFloat {

    public static void main(String[] args) {
        System.out.println("2.03f: " + Float.floatToIntBits(2.03f));
        System.out.println("0.5f: " + Float.floatToIntBits(0.5f));
        System.out.println("0.9f: " + Float.floatToIntBits(0.9f));
        System.out.println("1.0f: " + Float.floatToIntBits(1.0f));
        System.out.println("12.34f: " + Float.floatToIntBits(12.34f));
        System.out.println("1.5f: " + Float.floatToIntBits(1.5f));
        System.out.println("0.1f: " + Float.floatToIntBits(1f - 0.9f));
        System.out.println(1 - 0.9f);
        System.out.println(1f + 0.5f);

        int v = 0b00111101100010011001100110011010;
        System.out.println(v);

        long l = 34;
        for (int i = 0; i < 23; i++) {
            l *= 2;
        }
        System.out.println(l);

        float f2 = 0.01f;
        float f3 = f2 + 1;
        for (int i = 0; i < 999999; i++) {
            f3 += f2;
            int f4 = Float.floatToIntBits(f3);
            if ((f4 & 1) == 1) {
                System.out.println(f3 + "------ & 1 =====1");
                break;
            }
        }
        System.out.println("end...");

    }
}
