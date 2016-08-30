package com.generallycloud.nio.common.cmd;

import java.util.HashMap;

import com.generallycloud.nio.component.AttributesImpl;

public class CommandContext extends AttributesImpl {

	private HashMap<String, Executable>	executors	= new HashMap<String, Executable>();
	
	public void registExecutable(String cmd, Executable executable) {
		executors.put(cmd.toUpperCase(), executable);
	}
	
	public Executable getExecutable(String cmd){
		return executors.get(cmd.toUpperCase());
	}
	
	public String getCommandKeys(){
		return executors.keySet().toString();
		
	}
}
