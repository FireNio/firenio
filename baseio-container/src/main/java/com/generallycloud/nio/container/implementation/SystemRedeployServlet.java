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
package com.generallycloud.nio.container.implementation;

import java.io.IOException;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

/**
 * @author wangkai
 *
 */
public class SystemRedeployServlet extends FutureAcceptorService {
	
	public SystemRedeployServlet() {
		this.setServiceName("/system-redeploy");
	}
	
	@Override
	public void accept(SocketSession session, ReadFuture future) throws IOException {

		NamedReadFuture f = (NamedReadFuture) future;

		if (getServiceName().equals(f.getFutureName())) {

			ApplicationContext context = ApplicationContext.getInstance();

			if (context.redeploy()) {
				future.write("redeploy successful");
			} else {
				future.write("redeploy failed");
			}

			session.flush(future);

			return;
		}

		future.write("server is upgrading ...");

		session.flush(future);

	}

}
