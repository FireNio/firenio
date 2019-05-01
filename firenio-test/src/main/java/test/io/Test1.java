package test.io;

import java.util.Random;

import com.firenio.Options;
import com.firenio.collection.IntMap;
import com.firenio.common.Util;
import com.firenio.log.DebugUtil;

public class Test1 {

    public static void main(String[] args) {

        //        testMyMap();
        //                testNettyMap();
        Options.setSysClockStep(1);
        System.out.println("t1:" + Util.now());
        Util.sleep(100);
        System.out.println("t2:" + Util.now());
        long old   = Util.now_f();
        long count = 1024 * 1024 * 512;
        for (long i = 0; i < count; i++) {
            Util.now();
        }
        long past = Util.past(old);
        System.out.println("Time:" + past);

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
