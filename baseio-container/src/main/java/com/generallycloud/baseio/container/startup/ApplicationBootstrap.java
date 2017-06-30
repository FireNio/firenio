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
package com.generallycloud.baseio.container.startup;

import java.io.File;
import java.io.IOException;

import com.generallycloud.baseio.common.DebugUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.container.URLDynamicClassLoader;

public class ApplicationBootstrap {

	public static void main(String[] args) throws Exception {
		
		String className = ApplicationBootstrapEngine.class.getName(); 
		
		String rootPath = StringUtil.getValueFromArray(args, 0,FileUtil.getCurrentPath());
		
		DebugUtil.info1(" ROOT_PATH: {}", rootPath);
		
		boolean deployModel = Boolean.parseBoolean(StringUtil.getValueFromArray(args, 1, "false"));

		startup(className,rootPath,deployModel);
	}
	
	public static void startup(String className,String rootPath,boolean deployModel) throws Exception{
		
		URLDynamicClassLoader classLoader = newClassLoader(deployModel, rootPath);

		Class<?> bootClass = classLoader.loadClass(className);

		Thread.currentThread().setContextClassLoader(classLoader); 
		
		Bootstrap startup = (Bootstrap) bootClass.newInstance();
		
		startup.bootstrap(rootPath, deployModel);
	}
	
	private static URLDynamicClassLoader newClassLoader(boolean deployModel,String rootLocalAddress) throws IOException{
		URLDynamicClassLoader classLoader = new URLDynamicClassLoader(false);
		classLoader.addMatchExtend(Bootstrap.class.getName());
		if (deployModel) {
			classLoader.scan(new File(rootLocalAddress+"/lib"));
			classLoader.scan(new File(rootLocalAddress+"/conf"));
		}else{
			classLoader.addExcludePath("/app");
			classLoader.scan(new File(rootLocalAddress));
		}
		return classLoader;
	}

}
