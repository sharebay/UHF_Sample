package com.uhf.uhf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemPropertiesUtils {

	private static final String CLASS = "android.os.SystemProperties";
	private static final String GET_METHOD = "get";
	private static final String SET_METHOD = "set";

	public static String get(String key, String def) {
		String value = def;
		try {
			Class<?> spClass = Class.forName(CLASS);

			Method getMethod = spClass.getMethod(GET_METHOD, String.class,
					String.class);
			Object invoke = getMethod.invoke(null, key, def);
			value = invoke.toString();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return value;
	}
}
