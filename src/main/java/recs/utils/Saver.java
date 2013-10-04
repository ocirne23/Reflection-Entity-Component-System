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
import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Can write/read almost any object to/from a file, including classes with
 * generics like HashMap, ArrayList etc.
 *
 * @author Enrico van Oosten
 */
public class Saver {
	public static boolean ignoreTransient = true;

	private static final int NULL = 0;
	private static final int NEW = 1;
	private static final int REFERENCE = 2;

	private Saver() {

	}

	private static class Derp {
		public int da;
		public float wa;

		public Derp(int da, float wa) {
			this.da = da;
			this.wa = wa;
		}
	}

	public static void main(String[] args) {
		//HashMap test
		HashMap<String, Derp> derpMap = new HashMap<String, Derp>(4);
		for (int i = 0; i < 20; ++i) {
			derpMap.put(""+i, new Derp(i, i * 0.5f));
		}
		derpMap.put(""+3, derpMap.get(""+1));
		File derpMapFile = new File("derpmap");
		Saver.saveObject(derpMapFile, derpMap);

		System.out.println("HashMap test:");
		HashMap<String, Derp> loadedMap = Saver.readObject(derpMapFile, new HashMap<String, Derp>(), String.class, Derp.class);
		for (int i = 0; i < 20; ++i) {
			Derp derp = loadedMap.get(""+i);
			System.out.println(derp.da +":"+ derp.wa);
		}
		System.out.println("done");


		//ArrayList test
		ArrayList<Derp> derpList = new ArrayList<Derp>();
		for (int i = 0; i < 20; ++i) {
			derpList.add(new Derp(i, i * 0.5f));
		}
		File derpListFile = new File("derplist");
		Saver.saveObject(derpListFile, derpList);

		System.out.println("ArrayList test:");
		ArrayList<Derp> loadedList = Saver.readObject(derpListFile, new ArrayList<Derp>(), Derp.class);
		for (int i = 0; i < 20; ++i) {
			Derp derp = loadedList.get(i);
			System.out.println(derp.da +":"+ derp.wa);
		}
		System.out.println("done");


		//Queue test
		Queue<Derp> derpQueue = new LinkedList<Derp>();
		for (int i = 0; i < 20; ++i) {
			derpQueue.offer(new Derp(i, i * 0.5f));
		}
		File derpQueueFile = new File("derpqueue");
		Saver.saveObject(derpQueueFile, derpQueue);

		System.out.println("Queue test:");
		Queue<Derp> loadedQueue = Saver.readObject(derpQueueFile, new LinkedList<Derp>(), Derp.class);
		for (int i = 0; i < 20; ++i) {
			Derp derp = loadedQueue.poll();
			System.out.println(derp.da +":"+ derp.wa);
		}
		System.out.println("done");
	}


