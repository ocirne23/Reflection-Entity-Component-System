package recs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.utils.BinarySerializer;

public class BinarySerializerComplicatedTest {

	public File testFile;

	@Before
	public void setup() {
		testFile = new File("savertestfile");
	}

	private static final int HASHMAP_NUM_ITEMS = 1;

	@Test
	public void testHashMap() {
		System.out.println("HashMap test, num items: " + HASHMAP_NUM_ITEMS);

		HashMap<String, ComplicatedObject> map = new HashMap<String, ComplicatedObject>(HASHMAP_NUM_ITEMS);
		for (int i = 0; i < HASHMAP_NUM_ITEMS; ++i) {
			map.put("" + i, new ComplicatedObject(i));
		}
		//put a reference to the element in [1] in [3]
		//map.put("" + 3, map.get("" + 1));

		long saveStartTime = System.currentTimeMillis();
		BinarySerializer.saveObject(testFile, map, String.class, ComplicatedObject.class);
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

	@After
	public void breakDown() {
		testFile.delete();
		System.out.println("done");
	}

	private void assertEqualsComplicatedObjects(ComplicatedObject o1, ComplicatedObject o2) {
		System.out.println(o1 +":"+ o2);
		assertEquals(o1.someLong, o2.someLong);

		assertTrue(o1.someHashMap.keySet().size() == o2.someHashMap.keySet().size());

		for (Short key: o1.someHashMap.keySet()) {
			assertTrue(o2.someHashMap.containsKey(key));

			Long l1 = o1.someHashMap.get(key);
			Long l2 = o2.someHashMap.get(key);

			assertEquals(l1, l2);
		}
	}

	private static class ComplicatedObject {
		public long someLong;

		public HashMap<Short, Long> someHashMap;

		public ComplicatedObject(long someLong) {
			this.someLong = someLong;
			someHashMap = new HashMap<Short, Long>(4);
			someHashMap.put((short) 1, someLong);
		}
	}
}
