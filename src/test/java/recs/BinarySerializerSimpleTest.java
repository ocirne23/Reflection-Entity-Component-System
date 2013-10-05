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

	public File testFile;

	@Before
	public void setup() {
		testFile = new File("savertestfile");
	}

	private static final int HASHMAP_NUM_ITEMS = 10000;

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
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		HashMap<String, SimpleObject> loadedMap = BinarySerializer.readObject(testFile, new HashMap<String, SimpleObject>(), String.class, SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("HashMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("HashMap load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get("" + i);
			SimpleObject object = map.get("" + i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	private static final int ARRAYLIST_NUM_ITEMS = 10000;

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
		BinarySerializer.saveObject(testFile, list);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ArrayList<SimpleObject> loadedList = BinarySerializer.readObject(testFile, new ArrayList<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Arraylist save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Arraylist load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < ARRAYLIST_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedList.get(i);
			SimpleObject object = list.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	//lower number because stackoverflow
	private static final int LINKEDLIST_NUM_ITEMS = 1000;

	@Test
	public void testLinkedList() {
		System.out.println("LinkedList test, num items: " + LINKEDLIST_NUM_ITEMS);

		LinkedList<SimpleObject> queue = new LinkedList<SimpleObject>();
		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			queue.offer(new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, queue);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		LinkedList<SimpleObject> loadedQueue = BinarySerializer.readObject(testFile, new LinkedList<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("LinkedList save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("LinkedList load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedQueue.poll();
			SimpleObject object = queue.poll();

			assertEqualsSimpleObjects(loadedObject, object);
		}
		System.out.println("File size: " + testFile.length() + " bytes");
	}

	private static final int OBJECTMAP_NUM_ITEMS = 10000;

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
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ObjectMap<Integer, SimpleObject> loadedMap = BinarySerializer.readObject(testFile, new ObjectMap<Integer, SimpleObject>(), Integer.class, SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("ObjectMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("ObjectMap load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < OBJECTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get(i);
			SimpleObject object = map.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	private static final int INTMAP_NUM_ITEMS = 10000;

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
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		IntMap<SimpleObject> loadedMap = BinarySerializer.readObject(testFile, new IntMap<SimpleObject>(), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("IntMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("IntMap load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedMap.get(i);
			SimpleObject object = map.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	private static final int ARRAY_NUM_ITEMS = 10000;

	@Test
	public void testArray() {
		System.out.println("Array test, num items: " + ARRAY_NUM_ITEMS);

		SimpleObject[] array = new SimpleObject[ARRAY_NUM_ITEMS];

		for (int i = 0; i < ARRAY_NUM_ITEMS; ++i) {
			array[i] = new SimpleObject(i, (i % 2) == 0, i * 0.5f);
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		SimpleObject[] loadedArray = BinarySerializer.readObject(testFile, new SimpleObject[0], SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Array save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Array load time: " + (loadEndTime - loadStartTime) + " ms");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			SimpleObject loadedObject = loadedArray[i];
			SimpleObject object = array[i];

			assertEqualsSimpleObjects(loadedObject, object);
		}
		System.out.println("File size: " + testFile.length() + " bytes");
	}

	@Test
	public void testPrimitive() {
		System.out.println("Primitive test");

		double primitive = 5;

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, primitive);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		double loadedPrimitive = BinarySerializer.readObject(testFile, new Double(0), SimpleObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Primitive save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Primitive load time: " + (loadEndTime - loadStartTime) + " ms");

		assertEquals(primitive, loadedPrimitive, 0.0001f);

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	private void assertEqualsSimpleObjects(SimpleObject o1, SimpleObject o2) {
		assertEquals(o1.someInt, o2.someInt);
		assertEquals(o1.someBoolean, o2.someBoolean);
		assertEquals(o1.someFloat, o2.someFloat, 0.00001f);
	}

	@After
	public void breakDown() {
		testFile.delete();
		System.out.println("done");
	}

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

}
