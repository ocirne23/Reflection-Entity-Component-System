package recs.core.utils;

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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

/**
 * Can write/read almost any object to/from a file. Classes with generic
 * Object[] or likewise are not supported.
 *
 * @author Enrico van Oosten
 */
public class Saver {
	public static boolean ignoreTransient = false;

	private Saver() {
	}

	/**
	 * Stores any object in the given file.
	 */
	public static File saveObject(Object o, File file) {
		try {
			Class<?> c = o.getClass();
			if (c.isArray())
				throw new RuntimeException("Array should be wrapped in an object class");
			else if (c.isPrimitive())
				throw new RuntimeException("Primitive should be wrapped in an object class");

			FileOutputStream fileOStream = new FileOutputStream(file);
			DataOutputStream ostream = new DataOutputStream(fileOStream);
			LinkedList<Object> antiRecurstionList = new LinkedList<Object>();
			writeObject(o, ostream, antiRecurstionList);
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
	 */
	public static <T> T readObject(T o, File file) {
		FileInputStream fileIStream;
		try {
			fileIStream = new FileInputStream(file);
			DataInputStream istream = new DataInputStream(fileIStream);
			byte[] bytes = new byte[(int) file.length()];
			istream.readFully(bytes);
			ByteBuffer b = ByteBuffer.wrap(bytes);

			readObject(o, b);

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

	private static void writeObject(Object o, DataOutputStream ostream, LinkedList<Object> antiRecurstionList) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (o == null) {
			ostream.write(0);
			return;
		} else
			ostream.write(1);

		if (antiRecurstionList.contains(o))
			return;
		else
			antiRecurstionList.add(o);

		Class<?> c = o.getClass();
		do {
			for (Field f : c.getDeclaredFields()) {
				Class<?> type = f.getType();
				int modifiers = f.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;

				if (type.isPrimitive()) {
					writePrimitive(o, f, ostream);
				} else if (type.isArray()) {
					writeArray(o, f, ostream, antiRecurstionList);
				} else {
					f.setAccessible(true);
					Object obj = f.get(o);
					writeObject(obj, ostream, antiRecurstionList);
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

	private static void writeArray(Object o, Field f, DataOutputStream ostream, LinkedList<Object> antiRecursionList) throws IllegalArgumentException, IllegalAccessException, IOException {
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
			if (type.getComponentType() == Object.class) {
				throw new RuntimeException("Cannot parse generic Object[] array");
			}
			ostream.writeInt(length);
			for (Object obj : (Object[]) f.get(o)) {
				writeObject(obj, ostream, antiRecursionList);
			}
		}
	}

	private static boolean readObject(Object o, ByteBuffer b) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		if (b.get() == 0)
			return false;
		Class<?> c = o.getClass();
		do {
			for (Field f : c.getDeclaredFields()) {
				Class<?> type = f.getType();
				int modifiers = f.getModifiers();
				if (!ignoreTransient && Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;

				if (type.isPrimitive()) {
					readPrimitive(o, f, b);
				} else if (type.isArray()) {
					readArray(o, f, b);
				} else {
					f.setAccessible(true);
					Object obj = f.get(o);
					if (obj == null) {
						obj = createNewInstance(f.getType());
						f.set(o, obj);
					}
					readObject(obj, b);
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);
		return true;
	}

	private static void readArray(Object o, Field f, ByteBuffer b) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
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
			if(componentType.isArray())
				throw new RuntimeException("array arrays are not supported([][])");
			f.set(o, Array.newInstance(componentType, length));
			for (int i = 0; i < length; i++) {
				Object element = createNewInstance(componentType);
				if(readObject(componentType.cast(element), b))
					((Object[]) f.get(o))[i] = element;
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
