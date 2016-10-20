package com.generallycloud.nio.common.cmd;

import java.util.HashMap;
import java.util.Scanner;

public abstract class Command {

	private boolean					CONTINUE	= true;

	public abstract CmdResponse doHelp(CommandContext context,HashMap<String, String> params);

	private CmdResponse exec(CommandContext context,CmdRequest request) {
//		DebugUtil.debug(">>>> " + request);

		String cmd = request.getCmd();

		HashMap<String, String> params = request.getParams();

		Executable executable = context.getExecutable(cmd);

		if (executable == null) {
			return doHelp(context,params);
		}

		return executable.exec(context,params);
	}

	public void execute(CommandContext context) {

		printPrefix(context);

		while (CONTINUE) {

			Scanner scanner = new Scanner(System.in);

			String content = scanner.nextLine();

			CmdRequest request = parse(context,content);

			CmdResponse response = exec(context,request);

			CONTINUE = response.isContinue();

			System.out.println(response.getResponse());

			printPrefix(context);

		}
		return;
	}

	private CmdRequest parse(CommandContext context,String content) {
		CmdRequest request = new CmdRequest();
		content = content.trim();
		String[] contents = content.split(" ");
		if (contents.length == 0) {
			request.setCmd("none");
		} else if (contents.length == 1) {
			request.setCmd(contents[0].trim());
		} else {
			request.setCmd(contents[0].trim());
			for (int i = 1; i < contents.length; i++) {
				String[] arr = contents[i].split(":");
				String value = arr.length == 2 ? arr[1] : null;
				request.putParam(arr[0], value);
			}
		}
		return request;
	}

	public abstract void printPrefix(CommandContext context);

}
