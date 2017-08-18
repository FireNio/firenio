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
package com.generallycloud.sample.baseio.http11;

import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.container.http11.service.HttpFutureAcceptorService;

public class TestUploadServlet extends HttpFutureAcceptorService {

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {

        String res;

        if (future.hasBodyContent()) {

            res = "yes server already accept your message :) " + future.getRequestParams()
                    + " </BR><PRE style='font-size: 18px;color: #FF9800;'>"
                    + new String(future.getBodyContent()) + "</PRE>";
        } else {
            res = "yes server already accept your message :) " + future.getRequestParams();
        }

        future.setResponseHeader("Content-Type", "text/html");

        future.write(res);
        session.flush(future);
    }

}
