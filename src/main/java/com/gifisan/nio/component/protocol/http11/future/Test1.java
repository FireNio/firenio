package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;

public class Test1 {

	public static void main(String[] args) throws IOException {

		Cookie cookie = new Cookie("test", "aaaa");
		
		cookie.setComment("111");
		cookie.setMaxAge(999);
		cookie.setPath("sss");
		
		cookie.setVersion(5);

		System.out.println(cookie);
	}

	

}
