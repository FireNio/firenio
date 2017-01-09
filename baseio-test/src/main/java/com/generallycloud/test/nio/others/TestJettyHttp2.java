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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * @author wangkai
 *
 */
public class TestJettyHttp2 {

	
	public static void main(String[] args) throws Exception{
		
		HttpClient client = new HttpClient();
		
		String url = "https://www.taobao.com/";
		
//		url = "https://localhost:443/test";
		
		client.start();
		
		Request request = client.newRequest(url);
		
		ContentResponse response = request.send();
		
		String res = response.getContentAsString();
		
		System.out.println(res);
	}
}
