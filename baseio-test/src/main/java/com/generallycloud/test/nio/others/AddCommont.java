/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.test.nio.others;

import java.io.File;
import java.io.IOException;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.common.FileUtil.OnDirectoryScan;

/**
 * @author wangkai
 *
 */
public class AddCommont {

	public static void main(String[] args) throws Exception {

		String commont = "/*" + "\n * Copyright 2015 GenerallyCloud.com" + "\n *  "
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
		
		addCommont(new File("D:/GIT/baseio-master/baseio"), commont);
		
	}
	
	static void addCommont(File file,String c) throws Exception{
		
		FileUtil.scanDirectory(file, new OnDirectoryScan() {
			
			@Override
			public void onFile(File file) throws IOException {
				
				if (file.getName().endsWith(".java")) {
					
					String content = FileUtil.readFileToString(file, Encoding.UTF8);
					
					content = c + content;
					
					FileUtil.write(file, content.getBytes(Encoding.UTF8),false);
					
					System.out.println("File:"+file.getAbsolutePath());
				}
				
			}
			
			@Override
			public void onDirectory(File directory) {
				
			}
		});
	}

}
