package org.hibernate.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class IntrospectedClass {
	private final Class clazz;

	private Method[] declaredMethods;
	private Field[] declaredFields;

	public IntrospectedClass(Class clazz) {
		this.clazz = clazz;
	}

	public Class getClazz() {
		return clazz;
	}

	public Method[] getDeclaredMethods() {
		if (declaredMethods == null) {
			declaredMethods = clazz.getDeclaredMethods();
		}

		return declaredMethods;
	}

	public Field[] getDeclaredFields() {
		if (declaredFields == null) {
			declaredFields = clazz.getDeclaredFields();
		}

		return declaredFields;
	}

	public boolean isObjectClass() {
		return Object.class.equals( clazz );
	}
}
