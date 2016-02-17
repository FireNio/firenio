package com.gifisan.mtp.jms.client.cmd;

import java.util.HashMap;
import java.util.Scanner;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.common.DebugUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageBrowser;
import com.gifisan.mtp.jms.client.impl.MessageBrowserImpl;

public class Portal {

	private boolean CONTINUE = true;
	
	private String host = null;
	
	private String port = null;
	
	private MessageBrowser browser = null;
	
	private ClientConnector connector = null;
	
	public static void main(String[] args) {
		new Portal().portal();
	}
	
	public void portal(){
		printPrefix();
		
		while (CONTINUE) {
			
			Scanner scanner = new Scanner(System.in);
			
			String content = scanner.nextLine();
			
			CmdRequest request = parse(content);
			
			CmdResponse response = exec(request);
			
			CONTINUE = response.isContinue();
			
			System.out.println(response.getResponse());
			
			printPrefix();
			
		}
		
		return ;
		
	}
	
	private void printPrefix(){
		if (host == null) {
			System.out.print("未连接> ");
		}else{
			System.out.print("@"+host+":"+port+"> ");
		}
		
	}
	
	
	private CmdRequest parse(String content){
		CmdRequest request = new CmdRequest();
		content = content.trim();
		String []contents = content.split(" ");
		if (contents.length == 0) {
			request.setCmd("none");
		}else if (contents.length == 1) {
			request.setCmd(contents[0].trim());
		}else{
			request.setCmd(contents[0].trim());
			for (int i = 1; i < contents.length; i++) {
				String []arr = contents[i].split(":");
				String value = arr.length == 2 ? arr[1] : null;
				request.putParam(arr[0], value);
			}
		}
		return request;
		
	}
	
	private CmdResponse exec(CmdRequest request){
		System.out.println(">>>>>>>>>>>>>>"+request);
		
		String cmd = request.getCmd();
		Executable executable = executes.get(cmd);
		if (executable == null) {
			executable = executes.get("help");
		}
		HashMap<String, String> params = request.getParams();
		
		return executable.exec(params);
	}
	
	
	
	private HashMap<String, Executable> executes = new HashMap<String, Portal.Executable>(){
		
		{
			put("browser", new Executable() {
				
				public CmdResponse exec(HashMap<String, String> params) {
					
					CmdResponse response = new CmdResponse();
					if (browser == null) {
						response.setResponse("请先登录！");
						return response;
					}
					String messageID = params.get("-mid");
					if (StringUtil.isNullOrBlank(messageID)) {
						response.setResponse("参数不正确！\n"
								+"example:\n"
								+"browser -mid:mid");
						return response;
					}
					try {
						Message message = browser.browser(messageID);
						if (message == null) {
							response.setResponse("没有此ID的消息！");
						}else{
							response.setResponse(message.toString());
						}
					} catch (JMSException e) {
						browser.logout();
						host = null;
						port = null;
						DebugUtil.debug(e);
					}
					
					return response;
				}
			});
			put("size", new Executable() {
				
				public CmdResponse exec(HashMap<String, String> params) {
					
					CmdResponse response = new CmdResponse();
					if (browser == null) {
						response.setResponse("请先登录！");
						return response;
					}
					String messageID = params.get("-mid");
					if (StringUtil.isNullOrBlank(messageID)) {
						response.setResponse("参数不正确！\n"
								+"example:\n"
								+"browser -mid:mid");
						return response;
					}
					try {
						Message message = browser.browser(messageID);
						if (message == null) {
							response.setResponse("没有此ID的消息！");
						}else{
							response.setResponse(message.toString());
						}
					} catch (JMSException e) {
						browser.logout();
						host = null;
						port = null;
						DebugUtil.debug(e);
					}
					
					return response;
				}
			});
			
			put("disconnect", new Executable() {
				public CmdResponse exec(HashMap<String, String> params) {
					CmdResponse response = new CmdResponse();
					browser.logout();
					browser = null;
					host = null;
					port = null;
					response.setResponse("已断开连接！");
					return response;
					
				}
			});
			
			put("connect", new Executable() {
				
				public CmdResponse exec(HashMap<String, String> params) {
					
					CmdResponse response = new CmdResponse();
					
					String username = params.get("-un");
					String password = params.get("-p");
					String host     = params.get("-host");
					String port     = params.get("-port");
					
					if (StringUtil.isNullOrBlank(username) 
							|| StringUtil.isNullOrBlank(password)
							|| StringUtil.isNullOrBlank(host)
							|| StringUtil.isNullOrBlank(port)) {
						response.setResponse("参数不正确！\n"
												+"example:\n"
												+"connect -host:localhost -port:8300 -un=admin -p:admin100");
						return response;
					}
					
					
					
					connector = new ClientConnector(host, Integer.valueOf(port));
					
					
					
					try {
						connector.connect();
						
						ClientSesssion session = connector.getClientSession();
						
						browser = new MessageBrowserImpl(session);
						browser.login(username, password);
						
						Portal.this.host = host;
						Portal.this.port = port;
						
						response.setResponse("连接成功！");
						
					} catch (Exception e) {
						browser = null;
						response.setResponse(e.getMessage());
						//debug
						DebugUtil.debug(e);
					}
					return response;
				}
			});
			
			put("exit", new Executable() {
				
				public CmdResponse exec(HashMap<String, String> params) {
					
					CmdResponse response = new CmdResponse();
					response.setContinue(false);
					response.setResponse("系统退出！");
					
					return response;
				}
			});
			put("help", new Executable() {
				
				public CmdResponse exec(HashMap<String, String> params) {
					
					CmdResponse response = new CmdResponse();
					response.setResponse("帮助！");
					
					return response;
				}
			});
		}
	};
	
	
	interface Executable{
		
		CmdResponse exec(HashMap<String, String> params);
	}
	
	
}
