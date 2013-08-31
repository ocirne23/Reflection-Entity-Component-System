package recs.utils;

import recs.utils.libgdx.RECSMathUtils;

/**
 * Modified IntMap from Nathan Sweet to only contain an int table accessable through hashes.
 * Log(N) contains, add and remove.
 * @author Enrico
 */
public class RECSIntSet {
	// private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;
	private static final int EMPTY = 0;

	public int size;

	int[] itemTable;
	int capacity, stashSize;

	private float loadFactor;
	private int hashShift, mask, threshold;
	private int stashCapacity;
	private int pushIterations;

	private Items items1, items2;

	/**
	 * Creates a new map with an initial capacity of 32 and a load factor of
	 * 0.8. This map will hold 25 items before growing the backing table.
	 */
	public RECSIntSet() {
		this(32, 0.8f);
	}

	/**
	 * Creates a new map with a load factor of 0.8. This map will hold
	 * initialCapacity * 0.8 items before growing the backing table.
	 */
	public RECSIntSet(int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/**
	 * Creates a new map with the specified initial capacity and load factor.
	 * This map will hold initialCapacity * loadFactor items before growing the
	 * backing table.
	 */
	public RECSIntSet(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
		if (capacity > 1 << 30)
			throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
		capacity = RECSMathUtils.nextPowerOfTwo(initialCapacity);

		if (loadFactor <= 0)
			throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
		this.loadFactor = loadFactor;

		threshold = (int) (capacity * loadFactor);
		mask = capacity - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
		stashCapacity = Math.max(3, (int) Math.ceil(Math.log(capacity)) * 2);
		pushIterations = Math.max(Math.min(capacity, 8), (int) Math.sqrt(capacity) / 8);

		itemTable = new int[capacity + stashCapacity];
	}

	public void add(int item) {
		if (item == 0)
			throw new RuntimeException("Cannot add item 0");
		// Check for existing items.
		int index1 = item & mask;
		int item1 = itemTable[index1];
		if (item == item1)
			return;

		int index2 = hash2(item);
		int item2 = itemTable[index2];
		if (item == item2)
			return;

		int index3 = hash3(item);
		int item3 = itemTable[index3];
		if (item == item3)
			return;

		// Check for empty buckets.
		if (item1 == EMPTY) {
			itemTable[index1] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}

		if (item2 == EMPTY) {
			itemTable[index2] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}

		if (item3 == EMPTY) {
			itemTable[index3] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}
		push(item, index1, item1, index2, item2, index3, item3);
	}

	public void putAll(int... ints) {
		for (int i = 0; i < ints.length; i++)
			add(i);
	}

	/** Skips checks for existing items. */
	private void putResize(int item) {
		// Check for empty buckets.
		int index1 = item & mask;
		int item1 = itemTable[index1];
		if (item1 == EMPTY) {
			itemTable[index1] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}

		int index2 = hash2(item);
		int item2 = itemTable[index2];
		if (item2 == EMPTY) {
			itemTable[index2] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}

		int index3 = hash3(item);
		int item3 = itemTable[index3];
		if (item3 == EMPTY) {
			itemTable[index3] = item;
			if (size++ >= threshold)
				resize(capacity << 1);
			return;
		}
		push(item, index1, item1, index2, item2, index3, item3);
	}

	private void push(int insertItem, int index1, int item1, int index2, int item2, int index3, int item3) {
		// Push items until an empty bucket is found.
		int evictedItem;
		int i = 0, pushIterations = this.pushIterations;
		do {
			// Replace the item and value for one of the hashes.
			switch (RECSMathUtils.random(2)) {
			case 0:
				evictedItem = item1;
				itemTable[index1] = insertItem;
				break;
			case 1:
				evictedItem = item2;
				itemTable[index2] = insertItem;
				break;
			default:
				evictedItem = item3;
				itemTable[index3] = insertItem;
				break;
			}

			// If the evicted item hashes to an empty bucket, put it there and
			// stop.
			index1 = evictedItem & mask;
			item1 = itemTable[index1];
			if (item1 == EMPTY) {
				itemTable[index1] = evictedItem;
				if (size++ >= threshold)
					resize(capacity << 1);
				return;
			}

			index2 = hash2(evictedItem);
			item2 = itemTable[index2];
			if (item2 == EMPTY) {
				itemTable[index2] = evictedItem;
				if (size++ >= threshold)
					resize(capacity << 1);
				return;
			}

			index3 = hash3(evictedItem);
			item3 = itemTable[index3];
			if (item3 == EMPTY) {
				itemTable[index3] = evictedItem;
				if (size++ >= threshold)
					resize(capacity << 1);
				return;
			}

			if (++i == pushIterations)
				break;
			insertItem = evictedItem;
		} while (true);

		putStash(evictedItem);
	}

	private void putStash(int item) {
		if (stashSize == stashCapacity) {
			// Too many pushes occurred and the stash is full, increase the
			// table size.
			resize(capacity << 1);
			add(item);
			return;
		}
		// Store item in the stash.
		int index = capacity + stashSize;
		itemTable[index] = item;
		stashSize++;
		size++;
	}

	public void remove(int item) {
		int index = item & mask;
		if (itemTable[index] == item) {
			itemTable[index] = EMPTY;
			size--;
			return;
		}

		index = hash2(item);
		if (itemTable[index] == item) {
			itemTable[index] = EMPTY;
			size--;
			return;
		}

		index = hash3(item);
		if (itemTable[index] == item) {
			itemTable[index] = EMPTY;
			size--;
			return;
		}
	}

	void removeStashIndex(int index) {
		// If the removed location was not last, move the last tuple to the
		// removed location.
		stashSize--;
		int lastIndex = capacity + stashSize;
		if (index < lastIndex) {
			itemTable[index] = itemTable[lastIndex];
		}
	}

	public void clear() {
		int[] itemTable = this.itemTable;
		for (int i = capacity + stashSize; i-- > 0;) {
			itemTable[i] = EMPTY;
		}
		size = 0;
		stashSize = 0;
	}

	public boolean contains(int item) {
		int index = item & mask;
		if (itemTable[index] != item) {
			index = hash2(item);
			if (itemTable[index] != item) {
				index = hash3(item);
				if (itemTable[index] != item)
					return containsItemStash(item);
			}
		}
		return true;
	}

	private boolean containsItemStash(int item) {
		int[] itemTable = this.itemTable;
		for (int i = capacity, n = i + stashSize; i < n; i++)
			if (itemTable[i] == item)
				return true;
		return false;
	}

	/**
	 * Increases the size of the backing array to acommodate the specified
	 * number of additional items. Useful before adding many items to avoid
	 * multiple backing array resizes.
	 */
	public void ensureCapacity(int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold)
			resize(RECSMathUtils.nextPowerOfTwo((int) (sizeNeeded / loadFactor)));
	}

