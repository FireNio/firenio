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
package com.generallycloud.baseio.container.jms.server;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.RESMessage;

public class MQTransactionServlet extends MQServlet {

    public static final String SERVICE_NAME = MQTransactionServlet.class.getSimpleName();

    @Override
    public void doAccept(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) throws Exception {

        String action = future.getReadText();

        TransactionSection section = attachment.getTransactionSection();

        if ("begin".equals(action)) {
            RESMessage message = null;
            if (section == null) {
                section = new TransactionSection(getMQContext());
                attachment.setTransactionSection(section);
                message = RESMessage.SUCCESS;
            } else {
                message = MQRESMessage.R_TRANSACTION_BEGINED;
            }
            future.write(message.toString());
            session.flush(future);

        } else if ("commit".equals(action)) {
            RESMessage message = null;
            if (section == null) {
                message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
            } else {
                if (section.commit()) {
                    message = RESMessage.SUCCESS;
                } else {
                    message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
                }
                attachment.setTransactionSection(null);
            }

            future.write(message.toString());
            session.flush(future);

        } else if ("rollback".equals(action)) {
            RESMessage message = null;
            if (section == null) {
                message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
            } else {
                if (section.rollback()) {
                    message = RESMessage.SUCCESS;
                } else {
                    message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
                }
                attachment.setTransactionSection(null);
            }
            future.write(message.toString());
            session.flush(future);
        }
        // else if("complete".equals(action)){
        // RESMessage message = RESMessage.R_SUCCESS;
        // attachment.setTpl_message(null);
        // response.write(message.toString());
        // response.flush();
        //
        // }
        else {
            future.write(MQRESMessage.R_CMD_NOT_FOUND.toString());
            session.flush(future);
        }
    }

}