	/**
	 * Stores any object in the given file.
	 */
	public static File saveObject(File file, Object o) {
		try {
			Class<?> c = o.getClass();

			if (c.isArray())
				throw new RuntimeException("Array should be wrapped in an object class");
			else if (c.isPrimitive())
				throw new RuntimeException("Primitive should be wrapped in an object class");

			FileOutputStream fileOStream = new FileOutputStream(file);
			DataOutputStream ostream = new DataOutputStream(fileOStream);

			ObjectIntMap<Object> referenceMap = new ObjectIntMap<Object>();
			writeObject(o, ostream, referenceMap);

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

	/**
	 * Sets the values of the given object equal to the stored object in the
	 * file.
	 *
	 * The classes of the generic types used in the object need to be supplied, e.g:
	 * Saver.readObject(file, new HashMap<String, Integer>(), String.class, Integer.class);
	 */
	public static <T> T readObject(File file, T o, Class<?>... genericTypeArgs) {
		FileInputStream fileIStream;
		try {
			Class<?> c = o.getClass();

			HashMap<String, Class<?>> genericTypeClassMap = new HashMap<String, Class<?>>();
			TypeVariable<?>[] parameterTypes = c.getTypeParameters();
			for (int i = 0; i < parameterTypes.length; ++i) {
				genericTypeClassMap.put(parameterTypes[i].getName(), genericTypeArgs[i]);
			}

		//	System.out.println("genericTypeClassMap: " + genericTypeClassMap.toString());

			fileIStream = new FileInputStream(file);
			DataInputStream istream = new DataInputStream(fileIStream);
			byte[] bytes = new byte[(int) file.length()];
			istream.readFully(bytes);
			ByteBuffer b = ByteBuffer.wrap(bytes);

			ArrayList<Object> referenceList = new ArrayList<Object>();

			b.get();
			referenceList.add(o);
			readFields(o, b, genericTypeClassMap, referenceList);

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
		return o;
	}

	private static void writeObject(Object o, DataOutputStream ostream, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (o == null || o.getClass().isInterface()) {
			ostream.write(NULL);
			//System.out.println(ostream.size() + "\twriting null");
			return;
		} else {
			int referenceIdx = referenceMap.get(o, -1);
			// if this object has already been written, write a reference to it instead of a new instance.
			if (referenceIdx != -1) {
				ostream.write(REFERENCE);
				ostream.writeInt(referenceIdx);
			//	System.out.println(ostream.size() + "\twriting reference: " + o.getClass().getName() +", idx: " + referenceIdx);
				return;
			// if this is a new object, add it to the references together with the index in the ostream.
			} else {
				ostream.write(NEW);
				int objIdx = referenceMap.size;
				referenceMap.put(o, objIdx);
			//	System.out.println(ostream.size() + "\twriting new: " + o.getClass().getName() +", idx: " + objIdx);
			}
		}

		Class<?> c = o.getClass();
		do {
			//System.out.println("writing object: " + c.getName());
			for (Field f : c.getDeclaredFields()) {
				Class<?> type = f.getType();

				int modifiers = f.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;

				if (type.isPrimitive()) {
					//System.out.println(ostream.size() + "\twriting primitive " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					writePrimitive(o, f, ostream);
				} else if (type.isArray()) {
					//System.out.println(ostream.size() + "\twriting array " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					writeArray(o, f, ostream, referenceMap);
				} else {
					//System.out.println(ostream.size() + "\twriting object " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					f.setAccessible(true);
					Object obj = f.get(o);
					writeObject(obj, ostream, referenceMap);
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);
	}

	private static void writePrimitive(Object o, Field f, DataOutputStream ostream) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> type = f.getType();
		f.setAccessible(true);
		if (type == float.class) {
			ostream.writeFloat(f.getFloat(o));
		} else if (type == int.class) {
			ostream.writeInt(f.getInt(o));
		} else if (type == boolean.class) {
			ostream.writeBoolean(f.getBoolean(o));
		} else if (type == double.class) {
			ostream.writeDouble(f.getDouble(o));
		} else if (type == short.class) {
			ostream.writeShort(f.getShort(o));
		} else if (type == byte.class) {
			ostream.writeByte(f.getByte(o));
		} else if (type == char.class) {
			ostream.writeChar(f.getChar(o));
		} else if (type == long.class) {
			ostream.writeLong(f.getLong(o));
		}
	}

	private static void writeArray(Object o, Field f, DataOutputStream ostream, ObjectIntMap<Object> referenceMap) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> type = f.getType();
		f.setAccessible(true);
		int length = Array.getLength(f.get(o));

		ByteBuffer buffer = null;
		if (type == int[].class) {
			buffer = ByteBuffer.allocate(length * 4 + 4);
			buffer.putInt(length);
			buffer.asIntBuffer().put((int[]) f.get(o));
		} else if (type == float[].class) {
			buffer = ByteBuffer.allocate(length * 4 + 4);
			buffer.putInt(length);
			buffer.asFloatBuffer().put((float[]) f.get(o));
		} else if (type == boolean[].class) {
			buffer = ByteBuffer.allocate(length / 8 + 1 + 4);
			buffer.putInt(length);
			buffer.put(BitUtils.createByteArr((boolean[]) f.get(o)));
		} else if (type == double[].class) {
			buffer = ByteBuffer.allocate(length * 8 + 4);
			buffer.putInt(length);
			buffer.asDoubleBuffer().put((double[]) f.get(o));
		} else if (type == short[].class) {
			buffer = ByteBuffer.allocate(length * 2 + 4);
			buffer.putInt(length);
			buffer.asShortBuffer().put((short[]) f.get(o));
		} else if (type == byte[].class) {
			buffer = ByteBuffer.allocate(length + 4);
			buffer.putInt(length);
			buffer.put((byte[]) f.get(o));
		} else if (type == char[].class) {
			buffer = ByteBuffer.allocate(length * 2 + 4); // wattafak java.
			buffer.putInt(length);
			buffer.asCharBuffer().put((char[]) f.get(o));
		} else if (type == long[].class) {
			buffer = ByteBuffer.allocate(length * 8 + 4);
			buffer.putInt(length);
			buffer.asLongBuffer().put((long[]) f.get(o));
		}
		if (buffer != null) {
			ostream.write(buffer.array());
		} else {
			ostream.writeInt(length);

			if (type.getComponentType() == Object.class) {
				Class<?> elementClass = ((Object[]) f.get(o))[0].getClass();

				String elementType = elementClass.getName();
				ostream.writeInt(elementType.length());
				ostream.writeChars(elementType);
				//System.out.println("writing generic type: " + elementType +":"+ elementType.length());
			}

			for (Object obj : (Object[]) f.get(o)) {
				writeObject(obj, ostream, referenceMap);
			}
		}
	}

	/**
	 * @return false if object is null
	 */
	private static Object readObject(Class<?> type, ByteBuffer b, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		int objectStatus = b.get();

		//object is null
		if (objectStatus == NULL || type.isInterface()) {
			//System.out.println(b.position() + "\treading null: " + type.getName());
			return null;
		}

		Object obj = null;
		if (objectStatus == REFERENCE) {
			int refIdx = b.getInt();
			obj = referenceList.get(refIdx);
			//System.out.println(b.position() + "\treading reference: " + type.getName() + ", result: " + obj.getClass() +", idx: " + refIdx);
		} else if (objectStatus == NEW) {
			obj = createNewInstance(type);
			referenceList.add(obj);
			//System.out.println(b.position() + "\treading new: " + type.getName() + ", idx: " + (referenceList.size() - 1));
			if (obj !=  null)
				readFields(obj, b, genericTypeClassMap, referenceList);
		}

		return obj;
	}

	private static void readFields(Object o, ByteBuffer b, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> c = o.getClass();

		do {
			//System.out.println("reading object: " + c.getName());
			for (Field f : c.getDeclaredFields()) {
				Class<?> type = f.getType();
				Class<?> genericType = genericTypeClassMap.get(f.getGenericType().toString());

				if (genericType != null) {
					//System.out.println("setting generic type to: " + genericType +" from: " + type);
					type = genericType;
				}

				int modifiers = f.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;

				if (type.isPrimitive()) {
					//System.out.println(b.position() + "\treading primitive " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					readPrimitive(o, f, b);
				} else if (type.isArray()) {
					//System.out.println(b.position() + "\treading array " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					readArray(o, f, b, genericTypeClassMap, referenceList);
				//is object
				} else {
					//System.out.println(b.position() + "\treading object " + f.getName() +": "+ type.getName() +":"+ f.getGenericType());
					Object obj = readObject(type, b, genericTypeClassMap, referenceList);
					if (obj != null) {
						f.setAccessible(true);
						f.set(o, obj);
					}
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);
	}

	private static void readArray(Object o, Field f, ByteBuffer b, HashMap<String, Class<?>> genericTypeClassMap, ArrayList<Object> referenceList) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> type = f.getType();
		int length = b.getInt();

		f.setAccessible(true);
		if (type == int[].class) {
			byte[] data = new byte[length * 4];
			b.get(data, 0, length * 4);
			IntBuffer buf = ByteBuffer.wrap(data).asIntBuffer();
			int[] array = new int[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else if (type == float[].class) {
			byte[] data = new byte[length * 4];
			b.get(data, 0, length * 4);
			FloatBuffer buf = ByteBuffer.wrap(data).asFloatBuffer();
			float[] array = new float[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else if (type == boolean[].class) {
			byte[] data = new byte[length / 8 + 1];
			b.get(data, 0, length / 8 + 1);
			boolean[] array = BitUtils.getBooleans(data, length);
			f.set(o, array);
		} else if (type == double[].class) {
			byte[] data = new byte[length * 8];
			b.get(data, 0, length * 8);
			DoubleBuffer buf = ByteBuffer.wrap(data).asDoubleBuffer();
			double[] array = new double[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else if (type == short[].class) {
			byte[] data = new byte[length * 2];
			b.get(data, 0, length * 2);
			ShortBuffer buf = ByteBuffer.wrap(data).asShortBuffer();
			short[] array = new short[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else if (type == byte[].class) {
			byte[] data = new byte[length];
			b.get(data, 0, length);
			f.set(o, data);
		} else if (type == char[].class) {
			byte[] data = new byte[length * 2];
			b.get(data, 0, length * 2);
			CharBuffer buf = ByteBuffer.wrap(data).asCharBuffer();
			char[] array = new char[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else if (type == long[].class) {
			byte[] data = new byte[length * 8];
			b.get(data, 0, length * 8);
			LongBuffer buf = ByteBuffer.wrap(data).asLongBuffer();
			long[] array = new long[buf.remaining()];
			buf.get(array);
			f.set(o, array);
		} else {
			Class<?> componentType = type.getComponentType();
			//System.out.println("reading componentType: " + componentType);

			if (componentType == Object.class) {
				int typeStrLen = b.getInt();

				char[] chars = new char[typeStrLen];
				for (int i = 0; i < typeStrLen; ++i) {
					chars[i] = b.getChar();
				}

				String typeStr = new String(chars);

				try {
					componentType = Class.forName(typeStr);
					//System.out.println("setting componentType: " + componentType);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			if(componentType.isArray())
				throw new RuntimeException("array arrays are not supported([][])");

			f.set(o, Array.newInstance(componentType, length));
			for (int i = 0; i < length; i++) {
				Object element = readObject(componentType, b, genericTypeClassMap, referenceList);

				if(element != null) {
					((Object[]) f.get(o))[i] = element;
				}
			}
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
				else
					params[i] = 0;
			} else {
				params[i] = null;
			}
		}

		c.setAccessible(true);
		return (T) c.newInstance(params);
	}

	private static void readPrimitive(Object o, Field f, ByteBuffer b) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = f.getType();
		f.setAccessible(true);
		if (type == float.class) {
			f.set(o, b.getFloat());
		} else if (type == int.class) {
			f.set(o, b.getInt());
		} else if (type == boolean.class) {
			f.set(o, b.get() == 1);
		} else if (type == double.class) {
			f.set(o, b.getDouble());
		} else if (type == short.class) {
			f.set(o, b.getShort());
		} else if (type == byte.class) {
			f.set(o, b.get());
		} else if (type == char.class) {
			f.set(o, b.getChar());
		} else if (type == long.class) {
			f.set(o, b.getLong());
		}
	}
}
