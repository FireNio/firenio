package com.generallycloud.test.jdkproxy;

public class Test {

	public static void main(String[] args) {
		
		FontProvider fontProvider = ProviderFactory.getFontProvider();
		
		String font = fontProvider.getFont("微软雅黑");

		System.out.println(font);

	}

}
