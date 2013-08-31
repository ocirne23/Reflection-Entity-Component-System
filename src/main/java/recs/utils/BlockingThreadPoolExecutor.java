package main.java.recs.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
	private static final int QUEUE_SIZE = 100;
	/**
	 * Secondary queue
	 */
	private final LinkedBlockingQueue<Runnable> secondary = new LinkedBlockingQueue<Runnable>();

	public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
		super(corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(QUEUE_SIZE));
	}

	@Override
	public void execute(Runnable command) {
		//if internal queue is empty execute immediately, else put in secondary queue.
		if (getQueue().size() == 0) {
			super.execute(command);
		} else {
			try {
				secondary.put(command);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (secondary.size() > 0) {
			Runnable next = secondary.poll();
			if (next != null) {
				super.execute(next);
			}
		}
	}
}
