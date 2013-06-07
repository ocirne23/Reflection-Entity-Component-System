package recs.core.utils;

import java.util.Arrays;

/**
 * A bitset, with size limitation, allows comparison via bitwise operators to
 * other bitfields.
 *
 * @author mzechner
 * @author Enrico van Oosten
 */
public class RECSBits {
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
		if(other == null) return false;
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
		for(int i = 0; i < bits.length; i++)
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
	    System.out.println("Checking if: " + other.toString() + ":" + toString());
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
	
	@Override
	public String toString() {
	    StringBuilder b = new StringBuilder();
	    for(int i = 0; i < bits.length; i++) {
	        for(int j = 0; j < 32; j++) {
	            if(get(j + i * 32)) {
	                b.append((j + i * 32) +", ");
	            }
	        }
	    }
	    return b.toString();
	}
}
