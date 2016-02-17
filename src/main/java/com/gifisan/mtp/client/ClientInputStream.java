package com.gifisan.mtp.client;

import com.gifisan.mtp.component.EndPoint;
import com.gifisan.mtp.component.EndPointInputStream;

public class ClientInputStream extends EndPointInputStream{

	public ClientInputStream(EndPoint endPoint, int avaiable) {
		super(endPoint, avaiable);
	}
	
	

	public void havearest() {
		//不休息
	}
}
