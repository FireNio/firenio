package test.io;

import java.util.Random;

import com.firenio.collection.IntMap;
import com.firenio.log.DebugUtil;

public class Test1 {

    public static void main(String[] args) {

        testMyMap();
        //                testNettyMap();
    }

    static void testNettyMap() {
        Random         r   = new Random();
        IntMap<String> map = new IntMap<>(4, 0.75f);
        for (int i = 0; i < 1000; i++) {
            int    k = r.nextInt(Integer.MAX_VALUE);
            String v = String.valueOf(k);
            map.put(k, v);
        }
        DebugUtil.info("c:{}", map.conflict());
    }

    static void testMyMap() {
        Random         r   = new Random();
        IntMap<String> map = new IntMap<>(4, 0.75f);
        for (int i = 0; i < 20; i++) {
            int    k = r.nextInt(Integer.MAX_VALUE);
            String v = String.valueOf(k);
            map.put(k, v);
        }
        DebugUtil.info("c:{}", map.conflict());
        int s = 0;
        for (map.scan(); map.hasNext(); ) {
            s++;
            int    i = map.next();
            int    k = map.indexKey(i);
            String v = map.indexValue(i);
            if (i % 2 == 0) {
                int idx = map.indexKey(i + 1);
                if (idx != -1) {
                    String res = map.remove(idx);
                    if (res != null) {
                        s++;
                    }
                }
            } else {
                map.remove(k);
            }
        }
        DebugUtil.info("s:{}", s);
    }

}
