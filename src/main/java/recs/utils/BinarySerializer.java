/*******************************************************************************
 * Copyright 2013 Enrico van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package recs.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.utils.ObjectIntMap;

//TODO: documentation, comments, remove commented println's.

/**
 * Can write/read almost any object/array to/from a file, including classes with
 * generics like HashMap, ArrayList etc.
 *
 * @author Enrico van Oosten
 */
public class BinarySerializer {
	/**
	 * std collections have its elements as transient, this class can properly
	 * parse them.
	 */
	public static boolean ignoreTransient = true;

	private static final byte NULL = 0;
	private static final byte NEW = 1;
	private static final byte REFERENCE = 2;
	private static final byte NEWTYPE = 3;

	public static <T> File saveToFile(File file, T object) {
		FileOutputStream fostream = null;
		try {
			fostream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		saveToStream(object, fostream);
		try {
			fostream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public static <T> T readFromFile(File file, T object, Class<?>... genericTypeArgs) {
		FileInputStream fistream = null;
		try {
			fistream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		T o = readFromStream(fistream, (int) file.length(), object, genericTypeArgs);
		try {
			fistream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o;
	}

	public static <T> T readFromByteArr(byte[] data, T object, Class<?>... genericTypeArgs) {
		ByteArrayInputStream bistream = new ByteArrayInputStream(data);
		T o = readFromStream(bistream, data.length, object, genericTypeArgs);
		try {
			bistream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o;
	}

	public static <T> T readFromStream(InputStream inputStream, T object, Class<?>... genericTypeArgs) {

		return null;
	}

	public static <T> OutputStream saveToStream(T object, OutputStream outputStream) {
		Class<?> clazz = object.getClass();
		// //System.out.println("Saving object: " + clazz);
		Output output = new Output(outputStream);

		ObjectIntMap<Object> referenceMap = new ObjectIntMap<Object>();
		try {
			if (clazz.isArray()) {
				writeArray(object, object.getClass(), output, referenceMap);
			} else {
				writeObject(object, object.getClass(), output, referenceMap);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.flush();

		return outputStream;
	}

	public static <T> byte[] saveToByteArr(T object) {
		Class<?> clazz = object.getClass();
		// //System.out.println("Saving object: " + clazz);
		Output output = new Output(64, -1);

		ObjectIntMap<Object> referenceMap = new ObjectIntMap<Object>();
		try {
			if (clazz.isArray()) {
				writeArray(object, object.getClass(), output, referenceMap);
			} else {
				writeObject(object, object.getClass(), output, referenceMap);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output.toBytes();
	}


	private static Class<?> writeObject(Object object, Class<?> oldType, Output output, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (object == null) {
			output.write(NULL);
			return oldType;
		}
		Class<?> clazz = object.getClass();
		if (clazz.isInterface()) {
			output.write(NULL);
			return oldType;
		} else {
			int referenceIdx = referenceMap.get(object, -1);
			// if this object has already been written, write a reference to it
			// instead of a new instance.
			if (referenceIdx != -1) {
				output.write(REFERENCE);
				output.writeInt(referenceIdx);
				return oldType; // TODO: write type?
				// if this is a new object, add it to the references together
				// with the index in the ostream.
			} else {
				if (clazz != oldType) {
					output.write(NEWTYPE);
					writeClassName(output, clazz, referenceMap);
				} else {
					output.write(NEW);
				}
				int objIdx = referenceMap.size;
				referenceMap.put(object, objIdx);
			}
		}

		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				Class<?> type = field.getType();

				int modifiers = field.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;
				if (type.isPrimitive()) {
					writePrimitive(object, field, output);
					continue;
				}

				field.setAccessible(true);
				Object obj = field.get(object);
				if (obj == null) {
					writeObject(obj, type, output, referenceMap);
					continue;
				}

				if (type.isArray()) {
					writeArray(obj, field.getType(), output, referenceMap);
				} else {
					writeObject(obj, type, output, referenceMap);
				}
			}
			clazz = clazz.getSuperclass();
		}

		return object.getClass();
	}

	private static void writePrimitive(Object object, Field field, Output output) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> type = field.getType();
		field.setAccessible(true);
		if (type == float.class) {
			output.writeFloat(field.getFloat(object));
		} else if (type == int.class) {
			output.writeInt(field.getInt(object));
		} else if (type == boolean.class) {
			output.writeBoolean(field.getBoolean(object));
		} else if (type == double.class) {
			output.writeDouble(field.getDouble(object));
		} else if (type == short.class) {
			output.writeShort(field.getShort(object));
		} else if (type == byte.class) {
			output.writeByte(field.getByte(object));
		} else if (type == char.class) {
			output.writeChar(field.getChar(object));
		} else if (type == long.class) {
			output.writeLong(field.getLong(object));
		}
	}

	private static void writeArray(Object object, Class<?> type, Output output, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		int length = Array.getLength(object);

		output.writeInt(length);

		if (type == int[].class) {
			output.writeInts((int[]) object);
		} else if (type == float[].class) {
			output.writeFloats((float[]) object);
		} else if (type == boolean[].class) {
			byte[] boolByteData = BitUtils.createByteArr((boolean[]) object);
			output.write(boolByteData);
		} else if (type == double[].class) {
			output.writeDoubles((double[]) object);
		} else if (type == short[].class) {
			output.writeShorts((short[]) object);
		} else if (type == byte[].class) {
			output.writeBytes((byte[]) object);
		} else if (type == char[].class) {
			output.writeChars((char[]) object);
		} else if (type == long[].class) {
			output.writeLongs((long[]) object);
		} else {
			if (type.getComponentType().isArray()) {
				for (Object obj : (Object[]) object) {
					writeArray(obj, type.getComponentType(), output, referenceMap);
				}
			}

			if (type.getComponentType().isInterface()) {
				Object[] array = ((Object[]) object);
				// HACKY:
				// use the type of the first non null element in the array as
				// the type of the array.
				// if all is null, type is Object.
				Class<?> elementClass = null;
				for (int i = 0; i < array.length; ++i) {
					if (array[i] != null) {
						elementClass = array[i].getClass();
						continue;
					}
				}
				writeClassName(output, elementClass, referenceMap);
			}

			Class<?> elementType = type.getComponentType();
			for (Object obj : (Object[]) object) {
				elementType = writeObject(obj, elementType, output, referenceMap);
			}
		}
	}

	private static Class<?> readClassName(Input input, ArrayList<Object> referenceList) {
		int header = input.readInt();

		Class<?> componentType = null;
		if (header == -1) {
			String typeStr = input.readString();

			try {
				componentType = Class.forName(typeStr);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			referenceList.add(componentType);
		} else {
			componentType = (Class<?>) referenceList.get(header);
		}

		return componentType;
	}

	private static void writeClassName(Output output, Class<?> clazz, ObjectIntMap<Object> referenceMap) throws IOException {
		String type = null;

		if (clazz != null) {
			type = clazz.getName();
		} else {
			type = "java.lang.Object";
		}

		int refIdx = referenceMap.get(type, -1);
		output.writeInt(refIdx);
		if (refIdx == -1) {
			referenceMap.put(type, referenceMap.size);
			output.writeString(type);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T readFromStream(InputStream istream, int numBytes, T object, Class<?>... genericTypeArgs) {
		try {
			Class<?> clazz = object.getClass();

			HashMap<String, Class<?>> genericTypeClassMap = new HashMap<String, Class<?>>();
			TypeVariable<?>[] parameterTypes = clazz.getTypeParameters();

			for (int i = 0; i < parameterTypes.length; ++i) {
				genericTypeClassMap.put(parameterTypes[i].getName(), genericTypeArgs[i]);
			}

			Input input = new Input(istream, numBytes);
			ArrayList<Object> referenceList = new ArrayList<Object>();

			if (object.getClass().isArray()) {
				object = (T) readArray(object.getClass(), input, genericTypeClassMap, referenceList);
			} else {
				object = (T) readObject(object.getClass(), input, genericTypeClassMap, referenceList);
			}

			istream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return object;
	}

	private static Object readObject(Class<?> type, Input input, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		byte objectStatus = input.readByte();

		if (objectStatus == NULL) {
			return null;
		}

		if (objectStatus == REFERENCE) {
			int refIdx = input.readInt();
			return referenceList.get(refIdx);
		}

		if (objectStatus == NEWTYPE || type.isInterface()) {
			type = readClassName(input, referenceList);
		}

		Object obj = createNewInstance(type);
		referenceList.add(obj);
		if (obj != null) {
			readFields(obj, input, genericTypeClassMap, referenceList);
		}

		return obj;
	}

	private static void readFields(Object object, Input input, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> c = object.getClass();

		while (c != Object.class) {
			for (Field field : c.getDeclaredFields()) {
				Class<?> type = field.getType();

				if (genericTypeClassMap != null) {
					Class<?> genericType = genericTypeClassMap.get(field.getGenericType().toString());

					if (genericType != null) {
						type = genericType;
					}
				}

				int modifiers = field.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;

				if (field.getGenericType() instanceof ParameterizedType) {
					TypeVariable<?>[] typeVars = type.getTypeParameters();
					Type[] typeArgs = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

					if (typeVars.length > 0) {
						boolean updatedTypes = false;

						for (int i = 0; i < typeVars.length; ++i) {
							if (typeArgs[i] instanceof Class<?>) {
								if (!updatedTypes) {
									genericTypeClassMap = new HashMap<String, Class<?>>();
									updatedTypes = true;
								}
								genericTypeClassMap.put(typeVars[i].getName(), ((Class<?>) typeArgs[i]));
							}
						}
					}
				}

				if (type.isPrimitive()) {
					readPrimitive(object, field, input);
				} else if (type.isArray()) {
					field.setAccessible(true);
					Object array = readArray(field.getType(), input, genericTypeClassMap, referenceList);
					field.set(object, array);
					// is object
				} else {
					Object obj = readObject(type, input, genericTypeClassMap, referenceList);
					if (obj != null) {
						field.setAccessible(true);
						field.set(object, obj);
					}
				}
			}
			c = c.getSuperclass();
		}
	}

	private static Object readArray(Class<?> type, Input input, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		int length = input.readInt();

		if (type == int[].class) {
			return input.readInts(length);
		} else if (type == float[].class) {
			return input.readFloats(length);
		} else if (type == boolean[].class) {
			byte[] data = input.readBytes(length / 8 + 1);
			return BitUtils.getBooleans(data, length);
		} else if (type == double[].class) {
			return input.readDoubles(length);
		} else if (type == short[].class) {
			return input.readShorts(length);
		} else if (type == byte[].class) {
			return input.readBytes(length);
		} else if (type == char[].class) {
			return input.readChars(length);
		} else if (type == long[].class) {
			return input.readLongs(length);
		} else {
			Class<?> componentType = type.getComponentType();

			Object[] array = (Object[]) Array.newInstance(componentType, length);

			if (componentType.isArray()) {
				for (int i = 0; i < length; i++) {
					Object element = readArray(componentType, input, genericTypeClassMap, referenceList);

					if (element != null) {
						array[i] = element;
					}
				}
				return array;
			}

			if (componentType.isInterface()) {
				componentType = readClassName(input, referenceList);
			}

			for (int i = 0; i < length; i++) {
				Object element = readObject(componentType, input, genericTypeClassMap, referenceList);

				if (element != null) {
					componentType = element.getClass();
					array[i] = element;
				}
			}
			return array;
		}
	}

	private static void readPrimitive(Object object, Field field, Input input) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		field.setAccessible(true);
		if (type == float.class) {
			field.set(object, input.readFloat());
		} else if (type == int.class) {
			field.set(object, input.readInt());
		} else if (type == boolean.class) {
			field.set(object, input.readByte() == 1);
		} else if (type == double.class) {
			field.set(object, input.readDouble());
		} else if (type == short.class) {
			field.set(object, input.readShort());
		} else if (type == byte.class) {
			field.set(object, input.readByte());
		} else if (type == char.class) {
			field.set(object, input.readChar());
		} else if (type == long.class) {
			field.set(object, input.readLong());
		}
	}
/*
	private static class ConstructorComparable implements Comparator<Constructor<?>> {
		@Override
		public int compare(Constructor<?> c1, Constructor<?> c2) {
			return c1.getParameterTypes().length - c2.getParameterTypes().length;
		}
	}

	private static ConstructorComparable constructorComparable = new ConstructorComparable();
*/
	/**
	 * Creates a new instance of the given class type by using the best
	 * available constructor. Succeeds as long as passing 0/null values into the
	 * constructor does not crash the application.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T createNewInstance(Class<T> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// if its an interface or abstract class
		Constructor<?>[] constructors = type.getDeclaredConstructors();

		if (constructors.length == 0)
			return null;
		// if trying to create a generic
		if (type == Class.class)
			return null;

		//Arrays.sort(constructors, constructorComparable);


		for (Constructor<?> c : constructors) {
			//System.out.println("constructor: " + c.getName() +":"+ c.getParameterTypes().length);

			if (c.getParameterTypes().length == 0) {
				c.setAccessible(true);
				return (T) c.newInstance();
			}
		}

		Constructor<?> c = constructors[0];
		Class<?>[] parameters = c.getParameterTypes();
		Object[] params = new Object[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].isPrimitive()) {
				if (parameters[i] == char.class)
					params[i] = '0';
				else if (parameters[i] == boolean.class)
					params[i] = false;
				else if (parameters[i] == short.class)
					params[i] = (short) 0;
				else if (parameters[i] == byte.class)
					params[i] = (byte) 0;
				else
					params[i] = 0;
			} else {
				params[i] = null;
			}
		}

		c.setAccessible(true);
		return (T) c.newInstance(params);
	}

	private BinarySerializer() {

	}

}
