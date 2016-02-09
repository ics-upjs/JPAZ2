package sk.upjs.jpaz2;

import java.util.concurrent.*;

import javax.swing.SwingUtilities;

/**
 * Timer that ticks in a given period. Ticks are realized as call of the onTick
 * method. All methods are synchronized with JPAZ.
 */
public class TickTimer {

	/**
	 * Period in which onTick method is executed. 0 for disabled periodical call
	 * of the onTick method.
	 */
	private long tickPeriod = 0;

	/**
	 * Whether activity of the TickTimer is enabled;
	 */
	private boolean enabled = true;

	/**
	 * Determines whether all onTick methods are invoked in the Swing's EDT
	 */
	private boolean swingSynchronization = true;

	/**
	 * Indicates that the onTick method is currently in execution. We use this
	 * indication to suspend invalidation requests during onTick execution.
	 */
	private boolean tickExecutionInProgress = false;

	/**
	 * Runnable for calling the onTick method
	 */
	private Runnable tickRunnable = null;

	/**
	 * A Future object containing the next scheduled onTick call.
	 */
	private ScheduledFuture<?> nextTickFuture = null;

	/**
	 * Name of this TickTimer;
	 */
	private String name;

	/**
	 * Constructs a new timer with the onTick method invoked within Swing's EDT.
	 */
	public TickTimer() {
		this(null, true);
	}

	/**
	 * Constructs a new timer with a given name and the onTick method invoked
	 * within Swing's EDT
	 * 
	 * @param name
	 *            the desired name of this tick timer. The name is used in error
	 *            messages when an exception from onTick is caught.
	 */
	public TickTimer(String name) {
		this(name, true);
	}

	/**
	 * Constructs a new timer with a given method of synchronization with
	 * Swing's EDT.
	 * 
	 * @param synchronizeWithSwing
	 *            true, for onTick invoked within Swing's EDT, false otherwise
	 */
	public TickTimer(boolean synchronizeWithSwing) {
		this(null, synchronizeWithSwing);
	}

	/**
	 * Constructs a new timer with a given name and a given method of
	 * synchronization with Swing's EDT.
	 * 
	 * @param name
	 *            the desired name of this tick timer. The name is used in error
	 *            messages when an exception from onTick is caught.
	 * 
	 * @param synchronizeWithSwing
	 *            true, for onTick invoked within Swing's EDT, false otherwise
	 */
	public TickTimer(String name, boolean synchronizeWithSwing) {
		if (name == null) {
			name = this.toString();
		}

		this.name = name;
		swingSynchronization = synchronizeWithSwing;

		// create a proper runnable
		if (synchronizeWithSwing) {
			tickRunnable = new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							callOnTick();
						}
					});
				}
			};
		} else {
			tickRunnable = new Runnable() {
				public void run() {
					callOnTick();
				}
			};
		}
	}

	/**
	 * Gets the time period (in milliseconds) in which the onTick method is
	 * executed. The value 0 indicates disabled periodical execution of the
	 * onTick method.
	 * 
	 * @return the tick period in milliseconds.
	 */
	public long getTickPeriod() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return tickPeriod;
		}
	}

	/**
	 * Sets the time period (in milliseconds) in which the onTick method is
	 * executed. The value 0 indicates disabled periodical execution of the
	 * onTick method.
	 * 
	 * @param tickPeriod
	 *            the tick period in milliseconds.
	 */
	public void setTickPeriod(long tickPeriod) {
		if (tickPeriod < 0) {
			tickPeriod = 0;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.tickPeriod == tickPeriod) {
				return;
			}

			this.tickPeriod = tickPeriod;

			// cancel scheduling of the next tick
			if (nextTickFuture != null) {
				nextTickFuture.cancel(false);
				nextTickFuture = null;
			}

			scheduleNextTick();
		}
	}

	/**
	 * Returns whether the onTick method is invoked within Swing's EDT.
	 * 
	 * @return true, if the timer is synchronized with Swing
	 */
	public boolean isSynchronizedWithSwing() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return swingSynchronization;
		}
	}

	/**
	 * Returns whether tick timer is enabled
	 * 
	 * @return true, if tick timer is enabled, false otherwise
	 */
	public boolean isEnabled() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return enabled;
		}
	}

	/**
	 * Sets activity of the tick timer.
	 * 
	 * @param enabled
	 *            true, to enabled, false to disable timer fuctionality
	 */
	public void setEnabled(boolean enabled) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.enabled == enabled)
				return;

			this.enabled = enabled;
			scheduleNextTick();
		}
	}

	/**
	 * Realizes a call of the onTick method (called in EDT)
	 */
	private void callOnTick() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((tickPeriod <= 0) || (!enabled))
				return;

			tickExecutionInProgress = true;
			nextTickFuture = null;

			try {
				onTick();
			} catch (Throwable e) {
				System.err.println("An exception from the onTick method of "
						+ name + " catched: " + e);
			}

			tickExecutionInProgress = false;
			scheduleNextTick();
		}
	}

	/**
	 * Schedules next invocation of the onTick method.
	 */
	private void scheduleNextTick() {
		if ((tickPeriod > 0) && (!tickExecutionInProgress) && (enabled)) {
			nextTickFuture = JPAZUtilities.getScheduler().schedule(
					tickRunnable, tickPeriod, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Called periodically by the pane. The period of calling is determined by
	 * the property tickPeriod.
	 */
	protected void onTick() {
		// Handling code
	}
}
