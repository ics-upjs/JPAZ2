package sk.upjs.jpaz2;

import java.util.concurrent.*;

import javax.swing.SwingUtilities;

/**
 * The animation.
 */
public class Animation {

	/**
	 * Runnable implementing animation progress.
	 */
	private class AnimationRunnable implements Runnable {

		/**
		 * Start time of animation.
		 */
		private final long startTime;

		/**
		 * Constructs the runnable that controls animation.
		 */
		public AnimationRunnable() {
			startTime = System.nanoTime();
		}

		@Override
		public void run() {
			long animationTime = (System.nanoTime() - startTime) / 1_000_000;
			double fraction = Math.min(1.0, animationTime / (double) duration);

			synchronized (lock) {
				if (stopped) {
					return;
				}

				animator.animate(fraction);
			}

			if (fraction >= 1.0) {
				stop();
			}
		}
	}

	/**
	 * Runnable that implements completion of animation.
	 */
	private class CompletionRunnable implements Runnable {

		@Override
		public void run() {
			Runnable finalizer = null;
			synchronized (lock) {
				animator.animate(1.0);
				finalizer = Animation.this.finalizer;
			}

			if (finalizer != null) {
				try {
					finalizer.run();
				} catch (Exception ignore) {

				}
			}

			synchronized (lock) {
				completed = true;
				lock.notifyAll();
			}
		}
	}

	/**
	 * Duration of animation in milliseconds.
	 */
	private final long duration;

	/**
	 * Tick interval in milliseconds.
	 */
	private final long tickInterval;

	/**
	 * Animator.
	 */
	private final Animator animator;

	/**
	 * Scheduled ticks.
	 */
	private ScheduledFuture<?> scheduledTicks;

	/**
	 * Indicates whether the animation has been stopped.
	 */
	private boolean stopped;

	/**
	 * Indicates whether the animation has been completed.
	 */
	private boolean completed;

	/**
	 * Finalizer that is asynchronously executed when the application is
	 * completed.
	 */
	private Runnable finalizer;

	/**
	 * Internal synchronization lock.
	 */
	private final Object lock = new Object();

	/**
	 * Constructs animation.
	 * 
	 * @param duration
	 *            the duration of animation in milliseconds.
	 * @param animator
	 *            the animator.
	 */
	public Animation(long duration, Animator animator) {
		this(duration, animator, 50);
	}

	/**
	 * Constructs animation.
	 * 
	 * @param duration
	 *            the duration of animation in milliseconds.
	 * @param animator
	 *            the animator.
	 * @param tickInterval
	 *            the tick interval in milliseconds.
	 */
	private Animation(long duration, Animator animator, long tickInterval) {
		if (duration < 0) {
			throw new IllegalArgumentException("The duration must be a positive value.");
		}

		if (tickInterval <= 0) {
			throw new IllegalArgumentException("The duration must be a positive value.");
		}

		if (animator == null) {
			throw new NullPointerException("The animator cannot be null.");
		}

		this.duration = duration;
		this.animator = animator;
		this.tickInterval = tickInterval;
	}

	/**
	 * Returns duration of the animation.
	 * 
	 * @return the duration in milliseconds.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Returns the animator of the animation.
	 * 
	 * @return the animator.
	 */
	public Animator getAnimator() {
		return animator;
	}

	/**
	 * Set {@link Runnable} that is asynchronously executed when the animation
	 * is completed.
	 * 
	 * @param finalizer
	 *            the desired finalizer.
	 */
	public void setFinalizer(Runnable finalizer) {
		synchronized (lock) {
			if (isStarted()) {
				throw new IllegalStateException("Finalizer cannot be changed after start of the animation.");
			}

			this.finalizer = finalizer;
		}
	}

	/**
	 * Returns the code executed when the animation is completed.
	 * 
	 * @return the {@link Runnable}
	 */
	public Runnable getFinalizer() {
		synchronized (lock) {
			return this.finalizer;
		}
	}

	/**
	 * Starts the animation.
	 */
	public void start() {
		synchronized (lock) {
			if (isStarted()) {
				throw new IllegalStateException("The animation has been started.");
			}

			if (duration == 0) {
				stop();
				return;
			}
			
			scheduledTicks = JPAZUtilities.getScheduler().scheduleWithFixedDelay(new AnimationRunnable(), 0,
					tickInterval, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Stops the animation.
	 */
	public void stop() {
		synchronized (lock) {
			if (stopped) {
				return;
			}

			if (scheduledTicks != null) {
				scheduledTicks.cancel(false);
				scheduledTicks = null;
			}

			stopped = true;
			JPAZUtilities.getScheduler().execute(new CompletionRunnable());
		}
	}

	/**
	 * Returns whether the animation has been completed.
	 * 
	 * @return true, if the animation has been completed, false otherwise.
	 */
	public boolean isCompleted() {
		synchronized (lock) {
			return completed;
		}
	}

	/**
	 * Returns whether the animation has been started.
	 * 
	 * @return true, if the animation has been started, false otherwise.
	 */
	public boolean isStarted() {
		synchronized (lock) {
			return stopped || (scheduledTicks != null);
		}
	}

	/**
	 * Waits for completion of animation.
	 */
	public void waitForCompletion() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("The method cannot be invoked in the event dispatch thread.");
		}

		synchronized (lock) {
			while (!completed) {
				try {
					lock.wait();
				} catch (InterruptedException ignore) {

				}
			}
		}
	}

	/**
	 * Starts the animation and waits for its completion.
	 */
	public void startAndWait() {
		start();
		waitForCompletion();
	}
}
