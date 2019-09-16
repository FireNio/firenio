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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.elasticsearch.common.recycler.Recycler.C;

import com.firenio.common.FileUtil;

/**
 * @author: wangkai
 **/
public class TestLoad {

    public static void main(String[] args) throws Exception {

        String s_128_0   = "1280141.0028337";
        String s_128_1   = "1281063.0028301";
        String s_128_930 = "1289307.0028297";
        String s_128_938 = "1289385.0028258";
        String s_130_0   = "1300290.0028466";
        String s_130_2   = "1302952.0028294";
        cal(s_128_0);
        cal(s_128_1);
        cal(s_128_930);
        cal(s_128_938);
        cal(s_130_0);
        cal(s_130_2);

    }


    static void cal(String dic) throws Exception {
        int  count  = 10;
        File s_file = new File("C:\\Users\\wangkai\\Downloads\\" + dic + "\\z-small.txt");
        File m_file = new File("C:\\Users\\wangkai\\Downloads\\" + dic + "\\z-medium.txt");
        File l_file = new File("C:\\Users\\wangkai\\Downloads\\" + dic + "\\z-large.txt");

        List<Cfg> s_c = getQPS(s_file, count, 0);
        List<Cfg> m_c = getQPS(m_file, count, 1);
        List<Cfg> l_c = getQPS(l_file, count, 2);

        int[]         abs_cnt = new int[3];
        double        abs_cut = 0;
        double        abs_sum = 0;
        List<Integer> max_con = new ArrayList<>();
        List<Integer> max_qps = new ArrayList<>(count);
        List<Cfg>     temp    = new ArrayList<>(3);

        for (int i = 0; i < count; i++) {
            temp.clear();
            temp.add(s_c.get(i));
            temp.add(m_c.get(i));
            temp.add(l_c.get(i));
            Collections.sort(temp, new Comparator<Cfg>() {
                @Override
                public int compare(Cfg o1, Cfg o2) {
                    return (int) ((o1.rtt * 100) - (o2.rtt * 100));
                }
            });
            double qps   = 0;
            int    r_con = 1024;
            for (int j = 0; j < temp.size(); j++) {
                Cfg    c    = temp.get(j);
                double _qps = (Math.min(r_con, c.con) * (1000 / c.rtt));
                qps += _qps;
                r_con -= c.con;
                c.qps = _qps;
                abs_cnt[c.type] += _qps;
            }
            abs_sum += qps * 6;
            max_qps.add((int) qps);

            int    base  = 1900;
            double s_qps = s_c.get(i).qps;
            double m_qps = m_c.get(i).qps;
            double l_qps = l_c.get(i).qps;
            double a_qps = s_qps + m_qps + l_qps;
            if (s_qps - (a_qps * 1 / 6) > 0) {
                abs_cut += (s_qps - (a_qps * 1 / 6));
                //                abs_cut += (s_qps - (a_qps * 1 / 6)) / (a_qps * 1 / 6);
            }
            if (m_qps - (a_qps * 2 / 6) > 0) {
                abs_cut += (m_qps - (a_qps * 2 / 6));
                //                abs_cut += (m_qps - (a_qps * 2 / 6)) / (a_qps * 2 / 6);
            }
            if (l_qps - (a_qps * 3 / 6) > 0) {
                abs_cut += (l_qps - (a_qps * 3 / 6));
                //                abs_cut += (l_qps - (a_qps * 3 / 6)) / (a_qps * 3 / 6);
            }
            max_con.add(s_c.get(i).con + m_c.get(i).con + l_c.get(i).con);
        }
        System.out.println("abs_sum:" + abs_sum);
        System.out.println("sc:" + s_c);
        System.out.println("mc:" + m_c);
        System.out.println("lc:" + l_c);
        System.out.println("ac:" + max_qps);
        System.out.println("max_con: " + max_con);
        System.out.println("abs_cut: " + abs_cut);
        System.out.println(dic + ">>abs_cnt: " + (abs_cnt[0] + abs_cnt[1] + abs_cnt[2]) + ",abs_cnt_s:" + abs_cnt[0] + ",abs_cnt_m:" + abs_cnt[1] + ",abs_cnt_l:" + abs_cnt[2]);
        System.out.println();
    }

    static int mask(double q, double v, double base) {
        if (q < base) {
            return (int) v;
        }
        return (int) (v * (1 + (((q - base) / base) * ((q - base) / base))));
    }

    static List<Cfg> getQPS(File file, int count, int type) throws Exception {
        List<String> ss  = FileUtil.readLines(file);
        List<Cfg>    res = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String s               = ss.get(i);
            int    avg_rtt_idx     = s.indexOf("avg_rtt") + 8;
            int    avg_rtt_idx_end = s.indexOf(',', avg_rtt_idx);
            int    c               = Integer.parseInt(s.substring(s.length() - 3));
            double rtt             = Double.parseDouble(s.substring(avg_rtt_idx, avg_rtt_idx_end));
            if (c < 100 || c > 650 || rtt < 10 || rtt > 100) {
                System.out.println("error");
            }
            Cfg cfg = new Cfg();
            cfg.con = c;
            cfg.rtt = rtt;
            cfg.type = type;
            res.add(cfg);
        }
        return res;
    }

    static class Cfg {

        double rtt;
        int    con;
        int    type;
        double qps;

        @Override
        public String toString() {
            return String.valueOf((int)qps);
        }
    }


}
