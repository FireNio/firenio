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
package com.generallycloud.test.others;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.FileUtil.OnDirectoryScan;

/**
 * @author wangkai
 *
 */
public class AddCommont {

    public static void main(String[] args) throws Exception {

        replaceCommont();

    }

    static void replaceCommont() throws Exception {

        String commont = " * Copyright 2015-2017 GenerallyCloud.com";

        replaceCommont0(new File("D:/GIT/baseio-master/baseio"), commont);

    }

    static void addCommontAll() throws Exception {

        String commont = "/*\n * Copyright 2015 GenerallyCloud.com" + "\n *  "
                + "\n * Licensed under the Apache License, Version 2.0 (the \"License\");"
                + "\n * you may not use this file except in compliance with the License."
                + "\n * You may obtain a copy of the License at" + "\n *  "
                + "\n *      http://www.apache.org/licenses/LICENSE-2.0" + "\n *  "
                + "\n * Unless required by applicable law or agreed to in writing, software"
                + "\n * distributed under the License is distributed on an \"AS IS\" BASIS,"
                + "\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
                + "\n * See the License for the specific language governing permissions and"
                + "\n * limitations under the License." + "\n */ \n";

        System.out.println(commont);

        addCommontAll0(new File("D:/GIT/baseio-master/baseio"), commont);
    }

    static void addCommontAll0(File file, String commont) throws Exception {

        FileUtil.scanDirectory(file, new OnDirectoryScan() {

            @Override
            public void onFile(File file) throws IOException {

                if (file.getName().endsWith(".java")) {

                    String content = FileUtil.readStringByFile(file, Encoding.UTF8);

                    content = commont + content;

                    FileUtil.writeByFile(file, content.getBytes(Encoding.UTF8), false);

                    System.out.println("File:" + file.getAbsolutePath());
                }

            }

            @Override
            public void onDirectory(File directory) {

            }
        });
    }

    static void replaceCommont0(File file, String commont) throws Exception {

        FileUtil.scanDirectory(file, new OnDirectoryScan() {

            @Override
            public void onFile(File file) throws IOException {

                if (file.getName().endsWith(".java")) {

                    List<String> list = FileUtil.readLines(file);

                    StringBuilder ss = new StringBuilder();

                    for (int i = 0; i < list.size(); i++) {

                        if (i == 1) {
                            ss.append(commont);
                        } else {
                            ss.append(list.get(i));
                        }

                        //						if (i != list.size() - 1) {
                        ss.append("\n");
                        //						}
                    }

                    FileUtil.writeByFile(file, ss.toString().getBytes(Encoding.UTF8), false);

                    System.out.println("File:" + file.getAbsolutePath());
                }
            }

            @Override
            public void onDirectory(File directory) {

            }
        });
    }

}
