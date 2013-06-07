package recs.core.utils;

import java.util.Arrays;

/**
 * @author Enrico van Oosten
 */
public class RECSBits {
	private static final int WORD_MASK = 0xffffffff;
	int[] bits = { 0 };

	/**
	 * @param index
	 *            the index of the bit
	 * @return whether the bit is set
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index < 0
	 */
	public boolean get(int index) {
		final int word = index >>> 5;
		if (word >= bits.length)
			return false;
		return (bits[word] & (1 << (index & 31))) != 0;
	}

	/**
	 * @param index
	 *            the index of the bit to set
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index < 0
	 */
	public void set(int index) {
		final int word = index >>> 5;
		growWord(word);
		bits[word] |= 1 << (index & 31);
	}

	/**
	 * @param index
	 *            the index of the bit to flip
	 */
	public void flip(int index) {
		final int word = index >>> 5;
		bits[word] ^= 1 << (index & 31);
	}

	private void grow(int nrBits) {
		if (nrBits > bits.length * 32 - 1) {
			int[] newBits = new int[(int) Math.ceil(nrBits / 32)];
			System.arraycopy(bits, 0, newBits, 0, bits.length);
			bits = newBits;
		}
	}

	public void setWord(int wordNr, int word) {
		growWord(wordNr);
		bits[wordNr] = word;
	}

	private void growWord(int wordCount) {
		if (wordCount > bits.length) {
			int[] newBits = new int[wordCount];
			System.arraycopy(bits, 0, newBits, 0, bits.length);
			bits = newBits;
		}
	}

	/**
	 * @param index
	 *            the index of the bit to clear
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index < 0
	 */
	public void clear(int index) {
		final int word = index >>> 5;
		if (word >= bits.length)
			return;
		bits[word] &= ~(1 << (index & 31));
	}

	/**
	 * Clears the entire bitset
	 */
	public void clear() {
		int length = bits.length;
		for (int i = 0; i < length; i++) {
			bits[i] = 0;
		}
	}

	public void copy(RECSBits other) {
		bits = Arrays.copyOf(other.bits, other.bits.length);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		RECSBits otherBits = (RECSBits) other;
		if (bits.length != otherBits.bits.length)
			return false;
		for (int i = 0; i < bits.length; i++) {
			if (bits[i] != otherBits.bits[i])
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int total = Integer.MIN_VALUE;
		for (int i = 0; i < bits.length; i++)
			total += bits[i];
		return total;
	}

	public void add(RECSBits other) {
		int otherLength = other.bits.length;
		if (bits.length < otherLength)
			growWord(otherLength);
		for (int i = 0; i < otherLength; i++) {
			bits[i] = bits[i] | other.bits[i];
		}
	}

	/**
	 * Check if the other also has the bits of this Bits set.
	 *
	 * @param other
	 *            The other Bits.
	 * @return if it contains all the true bits of this Bits.
	 */
	public boolean contains(RECSBits other) {
		for (int i = 0; i < bits.length; i++)
			if ((bits[i] & other.bits[i]) != bits[i])
				return false;
		return true;
	}

	/**
	 * @return the number of bits currently stored, <b>not</b> the highset set
	 *         bit!
	 */
	public int numBits() {
		return bits.length << 5;
	}

	/**
	 * Get the bits that were altered by adding another RECSBits to this one.
	 *
	 * @param otherBits
	 * @return
	 */
	public RECSBits getAddedBits(RECSBits otherBits) {
		assert otherBits.contains(this) : "bits were not contained";

		RECSBits addedBits = new RECSBits();
		for (int i = 0, max = otherBits.bits.length; i < max; i++) {
			if (i > bits.length)
				addedBits.setWord(i, otherBits.bits[i]);
			else
				addedBits.setWord(i, bits[i] ^ otherBits.bits[i]);
		}
		return addedBits;
	}

	/**
	 * Get the number of bits set to true.
	 *
	 * @return
	 */
	public int cardinality() {
		int sum = 0;
		for (int i = 0; i < bits.length; i++) {
			sum += Integer.bitCount(bits[i]);
		}
		return sum;
	}

	/**
	 * Returns the index of the first bit that is set to true that occurs on or
	 * after the specified starting index. If no such bit exists then -1 is
	 * returned.
	 *
	 * To iterate over the true bits in a BitSet, use the following loop:
	 *
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
	 * //operate on index i here }
	 *
	 * @param fromIndex
	 * @return
	 */
	public int nextSetBit(int fromIndex) {
		int wordIndex = fromIndex * 32;
		if (wordIndex >= bits.length)
			return -1;

		int word = bits[wordIndex] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0)
				return (wordIndex * 32) + Integer.numberOfTrailingZeros(word);
			if (++wordIndex == bits.length)
				return -1;
			word = bits[wordIndex];
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < bits.length; i++) {
			for (int j = 0; j < 32; j++) {
				if (get(j + i * 32)) {
					b.append((j + i * 32) + ", ");
				}
			}
		}
		return b.toString();
	}
}
