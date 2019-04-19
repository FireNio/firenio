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
package test.others;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.firenio.collection.IntMap;
import com.firenio.common.Assert;

/**
 * @author wangkai
 */
public class TestIntMap {

    IntMap<String> map  = new IntMap<>(16);
    List<Integer>  list = new ArrayList<>(16);

    @Before
    public void before() {
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int    k = r.nextInt(Integer.MAX_VALUE);
            String v = String.valueOf(k);
            map.put(k, v);
            list.add(k);
        }
    }

    @Test
    public void testPut() {
        Assert.expectTrue(list.size() == 1000);
        Assert.expectTrue(map.size() == 1000);
        for (int i = 0; i < list.size(); i++) {
            Integer k = list.get(i);
            Assert.expectTrue(map.get(k).equals(String.valueOf(k)));
        }
    }

    @Test
    public void testScan() {
        int i = 0;
        for (map.scan(); map.hasNext(); ) {
            i++;
            Integer k = map.nextKey();
            String  v = map.value();
            Assert.expectTrue(v.equals(String.valueOf(k)));
            Assert.expectTrue(list.contains(k));
        }
        Assert.expectTrue(i == list.size());
    }

    @Test
    public void testRemove() {
        int i    = 0;
        int size = list.size();
        for (map.scan(); map.hasNext(); ) {
            i++;
            Integer k = map.nextKey();
            String  v = map.value();
            Assert.expectTrue(v.equals(String.valueOf(k)));
            Assert.expectTrue(list.contains(k));
            if (k % 2 == 0) {
                map.remove(k);
                list.remove(k);
            }
        }
        Assert.expectTrue(i == size);
        testScan();
    }

}
