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
import java.lang.reflect.Method;

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.container.URLDynamicClassLoader;

public class ApplicationBootstrap {

	public static void main(String[] args) throws Exception {
		
		String rootPath = StringUtil.getValueFromArray(args, 0,FileUtil.getCurrentPath());

		boolean deployModel = Boolean.parseBoolean(StringUtil.getValueFromArray(args, 1, "false"));

		System.out.println(
				"************************************* start ***********************************");

		URLDynamicClassLoader classLoader = newClassLoader(deployModel, rootPath);

		System.out.println(
				"************************************* scan end ***********************************");

		Class<?> bootClass = classLoader.loadClass(ApplicationBootstrapEngine.class.getName());

		Object startup = bootClass.newInstance();

		Method method = bootClass.getDeclaredMethod("bootstrap", java.lang.String.class,
				boolean.class);

		System.out.println(
				"************************************* startup ***********************************");

		method.invoke(startup, rootPath, deployModel);
	}
	
	private static URLDynamicClassLoader newClassLoader(boolean deployModel,String rootLocalAddress) throws IOException{
		URLDynamicClassLoader classLoader = new URLDynamicClassLoader();
		if (deployModel) {
//			classLoader.addMatchStartWith("org/slf4j/");
//			classLoader.addMatchStartWith("org/apache/log4j/");
			classLoader.scan(new File(rootLocalAddress+"/lib"));
			classLoader.scan(new File(rootLocalAddress+"/conf"));
		}else{
			classLoader.addExcludePath("/app");
			classLoader.scan(new File(rootLocalAddress));
		}
		return classLoader;
	}

}
