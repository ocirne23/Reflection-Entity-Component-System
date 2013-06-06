package recs.core.utils;
/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


import java.util.Random;

/** Utility and fast math functions.
 * <p>
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/atan2/floor/ceil.
 * @author Nathan Sweet */
public class RECSMathUtils {
	static public final float nanoToSec = 1 / 1000000000f;

	static public Random random = new Random();

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public final int random (int range) {
		return random.nextInt(range + 1);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public final int random (int start, int end) {
		return start + random.nextInt(end - start + 1);
	}

	/** Returns a random boolean value. */
	static public final boolean randomBoolean () {
		return random.nextBoolean();
	}

	/** Returns random number between 0.0 (inclusive) and 1.0 (exclusive). */
	static public final float random () {
		return random.nextFloat();
	}

	/** Returns a random number between 0 (inclusive) and the specified value (exclusive). */
	static public final float random (float range) {
		return random.nextFloat() * range;
	}

	// ---

	/** Returns the next power of two. Returns the specified value if the value is already a power of two. */
	static public int nextPowerOfTwo (int value) {
		if (value == 0) return 1;
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}
}