	private void resize(int newSize) {
		int oldEndIndex = capacity + stashSize;

		capacity = newSize;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
		stashCapacity = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
		pushIterations = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);

		int[] oldItemTable = itemTable;

		itemTable = new int[newSize + stashCapacity];

		stashSize = 0;
		for (int i = 0; i < oldEndIndex; i++) {
			int item = oldItemTable[i];
			if (item != EMPTY)
				putResize(item);
		}
	}

	private int hash2(int h) {
		h *= PRIME2;
		return (h ^ h >>> hashShift) & mask;
	}

	private int hash3(int h) {
		h *= PRIME3;
		return (h ^ h >>> hashShift) & mask;
	}

	@Override
	public String toString() {
		if (size == 0)
			return "[]";
		StringBuilder buffer = new StringBuilder(itemTable.length * 2);
		buffer.append('[');
		int[] itemTable = this.itemTable;
		int i = itemTable.length;

		while (i-- > 0) {
			int item = itemTable[i];
			if (item == EMPTY)
				continue;
			buffer.append(item);
			buffer.append(',');
		}
		buffer.append(']');
		return buffer.toString();
	}

	/**
	 * Returns an iterator for the items in the map. Remove is supported. Note
	 * that the same iterator instance is returned each time this method is
	 * called. Use the {@link Entries} constructor for nested or multithreaded
	 * iteration.
	 */
	public Items items() {
		if (items1 == null) {
			items1 = new Items(this);
			items2 = new Items(this);
		}
		if (!items1.valid) {
			items1.reset();
			items1.valid = true;
			items2.valid = false;
			return items1;
		}
		items2.reset();
		items2.valid = true;
		items1.valid = false;
		return items2;
	}

	static private class MapIterator {
		static final int INDEX_ILLEGAL = -2;
		static final int INDEX_ZERO = -1;

		public boolean hasNext;
		final RECSIntSet map;
		int nextIndex, currentIndex;
		boolean valid = true;

		public MapIterator(RECSIntSet map) {
			this.map = map;
			reset();
		}

		public void reset() {
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			findNextIndex();
		}

		void findNextIndex() {
			hasNext = false;
			int[] itemTable = map.itemTable;
			for (int n = map.capacity + map.stashSize; ++nextIndex < n;) {
				if (itemTable[nextIndex] != EMPTY && itemTable[nextIndex] != INDEX_ZERO) {
					hasNext = true;
					break;
				}
			}
		}
	}

	static public class Items extends MapIterator {
		public Items(RECSIntSet map) {
			super(map);
		}

		public int next() {
			int item = nextIndex == INDEX_ZERO ? 0 : map.itemTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return item;
		}
	}

	public static void main(String[] args) {
		RECSIntSet da = new RECSIntSet();
		for(int i = 1; i < 22; i++) {
			da.add(i);
		}
		System.out.println(da.itemTable.length);
	}
}
