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
package com.generallycloud.baseio.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class SharedBundle {

	private String					classPath		= null;
	private Map<String, File>		fullFilesMap	= new HashMap<>();

	public SharedBundle loadAllProperties() throws IOException {
		return loadAllProperties(FileUtil.getCurrentPath());
	}
	
	public SharedBundle loadAllProperties(String file) throws IOException {
		return loadAllProperties(file, Encoding.UTF8);
	}

	public SharedBundle loadAllProperties(String file, Charset charset) throws IOException {
		if (StringUtil.isNullOrBlank(file)) {
			return this;
		}
		return loadAllProperties(new File(file), charset);
	}

	public SharedBundle loadAllProperties(File root, Charset charset) throws IOException {
		
		if (root == null || !root.exists()) {
			throw new FileNotFoundException(root.getAbsolutePath());
		}

		this.fullFilesMap.clear();
		
		this.classPath = FileUtil.getPrettyPath(root.getCanonicalPath());

		this.loopLoadFile(root, charset,"");
		
		return this;
	}

	private void loopLoadFile(File file, Charset charset,String path) throws IOException {

		if (file.isDirectory()) {

			File[] files = file.listFiles();

			if (files == null) {
				return;
			}

			for (File f : files) {

				loopLoadFile(f, charset,path + "/" + f.getName());
			}
		} else {
			
			if (file.getName().endsWith(".class")) {
				return;
			}

			String filePathName = path.substring(1);
			
			fullFilesMap.put(filePathName, file);
		}
	}

	public String getClassPath() {
		return classPath;
	}

	public String readContent(String file, Charset charset) throws IOException {
		
		File cacheFile = readFile(file);
		
		if (cacheFile == null) {
			return FileUtil.input2String(readInputStream(file), charset);
		}

		return FileUtil.readStringByFile(cacheFile, charset);
	}
	
	public File readFile(String file){
		
		File cacheFile = fullFilesMap.get(file);
		
		if (cacheFile == null) {
			return fullFilesMap.get(file);
		}
		
		return cacheFile;
	}

	public InputStream readInputStream(String file) throws IOException {
		
		File cacheFile = readFile(file);
		
		if (cacheFile == null) {
			return FileUtil.readInputStreamByCls(file);
		}
		
		return new FileInputStream(cacheFile);
	}

	public FixedProperties readProperties(String fileName) throws IOException {
		return readProperties(fileName, Encoding.UTF8);
	}
	
	public FixedProperties readProperties(String fileName, Charset charset) throws IOException {
		
		File file = fullFilesMap.get(fileName);
		
		if (file == null) {
			return FileUtil.readPropertiesByCls(fileName, charset);
		}
		
		return FileUtil.readPropertiesByFile(file, charset);
	}

}
