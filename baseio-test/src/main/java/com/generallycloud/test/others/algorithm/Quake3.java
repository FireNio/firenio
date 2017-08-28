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
package com.generallycloud.test.others.algorithm;

/**
 * @author wangkai
 *
 */
public class Quake3 {

    public static float invsqrt(float x) {
        float xhalf = 0.5f * x;
        int i = 0x5f375a86 - (Float.floatToIntBits(x) >> 1); //MagicNumber:0x5f3759df,0x5f375a86
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x); //Newton-Raphson Method based on Taylor Series
        return x;
    }

    public static void main(String[] args) {

        float f = invsqrt(100f);

        System.out.println(1 / f);
    }

}
