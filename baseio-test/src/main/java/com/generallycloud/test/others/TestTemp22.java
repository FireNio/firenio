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

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.FileUtil.OnDirectoryScan;
import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class TestTemp22 {

    public static void main(String[] args) throws Exception {

        FileUtil.scanDirectory(
                new File(
                        "D:/工作仓库/SVN/1_CODE/goldtrade-common/src/main/java/com/brilliance/goldtrade/common/stand/model"),
                new OnDirectoryScan() {

                    @Override
                    public void onFile(File file) throws Exception {

                        String fname = file.getName();

                        if (!fname.endsWith("Request.java") && !fname.endsWith("Response.java")) {
                            return;
                        }

                        String code = fname.substring(0, fname.length() - 5);

                        String content = FileUtil.readStringByFile(file, Encoding.UTF8);

                        int index = content.indexOf("{");

                        String append = ""

                                + "\n\n\tpublic " + code + "() {" + "\n\t\tthis(true);" + "\n\t}"
                                + "\n\n\tpublic " + code + "(boolean initialize) {"
                                + "\n\t\tsuper(initialize);" + "\n\t}" + "\n\n\tpublic " + code
                                + "(StandModel model) {" + "\n\t\tsuper(model);" + "\n\t}";

                        String newContent = content.substring(0, index + 1) + append
                                + content.substring(index + 1);

                        FileUtil.writeByFile(file, newContent, Encoding.UTF8, false);

                        DebugUtil.debug("file:" + file.getAbsolutePath());

                    }

                    @Override
                    public void onDirectory(File directory) throws Exception {
                        // TODO Auto-generated method stub

                    }
                });

    }
}
