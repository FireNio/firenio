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
package com.generallycloud.nio.container.http11.example;

import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.container.http11.service.HttpFutureAcceptorService;

public class TestSimpleServlet extends HttpFutureAcceptorService {
	
//	private Logger	logger	= LoggerFactory.getLogger(TestSimpleServlet.class);

	@Override
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
//		System.out.println();
//		logger.info(future.getHost());
//		logger.info(future.getRequestURI());
//		System.out.println();
		String res = "yes server already accept your message :) " + future.getRequestParams();

		future.write(res);
		
		session.flush(future);
	}
}
