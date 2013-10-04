package recs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.utils.Saver;

public class SaverSimpleTest {

	public File testFile;

	@Before
	public void setup() {
		testFile = new File("savertestfile");
	}

	@Test
	public void testHashMap() {
		System.out.println("HashMap test");

		HashMap<String, SimpleObject> map = new HashMap<String, SimpleObject>(4);
		for (int i = 0; i < 20; ++i) {
			map.put("" + i, new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		map.put("" + 3, map.get("" + 1));

		Saver.saveObject(testFile, map);

		HashMap<String, SimpleObject> loadedMap = Saver.readObject(testFile, new HashMap<String, SimpleObject>(), String.class, SimpleObject.class);
		for (int i = 0; i < 20; ++i) {
			SimpleObject loadedObject = loadedMap.get("" + i);
			SimpleObject object = map.get("" + i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	@Test
	public void testArrayList() {
		System.out.println("ArrayList test");

		ArrayList<SimpleObject> list = new ArrayList<SimpleObject>();
		for (int i = 0; i < 20; ++i) {
			list.add(new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}
		list.set(3, list.get(1));

		Saver.saveObject(testFile, list);

		ArrayList<SimpleObject> loadedList = Saver.readObject(testFile, new ArrayList<SimpleObject>(), SimpleObject.class);
		for (int i = 0; i < 20; ++i) {
			SimpleObject loadedObject = loadedList.get(i);
			SimpleObject object = list.get(i);

			assertEqualsSimpleObjects(loadedObject, object);
		}

		System.out.println("File size: " + testFile.length() + " bytes");
	}

	@Test
	public void testQueue() {
		System.out.println("Queue test");

		Queue<SimpleObject> queue = new LinkedList<SimpleObject>();
		for (int i = 0; i < 20; ++i) {
			queue.offer(new SimpleObject(i, (i % 2) == 0, i * 0.5f));
		}

		Saver.saveObject(testFile, queue);

		Queue<SimpleObject> loadedQueue = Saver.readObject(testFile, new LinkedList<SimpleObject>(), SimpleObject.class);
		for (int i = 0; i < 20; ++i) {
			SimpleObject loadedObject = loadedQueue.poll();
			SimpleObject object = queue.poll();

			assertEqualsSimpleObjects(loadedObject, object);
		}
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
