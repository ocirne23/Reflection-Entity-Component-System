package recs.utils;

import java.util.Arrays;

/**
 * Bit container used to efficiently specify components and systems.
 *
 * @author Enrico van Oosten
 */
public class RECSBits {
	private static final int WORD_MASK = 0xffffffff;
	int[] bits = { 0 };

	public RECSBits() {

	}

	/**
	 * Copy constructor
	 */
	public RECSBits(RECSBits copyMe) {
		bits = Arrays.copyOf(copyMe.bits, copyMe.bits.length);
	}


	/**
	 * Returns if the bit at the index was set to true.
	 */
	public boolean get(int index) {
		final int word = index >>> 5;
		if (word >= bits.length)
			return false;
		return (bits[word] & (1 << (index & 31))) != 0;
	}

	/**
	 * Set the bit at the index to true.
	 */
	public void set(int index) {
		final int word = index >>> 5;
		growWord(word);
		bits[word] |= 1 << (index & 31);
	}

	/**
	 * flip the bit at the given index.
	 */
	public void flip(int index) {
		final int word = index >>> 5;
		bits[word] ^= 1 << (index & 31);
	}

	/**
	 * Set the bit at the index to false.
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

	/**
	 * Set this Bits equal to the given Bits.
	 */
	public void copy(RECSBits other) {
		bits = Arrays.copyOf(other.bits, other.bits.length);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof RECSBits))
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

	/**
	 * Add all the true bits of the given Bits to this.
	 */
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
		for (int i = 0; i < bits.length; i++) {
			int word = bits[i];

			//if any bits are set, and the other does not even have that word, return false.
			if (word != 0 && other.bits.length - 1 < i)
				return false;

			if ((word & other.bits[i]) != word)
				return false;
		}
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
		if (!this.contains(otherBits))
			throw new RuntimeException("bits were not contained: " + binaryString() + ":"+ otherBits.binaryString());
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
	 * Get a bitset representing the removed bits between this Bits and the
	 * other.
	 */
	public RECSBits getRemovedBits(RECSBits otherBits) {
		RECSBits addedBits = new RECSBits();
		for (int i = 0, max = Math.max(otherBits.bits.length, bits.length); i < max; i++) {
			if (i > bits.length)
				addedBits.setWord(i, otherBits.bits[i]);
			else if (i > otherBits.bits.length)
				addedBits.setWord(i, bits[i]);
			else
				addedBits.setWord(i, bits[i] ^ otherBits.bits[i]);
		}
		return addedBits;
	}

	/**
	 * Get the number of bits set to true.
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
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) { </br>
	 * //operate on index i here </br> }
	 */
	public int nextSetBit(int fromIndex) {
		int wordIndex = fromIndex / 32;
		if (wordIndex >= bits.length)
			return -1;

		int word = bits[wordIndex] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0)
				return Integer.numberOfTrailingZeros(word) + wordIndex * 32;
			if (++wordIndex == bits.length)
				return -1;
			word = bits[wordIndex];
		}
	}

	/**
	 * Prints a binary representation of this Bits.
	 */
	public String binaryString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < bits.length; i++) {
			b.append("[" + Integer.toBinaryString(bits[i]) + "],");
		}
		return b.toString();
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

	private void setWord(int wordNr, int word) {
		growWord(wordNr);
		bits[wordNr] = word;
	}

	private void growWord(int wordCount) {
		if (wordCount >= bits.length) {
			int[] newBits = new int[wordCount + 1];
			System.arraycopy(bits, 0, newBits, 0, bits.length);
			bits = newBits;
		}
	}
}
