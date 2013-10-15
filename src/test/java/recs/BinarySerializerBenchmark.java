package recs;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import recs.utils.BinarySerializer;



public class BinarySerializerBenchmark {
	private static final int WARMUP_ITERATIONS = 1000;

	/** Number of runs. */
	private static final int RUN_CNT = 1;

	/** Number of iterations. Set it to something rather big for obtaining meaningful results */
// private static final int ITER_CNT = 200000;
	private static final int ITER_CNT = 100;

	private static final int SLEEP_BETWEEN_RUNS = 100;

	SampleObject obj = createObject();

	private static SampleObject createObject () {
		long[] longArr = new long[3000];

		for (int i = 0; i < longArr.length; i++)
			longArr[i] = i;

		double[] dblArr = new double[3000];

		for (int i = 0; i < dblArr.length; i++)
			dblArr[i] = 0.1 * i;

		return new SampleObject(123, 123.456f, (short)321, longArr, dblArr);
	}

	private static class SampleObject {
		private int intVal;
		private float floatVal;
		private Short shortVal;
		private long[] longArr;
		private double[] dblArr;
		private SampleObject selfRef;

		public SampleObject () {
		}

		SampleObject (int intVal, float floatVal, Short shortVal, long[] longArr, double[] dblArr) {
			this.intVal = intVal;
			this.floatVal = floatVal;
			this.shortVal = shortVal;
			this.longArr = longArr;
			this.dblArr = dblArr;

			selfRef = this;
		}

		@Override
		public boolean equals (Object other) {
			if (this == other) return true;

			if (other == null || getClass() != other.getClass()) return false;

			SampleObject obj = (SampleObject)other;

			assert this == selfRef;
			assert obj == obj.selfRef;

			return intVal == obj.intVal && floatVal == obj.floatVal && shortVal.equals(obj.shortVal)
				&& Arrays.equals(dblArr, obj.dblArr) && Arrays.equals(longArr, obj.longArr);
		}
	}

	@Test
	public void benchmark() throws InterruptedException {
		runSerialization(1, WARMUP_ITERATIONS, false);
		runSerialization(RUN_CNT, ITER_CNT, true);
	}

	private void runSerialization (final int RUN_CNT, final int ITER_CNT, boolean outputResults) throws InterruptedException {
		long avgDur = 0;
		long bestTime = Long.MAX_VALUE;

		for (int i = 0; i < RUN_CNT; i++) {
			SampleObject newObj = null;

			long start = System.nanoTime();

			for (int j = 0; j < ITER_CNT; j++) {
				byte[] data = BinarySerializer.saveToByteArr(obj);
				newObj = BinarySerializer.readFromByteArr(data, new SampleObject());
			}

			long dur = System.nanoTime() - start;
			dur = TimeUnit.NANOSECONDS.toMillis(dur);
			// Check that unmarshalled object is equal to original one (should
			// never fail).
			if (!obj.equals(newObj)) throw new RuntimeException("Unmarshalled object is not equal to original object.");

			if (outputResults) System.out.format(">>> serialization (run %d): %,d ms\n", i + 1, dur);
			avgDur += dur;
			bestTime = Math.min(bestTime, dur);
			systemCleanupAfterRun();
		}

		avgDur /= RUN_CNT;

		if (outputResults) {
			System.out.format("\n>>> serialization (average): %,d ms\n\n", avgDur);
			System.out.format("\n>>> serialization (best time): %,d ms\n\n", bestTime);
		}
	}

	private void systemCleanupAfterRun () throws InterruptedException {
		System.gc();
		Thread.sleep(SLEEP_BETWEEN_RUNS);
		System.gc();
	}
}
