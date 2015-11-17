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
package test.io.buffer;

import com.firenio.buffer.ByteBufAllocatorGroup;
import com.firenio.buffer.PooledByteBufAllocator;
import com.firenio.common.Util;

/**
 * @author wangkai
 *
 */
public class TestAllocUtil {

    public static PooledByteBufAllocator direct() throws Exception {
        return direct(1024);
    }

    public static PooledByteBufAllocator direct(int cap) throws Exception {
        ByteBufAllocatorGroup group = new ByteBufAllocatorGroup(1, cap, 1, true);
        Util.start(group);
        return group.getAllocator(0);
    }

    public static PooledByteBufAllocator heap() throws Exception {
        return heap(1024);
    }

    public static PooledByteBufAllocator heap(int cap) throws Exception {
        ByteBufAllocatorGroup group = new ByteBufAllocatorGroup(1, cap, 1, false);
        Util.start(group);
        return group.getAllocator(0);
    }

}
