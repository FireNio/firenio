package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointInputStream;

public class ClientInputStream extends EndPointInputStream{

	public ClientInputStream(EndPoint endPoint, int avaiable) {
		super(endPoint, avaiable);
	}
	
	

	public void havearest() {
		//不休息
	}
}
