package com.gifisan.nio.common;

public class ClassUtil {

	private static Logger	logger	= LoggerFactory.getLogger(ClassUtil.class);

	public static Object newInstance(Class clazz) {

		if (clazz == null) {
			return null;
		}

		try {
			return clazz.newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
