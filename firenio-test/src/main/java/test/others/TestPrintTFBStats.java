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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.firenio.common.FileUtil;

/**
 * @author wangkai
 *
 */
public class TestPrintTFBStats {

    public static void main(String[] args) throws IOException {

        String s = FileUtil.readStringByCls("stats.txt.json");

        test(JSONArray.parseArray(s));

    }

    static final BigDecimal N1000 = new BigDecimal(1000);

    static void test(JSONArray array) {
        List<JSONObject> l = new ArrayList<>(1024);
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            for (String key : o.keySet()) {
                JSONObject o1 = o.getJSONObject(key);
                o1.put("t", new BigDecimal(key).multiply(N1000).longValue());
                l.add(o1);
            }
        }
        l.sort(new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return (int) (o1.getLongValue("t") - o2.getLongValue("t"));
            }

        });

        System.out.print("time\t");
        System.out.print("usr\t");
        System.out.print("sys\t");
        System.out.print("idl\t");
        System.out.println();
        for (int i = 0; i < l.size(); i++) {
            JSONObject o = l.get(i);
            double usr = 0;
            double sys = 0;
            double idl = 0;
            for (int j = 0; j < 28; j++) {
                JSONObject cc = o.getJSONObject("cpu" + j + " usage");
                usr += Double.parseDouble(cc.getString("usr"));
                sys += Double.parseDouble(cc.getString("sys"));
                idl += Double.parseDouble(cc.getString("idl"));
            }
            usr /= 28;
            sys /= 28;
            idl /= 28;
            System.out.print(i + "\t");
            System.out.print(new BigDecimal(usr).setScale(2, BigDecimal.ROUND_HALF_UP) + "\t");
            System.out.print(new BigDecimal(sys).setScale(2, BigDecimal.ROUND_HALF_UP) + "\t");
            System.out.print(new BigDecimal(idl).setScale(2, BigDecimal.ROUND_HALF_UP) + "\t");
            System.out.println();
        }
        System.out.println();
    }

}
