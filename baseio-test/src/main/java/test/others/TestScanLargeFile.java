/*
 * Copyright 2015 The Baseio Project
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.common.FileUtil.OnDirectoryScan;

/**
 * @author wangkai
 *
 */
public class TestScanLargeFile {

    public static void main(String[] args) throws Exception {

        List<File> fs = new ArrayList<>();

        String file = "C://git/baseio-master/baseio";

        FileUtil.scanDirectory(new File(file), new OnDirectoryScan() {

            @Override
            public void onFile(File file) throws Exception {
                fs.add(file);

            }

            @Override
            public boolean onDirectory(File directory) throws Exception {
                return !directory.getName().startsWith(".");
            }
        });

        Collections.sort(fs, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                return (int) (o2.length() - o1.length());
            }
        });

        int count = Math.min(20, fs.size());

        for (int i = 0; i < count; i++) {
            System.out.println(fs.get(i).length() + "\t" + fs.get(i).getAbsolutePath());
        }

    }

}
