package recs.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import recs.test.components.Position;

public class Saver {

	private static StringBuilder builder = new StringBuilder();

	public static void main(String[] args) {
		Position p = new Position(1, 2);
		String data = saveObject(p);

		Position p2 = new Position();
		readObject(p2, data, 0);
	}

	public static String saveObject(Object o) {
		try {
			appendObject(o);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		String data = builder.toString();
		builder.delete(0, builder.length());
		System.out.println("data: " + data);
		return data;
	}

	private static void appendObject(Object o) throws IllegalArgumentException, IllegalAccessException {
		System.out.println("appending class: " + o.getClass());
		for (Field f : o.getClass().getDeclaredFields()) {
			System.out.println("type: " + f.getType());
			Class<?> type = f.getType();
			if (type == Object.class)
				continue;
			f.setAccessible(true);
			Object value = f.get(o);
			if (value == null)
				continue;
			if (type.isPrimitive()) {
				if (Modifier.isFinal(f.getModifiers()))
					continue;
				builder.append(value);
				builder.append(';');
			} else if(type.isArray()) {
				appendArray(o, f);
			} else {
				appendObject(value);
			}
		}
	}

	private static void appendArray(Object o, Field f) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = f.getType();

		System.out.println("array of: " + type );
		//Class<?> elementType = Array.get(f.get(o), 0).getClass();
		int length = Array.getLength(f.get(o));
		for(int i = 0; i < length; i++) {
			Object item = Array.get(f.get(o), i);
			if(item != null)
				builder.append(item.toString());
			else
				builder.append("null");
			if(i == length - 1) {
				builder.append(';');
			} else {
				builder.append(',');
			}
		}
	}

	private static int readObject(Object o, String data, int fromIndex) {
		try {
			for (Field f : o.getClass().getDeclaredFields()) {
				Class<?> type = f.getType();
				if (type == Object.class)
					continue;

				System.out.println("setting for type: " + type);

				if (type.isPrimitive()) {
					if (Modifier.isFinal(f.getModifiers()))
						continue;
					f.setAccessible(true);
					int nextIndex = data.indexOf(';', fromIndex);
					String contents = data.substring(fromIndex, nextIndex);

					if (type == float.class) {
						System.out.println("setting float: " + contents);
						f.setFloat(o, Float.valueOf(contents));
					} else if (type == double.class) {
						System.out.println("setting double: " + contents);
						f.setDouble(o, Double.valueOf(contents));
					} else if (type == int.class) {
						System.out.println("setting int: " + contents);
						f.setInt(o, Integer.valueOf(contents));
					} else if (type == short.class) {
						System.out.println("setting short: " + contents);
						f.setShort(o, Short.valueOf(contents));
					} else if (type == byte.class) {
						System.out.println("setting byte: " + contents);
						f.setByte(o, Byte.valueOf(contents));
					} else if (type == char.class) {
						System.out.println("setting char: " + contents);
						f.setChar(o, Character.valueOf(contents.charAt(0)));
					} else if (type == long.class) {
						System.out.println("setting long: " + contents);
						f.setLong(o, Long.valueOf(contents));
					} else if (type == String.class) {
						f.set(o, contents);
					} else if (type == int[].class) {

					}
					fromIndex = nextIndex + 1;
				} else {
					boolean hasNoArg = false;
					Constructor<?> c = null;
					for (Constructor<?> constructor : type.getDeclaredConstructors()) {
						c = constructor;
						if (c.getParameterTypes().length == 0) {
							hasNoArg = true;
							c.setAccessible(true);
							Object next = c.newInstance();
							fromIndex = readObject(next, data, fromIndex);
						}
					}
					if (!hasNoArg) {
						System.out.println(type.getName());
						if (type.isArray()) {
							Object next = Array.newInstance(type, 0);
							fromIndex = readObject(next, data, fromIndex);
						} else {
							Class<?>[] parameters = c.getParameterTypes();
							Object[] params = new Object[parameters.length];

							for (int i = 0; i < parameters.length; i++) {
								if (parameters[i].isPrimitive()) {
									if (parameters[i] == char.class)
										params[i] = '0';
									else if (parameters[i] == boolean.class)
										params[i] = false;
									else
										params[i] = 0;
								} else {
									params[i] = null;
								}
							}
							c.setAccessible(true);
							Object next = c.newInstance(params);
							fromIndex = readObject(next, data, fromIndex);
						}
					}
				}
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not load class");
		}
		return fromIndex;

	}
}
