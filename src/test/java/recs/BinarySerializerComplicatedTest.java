package recs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

public class BinarySerializerComplicatedTest {

	private static class ComplicatedObject {
		public long someLong;
		public HashMap<Short, Long> someHashMap;
		SomeInterface nullInterface;
		SomeInterface someInterface;
		SomeInterface[] someInterfaceArray;

		public ComplicatedObject(long someLong) {
			this.someLong = someLong;

			someHashMap = new HashMap<Short, Long>(4);
			someInterface = new SomeInterfaceObject((int) someLong);
			someInterfaceArray = new SomeInterface[4];

			someHashMap.put((short) 1, someLong);
			someInterfaceArray[0] = new SomeInterfaceObject((int) someLong + 1);
			someInterfaceArray[1] = new SomeInterfaceObject((int) someLong + 2);
			//2 is null
			someInterfaceArray[3] = someInterfaceArray[0]; //3 is reference
		}
	}
	private static class SomeInterfaceObject implements SomeInterface {
		int i = 42;

		public SomeInterfaceObject(int i) {
			this.i = i;
		}

		@Override
		public int foo() {
			return i;
		}
	}
	private static class AnotherInterfaceObject implements SomeInterface {
		long l;

		public AnotherInterfaceObject(long l) {
			this.l = l;
		}

		@Override
		public int foo() {
			return (int) l;
		}

	}
	private static interface SomeInterface {
		public int foo();
	}


	private void assertEqualsComplicatedObjects(ComplicatedObject o1, ComplicatedObject o2) {
		assertEquals(o1.someLong, o2.someLong);

		assertTrue(o1.someHashMap.keySet().size() == o2.someHashMap.keySet().size());
		for (Short key: o1.someHashMap.keySet()) {
			assertTrue(o2.someHashMap.containsKey(key));

			assertNull(o1.nullInterface);
			assertNull(o2.nullInterface);

			assertNotNull(o1.someInterface);
			assertNotNull(o2.someInterface);

			assertEquals(o1.someInterfaceArray.length, o2.someInterfaceArray.length);

			assertNull(o1.someInterfaceArray[2]);
			assertNull(o2.someInterfaceArray[2]);
			for (int i = 0; i < o1.someInterfaceArray.length; ++i) {
				if (i == 2)
					continue; // 2 is null;
				assertEquals(o1.someInterfaceArray[i].foo(), o2.someInterfaceArray[i].foo());
			}

			// 3 is reference to 0;
			assertEquals(o1.someInterfaceArray[3], o1.someInterfaceArray[0]);
			assertEquals(o2.someInterfaceArray[3], o2.someInterfaceArray[0]);

			assertEquals(o1.someInterface.foo(), o2.someInterface.foo());

			Long l1 = o1.someHashMap.get(key);
			Long l2 = o2.someHashMap.get(key);

			assertEquals(l1, l2);
		}
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

	private static final int HASHMAP_NUM_ITEMS = 5;
	private static final int ARRAYLIST_NUM_ITEMS = 5;
	//lower number because stackoverflow
	private static final int LINKEDLIST_NUM_ITEMS = 10;
	private static final int OBJECTMAP_NUM_ITEMS = 10;
	private static final int INTMAP_NUM_ITEMS = 10;
	private static final int ARRAY_NUM_ITEMS = 10;
	private static final int ARRAYARRAY_NUM_PER_ARRAY = 10;

	@Test
	public void testBasic() {
		ComplicatedObject object = new ComplicatedObject(42);

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, object);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ComplicatedObject loadedObject = BinarySerializer.readObject(testFile, new ComplicatedObject(0));
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Basic save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Basic load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		assertEqualsComplicatedObjects(loadedObject, object);
	}

