package com.uhf.uhf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.PowerManager;

public class PowerManagerUtils {
	private static final String CLASS = "android.os.PowerManager";
	private static final String UART_POWER_OPEN = "modulePower";

	public static void open(PowerManager manager, int uart_num) {
		try {
			Class<?> spClass = Class.forName(CLASS);

			Method getMethod = spClass.getMethod(UART_POWER_OPEN, int.class,
					boolean.class);
			getMethod.invoke(manager, uart_num, true);
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
	}

	public static void close(PowerManager manager, int uart_num) {
		try {
			Class<?> spClass = Class.forName(CLASS);

			Method getMethod = spClass.getMethod(UART_POWER_OPEN, int.class,
					boolean.class);
			getMethod.invoke(manager, uart_num, false);
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
	}
}
