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

import org.junit.Test;

import com.firenio.collection.ByteTree;

import junit.framework.Assert;

/**
 * @author wangkai
 *
 */
public class TestByteTree {

    @Test
    public void test() {

        ByteTree t = new ByteTree();

        t.add("bcd");
        t.add("ccc");
        t.add("bbd");
        t.add("ddd");
        t.add("abcd");
        t.add("bcdd");
        t.add("bbdd");

        Assert.assertNotNull(t.get("1bcd1".getBytes(), 1, 3));
        Assert.assertNotNull(t.get("1ccc1".getBytes(), 1, 3));
        Assert.assertNotNull(t.get("1bbd1".getBytes(), 1, 3));

        Assert.assertNull(t.get("1bbb1".getBytes(), 1, 3));
        Assert.assertNull(t.get("1cccc1".getBytes(), 1, 4));
        Assert.assertNull(t.get("1cc1".getBytes(), 1, 2));
        Assert.assertNull(t.get("1bc1".getBytes(), 1, 2));

    }

}
