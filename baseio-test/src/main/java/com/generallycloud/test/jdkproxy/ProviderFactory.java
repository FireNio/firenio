package com.generallycloud.test.jdkproxy;

import java.lang.reflect.Proxy;

public class ProviderFactory {

	public static FontProvider getFontProvider() {
		Class<FontProvider> targetClass = FontProvider.class;
		return (FontProvider) Proxy.newProxyInstance(
				targetClass.getClassLoader(), 
				targetClass.getInterfaces(),
				new CachedProviderHandler(new FontProviderFromDisk()));
	}

	static class FontProviderFromDisk implements FontProvider {

		@Override
		public String getFont(String name) {
			return "DISK:" + name;
		}

	}
}
