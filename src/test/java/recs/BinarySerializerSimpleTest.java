package recs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.utils.BinarySerializer;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

public class BinarySerializerSimpleTest {

	private static class SimpleObject {
		public int someInt;
		public boolean someBoolean;
		public float someFloat;

		public SimpleObject(int someInt, boolean someBoolean, float someFloat) {
			this.someInt = someInt;
			this.someBoolean = someBoolean;
			this.someFloat = someFloat;
		}
	}

	private void assertEqualsSimpleObjects(SimpleObject o1, SimpleObject o2) {
		assertEquals(o1.someInt, o2.someInt);
		assertEquals(o1.someBoolean, o2.someBoolean);
		assertEquals(o1.someFloat, o2.someFloat, 0.00001f);
	}


	public File testFile;

	@Before
	public void setup() {
		testFile = new File("savertestfile");
	}

	@After
	public void breakDown() {
		testFile.delete();
		System.out.println("done");
	}


	private static final int HASHMAP_NUM_ITEMS = 10;
	//lower number because stackoverflow
	private static final int LINKEDLIST_NUM_ITEMS = 10;
	private static final int ARRAYLIST_NUM_ITEMS = 10;
	private static final int OBJECTMAP_NUM_ITEMS = 10;
	private static final int INTMAP_NUM_ITEMS = 10;
	private static final int ARRAY_NUM_ITEMS = 10;
	private static final int ARRAYARRAY_NUM_PER_ARRAY = 10;

	@Test
	public void testBasic() {
		SimpleObject object = new SimpleObject(42, true, 0.5f);

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, object);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		SimpleObject loadedObject = BinarySerializer.readFromFile(testFile, new SimpleObject(0, false, 0));
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Basic save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Basic load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		assertEqualsSimpleObjects(loadedObject, object);
	}

	@Test
	public void testHashMap() {
		System.out.println("HashMap test, num items: " + HASHMAP_NUM_ITEMS);

		HashMap<String, SimpleObject> map = new HashMap<String, SimpleObject>(HASHMAP_NUM_ITEMS);
		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			map.put("" + i, new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		//put a reference to the element in [1] in [3]
		map.put("" + 3, map.get("" + 1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		HashMap<String, SimpleObject> loadedMap = BinarySerializer.readFromFile(testFile, new HashMap<String, SimpleObject>(), String.class, SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("HashMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("HashMap load time: " + (loadEndTime - loadStartTime) + " ms");

		System.out.println("File size: " + testFile.length() + " bytes");
		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get("" + i);
			SimpleObject object = map.get("" + i);

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testArrayList() {
		System.out.println("ArrayList test, num items: " + ARRAYLIST_NUM_ITEMS);

		ArrayList<SimpleObject> list = new ArrayList<SimpleObject>(ARRAYLIST_NUM_ITEMS);
		for (int i = 0; i < ARRAYLIST_NUM_ITEMS; ++i) {
			list.add(new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		//put a reference to the element in [1] in [3]
		list.set(3, list.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, list);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ArrayList<SimpleObject> loadedList = BinarySerializer.readFromFile(testFile, new ArrayList<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Arraylist save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Arraylist load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < ARRAYLIST_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedList.get(i);
			SimpleObject object = list.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testLinkedList() {
		System.out.println("LinkedList test, num items: " + LINKEDLIST_NUM_ITEMS);

		LinkedList<SimpleObject> queue = new LinkedList<SimpleObject>();
		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			queue.offer(new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, queue);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		LinkedList<SimpleObject> loadedQueue = BinarySerializer.readFromFile(testFile, new LinkedList<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("LinkedList save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("LinkedList load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedQueue.poll();
			SimpleObject object = queue.poll();

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testObjectMap() {
		System.out.println("ObjectMap test, num items: " + OBJECTMAP_NUM_ITEMS);

		ObjectMap<Integer, SimpleObject> map = new ObjectMap<Integer, SimpleObject>(OBJECTMAP_NUM_ITEMS);
		for (int i = 0; i < OBJECTMAP_NUM_ITEMS; ++i) {
			map.put(i, new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		//put a reference to the element in [1] in [3]
		map.put(3, map.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ObjectMap<Integer, SimpleObject> loadedMap = BinarySerializer.readFromFile(testFile, new ObjectMap<Integer, SimpleObject>(), Integer.class, SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("ObjectMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("ObjectMap load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < OBJECTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get(i);
			SimpleObject object = map.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testIntMap() {
		System.out.println("IntMap test, num items: " + INTMAP_NUM_ITEMS);

		IntMap<SimpleObject> map = new IntMap<SimpleObject>(INTMAP_NUM_ITEMS);
		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			map.put(i, new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		//put a reference to the element in [1] in [3]
		map.put(3, map.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		IntMap<SimpleObject> loadedMap = BinarySerializer.readFromFile(testFile, new IntMap<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("IntMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("IntMap load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get(i);
			SimpleObject object = map.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testArray() {
		System.out.println("Array test, num items: " + ARRAY_NUM_ITEMS);

		SimpleObject[] array = new SimpleObject[ARRAY_NUM_ITEMS];

		for (int i = 0; i < ARRAY_NUM_ITEMS; ++i) {
			array[i] = new SimpleObject(i, (i % 2) == 0, i * 0.5f);
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		SimpleObject[] loadedArray = BinarySerializer.readFromFile(testFile, new SimpleObject[0], SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Array save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Array load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedArray[i];
			SimpleObject object = array[i];

			assertEqualsSimpleObjects(loadedObject, object);
		}
	}

	@Test
	public void testPrimitive() {
		System.out.println("Primitive test, testing double");

		double primitive = 5;

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, primitive);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		double loadedPrimitive = BinarySerializer.readFromFile(testFile, new Double(0), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Primitive save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Primitive load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		assertEquals(primitive, loadedPrimitive, 0.0001f);
	}

	@Test
	public void testPrimitiveArray() {
		System.out.println("Primitive Array test, type: double, num items: " + ARRAY_NUM_ITEMS);

		double[] array = new double[ARRAY_NUM_ITEMS];

		for (int i = 0; i < ARRAY_NUM_ITEMS; ++i) {
			array[i] = i * 0.5f;
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		double[] loadedArray = BinarySerializer.readFromFile(testFile, new double[0], SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Primitive Array save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Primitive Array load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			assertEquals(array[i], loadedArray[i], 0.00001f);
		}
	}

	@Test
	public void testArrayArray() {
		System.out.println("ArrayArray test, num items: " + ARRAYARRAY_NUM_PER_ARRAY * ARRAYARRAY_NUM_PER_ARRAY);

		SimpleObject[][] array = new SimpleObject[ARRAYARRAY_NUM_PER_ARRAY][ARRAYARRAY_NUM_PER_ARRAY];

		for (int i = 0; i < ARRAYARRAY_NUM_PER_ARRAY; ++i) {
			for (int j = 0; j < ARRAYARRAY_NUM_PER_ARRAY; j++) {
				array[i][j] = new SimpleObject(i, (i % 2) == 0, i * 0.5f);
			}
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveToFile(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		SimpleObject[][] loadedArray = BinarySerializer.readFromFile(testFile, new SimpleObject[0][0], SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("ArrayArray save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("ArrayArray load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < ARRAYARRAY_NUM_PER_ARRAY; ++i) {
			for (int j = 0; j < ARRAYARRAY_NUM_PER_ARRAY; j++) {
				SimpleObject loadedObject = loadedArray[i][j];
				SimpleObject object = array[i][j];

				assertEqualsSimpleObjects(loadedObject, object);
			}
		}
	}
}