	@Test
	public void testHashMap() {
		System.out.println("HashMap test, num items: " + HASHMAP_NUM_ITEMS);

		HashMap<String, ComplicatedObject> map = new HashMap<String, ComplicatedObject>(HASHMAP_NUM_ITEMS);
		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			map.put("" + i, new ComplicatedObject(i));
		}
		//put a reference to the element in [1] in [3]
		map.put("" + 3, map.get("" + 1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		HashMap<String, ComplicatedObject> loadedMap = BinarySerializer.readObject(testFile, new HashMap<String, ComplicatedObject>(), String.class, ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("HashMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("HashMap load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedMap.get("" + i);
			ComplicatedObject object = map.get("" + i);

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}

	@Test
	public void testArrayList() {
		System.out.println("ArrayList test, num items: " + ARRAYLIST_NUM_ITEMS);

		ArrayList<ComplicatedObject> list = new ArrayList<ComplicatedObject>(ARRAYLIST_NUM_ITEMS);
		for (int i = 0; i < ARRAYLIST_NUM_ITEMS; ++i) {
			list.add(new ComplicatedObject(i));
		}
		//put a reference to the element in [1] in [3]
		list.set(3, list.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, list);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ArrayList<ComplicatedObject> loadedList = BinarySerializer.readObject(testFile, new ArrayList<ComplicatedObject>(), ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Arraylist save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Arraylist load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < ARRAYLIST_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedList.get(i);
			ComplicatedObject object = list.get(i);

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}

	@Test
	public void testLinkedList() {
		System.out.println("LinkedList test, num items: " + LINKEDLIST_NUM_ITEMS);

		LinkedList<ComplicatedObject> queue = new LinkedList<ComplicatedObject>();
		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			queue.offer(new ComplicatedObject(i));
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, queue);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		LinkedList<ComplicatedObject> loadedQueue = BinarySerializer.readObject(testFile, new LinkedList<ComplicatedObject>(), ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("LinkedList save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("LinkedList load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < LINKEDLIST_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedQueue.poll();
			ComplicatedObject object = queue.poll();

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}


	@Test
	public void testObjectMap() {
		System.out.println("ObjectMap test, num items: " + OBJECTMAP_NUM_ITEMS);

		ObjectMap<Integer, ComplicatedObject> map = new ObjectMap<Integer, ComplicatedObject>(OBJECTMAP_NUM_ITEMS);
		for (int i = 0; i < OBJECTMAP_NUM_ITEMS; ++i) {
			map.put(i, new ComplicatedObject(i));
		}
		//put a reference to the element in [1] in [3]
		map.put(3, map.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ObjectMap<Integer, ComplicatedObject> loadedMap = BinarySerializer.readObject(testFile, new ObjectMap<Integer, ComplicatedObject>(), Integer.class, ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("ObjectMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("ObjectMap load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < OBJECTMAP_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedMap.get(i);
			ComplicatedObject object = map.get(i);

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}


	@Test
	public void testIntMap() {
		System.out.println("IntMap test, num items: " + INTMAP_NUM_ITEMS);

		IntMap<ComplicatedObject> map = new IntMap<ComplicatedObject>(INTMAP_NUM_ITEMS);
		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			map.put(i, new ComplicatedObject(i));
		}
		//put a reference to the element in [1] in [3]
		map.put(3, map.get(1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, map);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		IntMap<ComplicatedObject> loadedMap = BinarySerializer.readObject(testFile, new IntMap<ComplicatedObject>(), ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("IntMap save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("IntMap load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedMap.get(i);
			ComplicatedObject object = map.get(i);

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}

	@Test
	public void testArray() {
		System.out.println("Array test, num items: " + ARRAY_NUM_ITEMS);

		ComplicatedObject[] array = new ComplicatedObject[ARRAY_NUM_ITEMS];

		for (int i = 0; i < ARRAY_NUM_ITEMS; ++i) {
			array[i] = new ComplicatedObject(i);
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ComplicatedObject[] loadedArray = BinarySerializer.readObject(testFile, new ComplicatedObject[0], ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("Array save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("Array load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < INTMAP_NUM_ITEMS; ++i) {
			ComplicatedObject loadedObject = loadedArray[i];
			ComplicatedObject object = array[i];

			assertEqualsComplicatedObjects(loadedObject, object);
		}
	}

	@Test
	public void testArrayArray() {
		System.out.println("ArrayArray test, num items: " + ARRAYARRAY_NUM_PER_ARRAY * ARRAYARRAY_NUM_PER_ARRAY);

		ComplicatedObject[][] array = new ComplicatedObject[ARRAYARRAY_NUM_PER_ARRAY][ARRAYARRAY_NUM_PER_ARRAY];

		for (int i = 0; i < ARRAYARRAY_NUM_PER_ARRAY; ++i) {
			for (int j = 0; j < ARRAYARRAY_NUM_PER_ARRAY; j++) {
				array[i][j] = new ComplicatedObject(i);
			}
		}

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, array);
		long saveEndTime = System.currentTimeMillis();

		long loadStartTime = System.currentTimeMillis();
		ComplicatedObject[][] loadedArray = BinarySerializer.readObject(testFile, new ComplicatedObject[0][0], ComplicatedObject.class);
		long loadEndTime = System.currentTimeMillis();

		System.out.println("ArrayArray save time: " + (saveEndTime - saveStartTime) + " ms");
		System.out.println("ArrayArray load time: " + (loadEndTime - loadStartTime) + " ms");
		System.out.println("File size: " + testFile.length() + " bytes");

		for (int i = 0; i < ARRAYARRAY_NUM_PER_ARRAY; ++i) {
			for (int j = 0; j < ARRAYARRAY_NUM_PER_ARRAY; j++) {
				ComplicatedObject loadedObject = loadedArray[i][j];
				ComplicatedObject object = array[i][j];

				assertEqualsComplicatedObjects(loadedObject, object);
			}
		}
	}
}
