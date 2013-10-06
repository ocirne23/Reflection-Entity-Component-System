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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
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
	 * std collections have its elements as transient, this class can properly parse them.
	 */
	public static boolean ignoreTransient = true;

	private static final int NULL = 0;
	private static final int NEW = 1;
	private static final int REFERENCE = 2;

	/**
	 * Stores any object in the given file.
	 *
	 * @return
	 * 		the given file.
	 */
	public static <T> File saveObject(File file, T object) {
		try {
			Class<?> clazz = object.getClass();

			//System.out.println("Saving object: " + clazz);

			FileOutputStream fileOStream = new FileOutputStream(file);
			DataOutputStream ostream = new DataOutputStream(fileOStream);
			ObjectIntMap<Object> referenceMap = new ObjectIntMap<Object>();

			if (clazz.isArray()) {
				writeArray(object, object.getClass(), ostream, referenceMap);
			} else {
				writeObject(object, false, ostream, referenceMap);
			}

			ostream.flush();
			fileOStream.flush();
			ostream.close();
			fileOStream.close();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private static void writeObject(Object object, boolean isInterface, DataOutputStream ostream, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (object == null || object.getClass().isInterface()) {
			ostream.write(NULL);
			return;
		} else {
			int referenceIdx = referenceMap.get(object, -1);
			//if this object has already been written, write a reference to it instead of a new instance.
			if (referenceIdx != -1) {
				ostream.write(REFERENCE);
				ostream.writeInt(referenceIdx);
				return;
			//if this is a new object, add it to the references together with the index in the ostream.
			} else {
				ostream.write(NEW);
				int objIdx = referenceMap.size;
				referenceMap.put(object, objIdx);
			}
		}

		if (isInterface) {
			writeGenericType(ostream, object.getClass());
		}

		Class<?> clazz = object.getClass();

		do {
			for (Field field : clazz.getDeclaredFields()) {
				Class<?> type = field.getType();

				int modifiers = field.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;
				if (type.isPrimitive()) {
					//System.out.println(ostream.size() + "\twriting primitive: " + field.getName() +":"+ type +":"+ field.getGenericType());
					writePrimitive(object, field, ostream);
					//System.out.println("writing primitive: " + field.getName() +":"+ field.getType());
					continue;
				}

				field.setAccessible(true);
				Object obj = field.get(object);
				if (obj == null) {
					//System.out.println(ostream.size() + "\twriting null: " + field.getName() +":"+ type +":"+ field.getGenericType());
					writeObject(obj, false, ostream,referenceMap);
					continue;
				}


				if (type.isArray()) {
					//System.out.println(ostream.size() + "\twriting array: " + field.getName() +":"+ type +":"+ field.getGenericType());
					writeArray(obj, field.getType(), ostream, referenceMap);
				} else {
					//System.out.println(ostream.size() + "\twriting object: " + field.getName() +":"+ obj.getClass() +":"+ field.getGenericType());
					writeObject(obj, field.getType().isInterface(), ostream, referenceMap);
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz != Object.class);

		//System.out.println(ostream.size() + "\tfinished writing: " + object.getClass());
	}

	private static void writePrimitive(Object object, Field field, DataOutputStream ostream) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> type = field.getType();
		field.setAccessible(true);
		if (type == float.class) {
			ostream.writeFloat(field.getFloat(object));
		} else if (type == int.class) {
			ostream.writeInt(field.getInt(object));
		} else if (type == boolean.class) {
			ostream.writeBoolean(field.getBoolean(object));
		} else if (type == double.class) {
			ostream.writeDouble(field.getDouble(object));
		} else if (type == short.class) {
			ostream.writeShort(field.getShort(object));
		} else if (type == byte.class) {
			ostream.writeByte(field.getByte(object));
		} else if (type == char.class) {
			ostream.writeChar(field.getChar(object));
		} else if (type == long.class) {
			ostream.writeLong(field.getLong(object));
		}
	}

	private static void writeArray(Object object, Class<?> type, DataOutputStream ostream, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		int length = Array.getLength(object);

		ByteBuffer buffer = null;
		if (type == int[].class) {
			buffer = ByteBuffer.allocate(length * 4 + 4);
			buffer.putInt(length);
			buffer.asIntBuffer().put((int[]) object);
		} else if (type == float[].class) {
			buffer = ByteBuffer.allocate(length * 4 + 4);
			buffer.putInt(length);
			buffer.asFloatBuffer().put((float[]) object);
		} else if (type == boolean[].class) {
			buffer = ByteBuffer.allocate(length / 8 + 1 + 4);
			buffer.putInt(length);
			buffer.put(BitUtils.createByteArr((boolean[]) object));
		} else if (type == double[].class) {
			buffer = ByteBuffer.allocate(length * 8 + 4);
			buffer.putInt(length);
			buffer.asDoubleBuffer().put((double[]) object);
		} else if (type == short[].class) {
			buffer = ByteBuffer.allocate(length * 2 + 4);
			buffer.putInt(length);
			buffer.asShortBuffer().put((short[]) object);
		} else if (type == byte[].class) {
			buffer = ByteBuffer.allocate(length + 4);
			buffer.putInt(length);
			buffer.put((byte[]) object);
		} else if (type == char[].class) {
			buffer = ByteBuffer.allocate(length * 2 + 4); // wattafak java.
			buffer.putInt(length);
			buffer.asCharBuffer().put((char[]) object);
		} else if (type == long[].class) {
			buffer = ByteBuffer.allocate(length * 8 + 4);
			buffer.putInt(length);
			buffer.asLongBuffer().put((long[]) object);
		}
		if (buffer != null) {
			ostream.write(buffer.array());
		} else {
			ostream.writeInt(length);

			if (type.getComponentType().isArray()) {
				for (Object obj : (Object[]) object) {
					writeArray(obj, type.getComponentType(), ostream, referenceMap);
				}
			}

			if (type.getComponentType() == Object.class || type.getComponentType().isInterface()) {
				Object[] array = ((Object[]) object);

				// HACKY:
				// use the type of the first non null element in the array as the type of the array.
				// if all is null, type is Object.
				Class<?> elementClass = null;
				for (int i = 0; i < array.length; ++i) {
					if (array[i] != null) {
						elementClass = array[i].getClass();
						continue;
					}
				}

				writeGenericType(ostream, elementClass);
			}

			for (Object obj : (Object[]) object) {
				//if (obj != null)
				//	System.out.println(ostream.size() + "\twriting object from array: " + type.getComponentType() +":"+ obj.getClass());

				writeObject(obj, false, ostream, referenceMap);
			}
		}

		//System.out.println(ostream.size() + "\tfinished writing array: " + type);
	}

	private static Class<?> readGenericType(ByteBuffer byteBuffer) {
		int typeStrLen = byteBuffer.getInt();

		char[] chars = new char[typeStrLen];
		for (int i = 0; i < typeStrLen; ++i) {
			chars[i] = byteBuffer.getChar();
		}

		String typeStr = new String(chars);

		Class<?> componentType = null;

		try {
			componentType = Class.forName(typeStr);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return componentType;
	}

	private static void writeGenericType(DataOutputStream ostream, Class<?> clazz) throws IOException {
		String type = null;

		if (clazz != null) {
			type = clazz.getName();
		} else {
			type = "java.lang.Object";
		}

		ostream.writeInt(type.length());
		ostream.writeChars(type);
	}



	/**
	 * Sets the values of the given object equal to the stored object in the
	 * file.
	 *
	 * The classes of the generic types used in the object need to be supplied, e.g:
	 * Saver.readObject(file, new HashMap<String, Integer>(), String.class, Integer.class);
	 *
	 * @return
	 * 		the given object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readObject(File file, T object, Class<?>... genericTypeArgs) {
		FileInputStream fileIStream;
		try {
			Class<?> clazz = object.getClass();

			HashMap<String, Class<?>> genericTypeClassMap = new HashMap<String, Class<?>>();
			TypeVariable<?>[] parameterTypes = clazz.getTypeParameters();

			for (int i = 0; i < parameterTypes.length; ++i) {
				genericTypeClassMap.put(parameterTypes[i].getName(), genericTypeArgs[i]);
			}

			fileIStream = new FileInputStream(file);
			DataInputStream istream = new DataInputStream(fileIStream);
			byte[] bytes = new byte[(int) file.length()];

			istream.readFully(bytes);
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			ArrayList<Object> referenceList = new ArrayList<Object>();

			if (object.getClass().isArray()) {
				object = (T) readArray(object.getClass(), byteBuffer, genericTypeClassMap, referenceList);
			} else {
				object = (T) readObject(object.getClass(), byteBuffer, genericTypeClassMap, referenceList);
			}

			istream.close();
			fileIStream.close();
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

	private static Object readObject(Class<?> type, ByteBuffer byteBuffer, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		int objectStatus = byteBuffer.get();

		if (objectStatus == NULL) {
			return null;
		}

		Object obj = null;
		if (objectStatus == REFERENCE) {
			int refIdx = byteBuffer.getInt();
			obj = referenceList.get(refIdx);
		} else if (objectStatus == NEW) {
			if (type.isInterface()) {
				//System.out.println("interface: " + type);
				type = readGenericType(byteBuffer);
			}

			obj = createNewInstance(type);
			referenceList.add(obj);
			if (obj !=  null)
				readFields(obj, byteBuffer, genericTypeClassMap, referenceList);
		}

		return obj;
	}

	private static void readFields(Object object, ByteBuffer byteBuffer, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> c = object.getClass();

		do {
			for (Field field : c.getDeclaredFields()) {
				Class<?> type = field.getType();

				if (genericTypeClassMap != null) {
					Class<?> genericType = genericTypeClassMap.get(field.getGenericType().toString());

					if (genericType != null) {
						type = genericType;
					}
				}

				//if (type.getTypeParameters().length > 0)
				//	System.out.println("type parameters: " + Arrays.toString(type.getTypeParameters()));

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
								//System.out.println(byteBuffer.position() + "\ttypeVars: " + field.getGenericType() +" : " + typeVars[i].getName() +" : "+ ((Class<?>) typeArgs[i]).getName());
							}
						}
					}
				}

				if (type.isPrimitive()) {
					//System.out.println(byteBuffer.position() + "\treading primitive: " + field.getName() +":"+ type +":"+ field.getGenericType());
					readPrimitive(object, field, byteBuffer);
				} else if (type.isArray()) {
					//System.out.println(byteBuffer.position() + "\treading array: " + field.getName() +":"+ type +":"+ field.getGenericType());
					field.setAccessible(true);
					Object array = readArray(field.getType(), byteBuffer, genericTypeClassMap, referenceList);
					field.set(object, array);
				//is object
				} else {
					//System.out.println(byteBuffer.position() + "\treading object: " + field.getName() +":"+ type +":"+ field.getGenericType());

					Object obj = readObject(type, byteBuffer, genericTypeClassMap, referenceList);
					if (obj != null) {
						field.setAccessible(true);
						field.set(object, obj);
					}
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);

		//System.out.println(byteBuffer.position() + "\tfinished reading: " + object.getClass());
	}

	private static Object readArray(Class<?> type, ByteBuffer byteBuffer, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		int length = byteBuffer.getInt();

		if (type == int[].class) {
			byte[] data = new byte[length * 4];
			byteBuffer.get(data, 0, length * 4);
			IntBuffer buf = ByteBuffer.wrap(data).asIntBuffer();
			int[] array = new int[buf.remaining()];
			buf.get(array);
			return array;
		} else if (type == float[].class) {
			byte[] data = new byte[length * 4];
			byteBuffer.get(data, 0, length * 4);
			FloatBuffer buf = ByteBuffer.wrap(data).asFloatBuffer();
			float[] array = new float[buf.remaining()];
			buf.get(array);
			return array;
		} else if (type == boolean[].class) {
			byte[] data = new byte[length / 8 + 1];
			byteBuffer.get(data, 0, length / 8 + 1);
			boolean[] array = BitUtils.getBooleans(data, length);
			return array;
		} else if (type == double[].class) {
			byte[] data = new byte[length * 8];
			byteBuffer.get(data, 0, length * 8);
			DoubleBuffer buf = ByteBuffer.wrap(data).asDoubleBuffer();
			double[] array = new double[buf.remaining()];
			buf.get(array);
			return array;
		} else if (type == short[].class) {
			byte[] data = new byte[length * 2];
			byteBuffer.get(data, 0, length * 2);
			ShortBuffer buf = ByteBuffer.wrap(data).asShortBuffer();
			short[] array = new short[buf.remaining()];
			buf.get(array);
			return array;
		} else if (type == byte[].class) {
			byte[] data = new byte[length];
			byteBuffer.get(data, 0, length);
			return data;
		} else if (type == char[].class) {
			byte[] data = new byte[length * 2];
			byteBuffer.get(data, 0, length * 2);
			CharBuffer buf = ByteBuffer.wrap(data).asCharBuffer();
			char[] array = new char[buf.remaining()];
			buf.get(array);
			return array;
		} else if (type == long[].class) {
			byte[] data = new byte[length * 8];
			byteBuffer.get(data, 0, length * 8);
			LongBuffer buf = ByteBuffer.wrap(data).asLongBuffer();
			long[] array = new long[buf.remaining()];
			buf.get(array);
			return array;
		} else {
			Class<?> componentType = type.getComponentType();

			if (componentType == Object.class) {
				componentType = readGenericType(byteBuffer);
			}

			Object[] array = (Object[]) Array.newInstance(componentType, length);

			if (componentType.isArray()) {
				for (int i = 0; i < length; i++) {
					Object element = readArray(componentType, byteBuffer, genericTypeClassMap, referenceList);

					if(element != null) {
						array[i] = element;
					}
				}
				return array;
			}

			if (componentType.isInterface()) {
				componentType = readGenericType(byteBuffer);
				System.out.println("array is interfacearray: " + componentType);
			}

			for (int i = 0; i < length; i++) {
				//System.out.println(byteBuffer.position() + "\treading object from array: " + type.getComponentType());

				Object element = readObject(componentType, byteBuffer, genericTypeClassMap, referenceList);

				if(element != null) {
					array[i] = element;
				}
			}
			return array;
		}
	}

	private static void readPrimitive(Object object, Field field, ByteBuffer byteBuffer) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		field.setAccessible(true);
		if (type == float.class) {
			field.set(object, byteBuffer.getFloat());
		} else if (type == int.class) {
			field.set(object, byteBuffer.getInt());
		} else if (type == boolean.class) {
			field.set(object, byteBuffer.get() == 1);
		} else if (type == double.class) {
			field.set(object, byteBuffer.getDouble());
		} else if (type == short.class) {
			field.set(object, byteBuffer.getShort());
		} else if (type == byte.class) {
			field.set(object, byteBuffer.get());
		} else if (type == char.class) {
			field.set(object, byteBuffer.getChar());
		} else if (type == long.class) {
			field.set(object, byteBuffer.getLong());
		}
	}


	/**
	 * Creates a new instance of the given class type by using the best
	 * available constructor. Succeeds as long as passing 0/null values into the
	 * constructor does not crash the application.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T createNewInstance(Class<T> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// if its an interface or abstract class
		if(type.getDeclaredConstructors().length == 0)
			return null;

		for (Constructor<?> c : type.getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				c.setAccessible(true);
				return (T) c.newInstance();
			}
		}

		Constructor<?> c = type.getDeclaredConstructors()[0];
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
