package Exp3;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class run {
	
	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public static void main(String[] args) throws Exception {
		
		final Runnable beeper = new Runnable() {
			public void run() {
				Hierarchy2.run();
			}
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
				beeper, 0, 10*60, TimeUnit.SECONDS);
		scheduler.schedule(new Runnable() {
			public void run() {
				beeperHandle.cancel(true);
			}
		}, 10000, TimeUnit.SECONDS);
	}
}
