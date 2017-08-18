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
package com.generallycloud.baseio.container.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.jms.cmd.CmdResponse;
import com.generallycloud.baseio.container.jms.cmd.Command;
import com.generallycloud.baseio.container.jms.cmd.CommandContext;

@Deprecated
public class MQCommand extends Command {

    private HelpExecutable helpExecutable = new HelpExecutable();

    public static void main(String[] args) {

        CommandContext context = new CommandContext();

        MQCommand command = new MQCommand();

        context.registExecutable("browser", new BrowserExecutable());
        context.registExecutable("connect", new ConnectExecutable());
        context.registExecutable("disconnect", new DisconnectExecutable());
        context.registExecutable("exit", new ExitExecutable());
        context.registExecutable("help", command.helpExecutable);
        context.registExecutable("?", command.helpExecutable);
        context.registExecutable("size", new MessageSizeExecutable());

        command.execute(context);
    }

    @Override
    public void printPrefix(CommandContext context) {

        SocketChannelConnector connector = (SocketChannelConnector) context
                .getAttribute("KEY_CONNECTOR");

        if (connector == null) {
            System.out.print("未连接> ");
        } else {
            System.out.print(connector + "> ");
        }
    }

    @Override
    public CmdResponse doHelp(CommandContext context, HashMap<String, String> params) {

        return helpExecutable.exec(context, params);
    }
}
