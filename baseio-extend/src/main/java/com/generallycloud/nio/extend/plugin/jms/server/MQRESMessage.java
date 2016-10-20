package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.extend.RESMessage;

public class MQRESMessage {

	public static int	CODE_TRANSACTION_BEGINED		= 901;
	public static int	CODE_TRANSACTION_NOT_BEGIN	= 902;
	public static int CODE_TRANSACTION_UNAUTH		= 903;
	public static int CODE_CMD_NOT_FOUND			= 904;

	public static RESMessage R_TRANSACTION_BEGINED = new RESMessage(CODE_TRANSACTION_BEGINED
			, "transaction begined");
	public static RESMessage R_TRANSACTION_NOT_BEGIN = new RESMessage(CODE_TRANSACTION_NOT_BEGIN
			, "transaction not begin");
	public static RESMessage R_UNAUTH	 = new RESMessage(CODE_TRANSACTION_UNAUTH	
			, "unauthorized");
	public static RESMessage R_CMD_NOT_FOUND	 = new RESMessage(CODE_CMD_NOT_FOUND	
			, "cmd not found");
	
	
	
}
