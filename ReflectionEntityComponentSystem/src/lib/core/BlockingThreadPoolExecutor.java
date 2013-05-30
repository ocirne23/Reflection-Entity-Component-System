package lib.core;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
	private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(100);

	public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
		super(corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100));
	}

	@Override
	public void execute(Runnable command) {
		if (getQueue().size() < 100) {
			super.execute(command);
		} else {
			try {
				queue.put(command);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		Runnable next = queue.poll();
		if (next != null) {
			super.execute(next);
		}
	}
}
