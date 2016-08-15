package com.gifisan.nio.component.protocol.http11;

import com.alibaba.fastjson.JSONObject;

public class Test2 {

	
	private boolean isEnd;
	
	
	public static void main(String[] args) {

		byte[] array = new byte[] { -127, -124, 10, -98, 76, -66, 126, -5, 63, -54 };

		Test2 t = new Test2();
		
		byte b = -124;
		
		b = -128;
		
		System.out.println(b & 0x7f);
		
		b = 11;
		
		System.out.println(b >> 7);
		
		System.out.println(JSONObject.toJSONString(t));
	}


	public boolean isEnd() {
		return isEnd;
	}


	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}
	
	
}
