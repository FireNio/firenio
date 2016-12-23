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
package com.generallycloud.nio.sample.http11;

import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.http11.service.HttpFutureAcceptorService;

public class TestRedeployServlet extends HttpFutureAcceptorService {

	@Override
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		ApplicationContext applicationContext = ApplicationContext.getInstance();
		
		if(applicationContext.redeploy()){
			future.write("服务部署完成...");
		}else{
			future.write("服务部署失败...");
		}

		session.flush(future);
	}

}
