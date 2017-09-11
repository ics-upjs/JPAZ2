package sk.upjs.jpaz2;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.swing.*;

/**
 * The class JPAZUtilities contains helper methods for JPAZ framework.
 */
public final class JPAZUtilities {

	// ---------------------------------------------------------------------------------------------------
	// Private constructor
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Private constructor forbidding creation of class instances
	 */
	private JPAZUtilities() {
	}

	// ---------------------------------------------------------------------------------------------------
	// JPAZ locking
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Lock used to coordinate and synchronize all JPAZ related atomic actions
	 */
	private static final Object jpazLock = new Object();

	/**
	 * Returns the object used as synchronization lock for all JPAZ objects. In
	 * order to avoid deadlock and achieving mutual exclusion, all JPAZ objects
	 * use the only shared lock. This object should be used as a lock for each
	 * JPAZ atomic action.
	 * 
	 * @return synchronization the lock object.
	 */
	public static synchronized Object getJPAZLock() {
		return jpazLock;
	}

	// ---------------------------------------------------------------------------------------------------
	// Swing thread-safety
	// ---------------------------------------------------------------------------------------------------

	/**
	 * An interface defining a method for computation an object (a result).
	 */
	static interface Computable {
		/**
		 * Computes a result.
		 * 
		 * @return the computed result.
		 */
		public Object compute();
	}

	/**
	 * A wrapper class for getting a result of a call.
	 */
	private static class ComputationResult {
		/**
		 * Result of a computation.
		 */
		Object value;
	}

	/**
	 * Invokes a Runnable in the EDT. It is a modified version of
	 * SwingUtilities.invokeAndWait. If called from EDT, the Runnable is
	 * executed immediately.
	 * 
	 * @param doRun
	 *            the Runnable object to run
	 */
	static void invokeAndWait(Runnable doRun) {
		if (doRun == null)
			return;

		try {
			if (SwingUtilities.isEventDispatchThread()) {
				doRun.run();
			} else {
				SwingUtilities.invokeAndWait(doRun);
			}
		} catch (Throwable e) {
			throw new RuntimeException("Invocation in Swing EDT failed.", e);
		}
	}

	/**
	 * Invokes a Computable in the EDT. Comparing to Runnable, Computable can
	 * return a value.
	 * 
	 * @param doComputation
	 *            the Computable object to run
	 * @return result of the Computable object's compute method
	 */
	static Object invokeAndWait(final Computable doComputation) {
		if (doComputation == null)
			throw new NullPointerException("doComputation cannot be null.");

		try {
			if (SwingUtilities.isEventDispatchThread()) {
				return doComputation.compute();
			} else {
				final ComputationResult result = new ComputationResult();
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						result.value = doComputation.compute();
					}
				});
				return result.value;
			}
		} catch (Throwable e) {
			throw new RuntimeException("Invocation in Swing EDT failed.", e);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Slowdown
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Blocks the execution for a given amount of time.
	 * 
	 * @param time
	 *            the time in milliseconds.
	 */
	public static void delay(long time) {
		if (isHeadlessMode()) {
			return;
		}

		try {
			Thread.sleep(time);
		} catch (Exception e) {
			// nothing to do (
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Math methods
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Computes cosine of an angle in degrees.
	 * 
	 * @param angle
	 *            the angle.
	 * @return cosine of the angle.
	 */
	static double degreeCos(double angle) {
		if (angle == 0) {
			return 1;
		}
		if (angle == 90) {
			return 0;
		}
		if (angle == 180) {
			return -1;
		}
		if (angle == 270) {
			return 0;
		}

		return Math.cos(Math.toRadians(angle));
	}

	/**
	 * Normalizes angle in degrees.
	 * 
	 * @param angle
	 *            the angle in degrees.
	 * @return the normalized angle.
	 */
	public static double normalizeAngleInDegrees(double angle) {
		angle %= 360.0;
		if (angle < 0) {
			angle += 360.0;
		}

		return angle;
	}

	/**
	 * Computes sines of an angle in degrees.
	 * 
	 * @param angle
	 *            the angle.
	 * @return sines of the angle.
	 */
	static double degreeSin(double angle) {
		if (angle == 0) {
			return 0;
		}
		if (angle == 90) {
			return 1;
		}
		if (angle == 180) {
			return 0;
		}
		if (angle == 270) {
			return -1;
		}

		return Math.sin(Math.toRadians(angle));
	}

	// ---------------------------------------------------------------------------------------------------
	// Switch UI manager
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Changes look-and-feel to Windows look
	 */
	public static void setWindowsLook() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			throw new RuntimeException("Change of look&feel failed.", e);
		}
	}

	/**
	 * Changes look-and-feel to Java look
	 */
	public static void setJavaLook() {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			throw new RuntimeException("Change of look&feel failed.", e);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Settings
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Indicates whether windows shaking is enabled.
	 */
	private static boolean windowShakingEnabled = true;

	/**
	 * Indicates whether smart localization of method invocation frames is
	 * enabled.
	 */
	private static boolean smartLocationEnabled = true;

	/**
	 * Indicates whether headless mode is enabled.
	 */
	private static boolean headlessMode = false;

	/**
	 * Indicates whether headless mode is locked (cannot be changed).
	 */
	private static boolean headlessModeLocked = false;

	/**
	 * Rectangle that defines area where JPAZ-related frames will be located.
	 */
	private static Rectangle screenBounds = null;

	/**
	 * Returns whether windows shaking is enabled.
	 * 
	 * @return true, if windows shaking is enabled; false otherwise.
	 */
	public static boolean isWindowShakingEnabled() {
		synchronized (getJPAZLock()) {
			return windowShakingEnabled;
		}
	}

	/**
	 * Sets windows shaking. If enabled, JPAZ windows are shaked after they get
	 * focus.
	 * 
	 * @param windowShakingEnabled
	 *            true, for enabled shaking; false otherwise.
	 */
	public static void setWindowShakingEnabled(boolean windowShakingEnabled) {
		synchronized (getJPAZLock()) {
			JPAZUtilities.windowShakingEnabled = windowShakingEnabled;
		}
	}

	/**
	 * Returns whether smart location of method invocation windows is enabled.
	 * 
	 * @return true, if smart localization of method invocation windows is
	 *         enabled; false otherwise.
	 */
	public static boolean isSmartLocationEnabled() {
		synchronized (getJPAZLock()) {
			return smartLocationEnabled;
		}
	}

	/**
	 * Sets smart location of method invocation windows
	 * 
	 * @param smartLocationEnabled
	 *            true, for enabled smart location; false otherwise.
	 */
	public static void setSmartLocationEnabled(boolean smartLocationEnabled) {
		synchronized (getJPAZLock()) {
			JPAZUtilities.smartLocationEnabled = smartLocationEnabled;
		}
	}

	/**
	 * Returns whether headless mode is enabled.
	 * 
	 * @return true, if headless mode is enabled; false otherwise.
	 */
	public static boolean isHeadlessMode() {
		synchronized (getJPAZLock()) {
			return headlessMode;
		}
	}

	/**
	 * Sets headless mode. The mode can be set only once.
	 * 
	 * @param headlessMode
	 *            true, to enable headless mode; false to disable.
	 */
	public static void setHeadlessMode(boolean headlessMode) {
		synchronized (getJPAZLock()) {
			if (JPAZUtilities.headlessModeLocked) {
				return;
			}

			JPAZUtilities.headlessMode = headlessMode;
			JPAZUtilities.headlessModeLocked = true;
		}
	}

	/**
	 * Locks the headless mode. After invoking this method, headless mode cannot
	 * be changed.
	 */
	public static void lockHeadlessMode() {
		synchronized (getJPAZLock()) {
			JPAZUtilities.headlessModeLocked = true;
		}
	}

	/**
	 * Defines are where JPAZ related frames/windows should be located.
	 * 
	 * @param boundingRectangle
	 *            the area defined as rectangle or null, if no restrictions are
	 *            applied.
	 */
	public static void setScreenBounds(Rectangle boundingRectangle) {
		synchronized (getJPAZLock()) {
			if (boundingRectangle == null) {
				screenBounds = null;
			} else {
				if ((boundingRectangle.width < 1) || (boundingRectangle.height < 1)) {
					throw new IllegalArgumentException("The bounding rectagle cannot be empty.");
				}

				screenBounds = new Rectangle(boundingRectangle);
			}
		}
	}

	/**
	 * Returns area where JPAZ-related windows/frames should be located.
	 * 
	 * @return the area or null, if no restrictions are applied.
	 */
	public static Rectangle getScreenBounds() {
		synchronized (getJPAZLock()) {
			if (screenBounds == null) {
				return null;
			} else {
				return new Rectangle(screenBounds);
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Resource loading support
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns URL of a resource with given name.
	 * 
	 * @param resourceName
	 *            the string specifying the resource.
	 * @return a URL object for reading the resource, or null if the resource
	 *         could not be found or the invoker doesn't have adequate
	 *         privileges to get the resource.
	 */
	public static URL getResourceAsURL(String resourceName) {
		return JPAZUtilities.class.getResource(resourceName);
	}

	/**
	 * Returns a resource with given name as an open InputStream
	 * 
	 * @param resourceName
	 *            the string specifying the resource.
	 * @return an input stream for reading the resource, or null if the resource
	 *         could not be found.
	 */
	public static InputStream getResourceAsStream(String resourceName) {
		return JPAZUtilities.class.getResourceAsStream(resourceName);
	}

	// ---------------------------------------------------------------------------------------------------
	// Ticking support
	// ---------------------------------------------------------------------------------------------------

	/**
	 * ScheduledThreadPoolExecutor used to schedule and execute timer ticks.
	 */
	private static ScheduledThreadPoolExecutor tickExecutor = null;

	/**
	 * Returns scheduled thread-pool executor used for scheduling and execution
	 * of timer ticks.
	 * 
	 * @return the common ScheduledThreadPoolExecutor object for scheduling
	 *         timer ticks.
	 */
	static ScheduledThreadPoolExecutor getScheduler() {
		synchronized (getJPAZLock()) {
			if (tickExecutor == null) {
				tickExecutor = new ScheduledThreadPoolExecutor(1);
			}

			return tickExecutor;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Demo
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Retrieves reference string from result of {@link Object#toString()}.
	 * 
	 * @param toStringResult
	 *            the result of {@link Object#toString()}.
	 * @return the string with identification of reference to the object.
	 */
	static String retrieveInternalId(String toStringResult) {
		if (toStringResult == null) {
			return null;
		}

		int atIdx = toStringResult.lastIndexOf('@');
		if (atIdx < 0) {
			return toStringResult;
		} else {
			return toStringResult.substring(atIdx + 1);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Demo
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Executes a sample code.
	 */
	public static void sunDemo() {
		// create pane
		WinPane pane = new WinPane(300, 300);
		pane.setBackgroundColor(new Color(135, 206, 250));

		// create turtle and add the turtle to the pane
		Turtle t = new Turtle();
		pane.add(t);
		t.center();

		// draw sun rays
		t.setPenWidth(3);
		t.setPenColor(Color.yellow);
		for (int i = 0; i < 12; i++) {
			t.turn(360 / 12);
			t.step(100);
			t.step(-100);
		}
		t.setFillColor(Color.orange);
		t.dot(70);

		t.penUp();
		t.setPenColor(Color.darkGray);

		// random walk
		t.setDirection(Math.random() * 360.0);
		t.setRangeStyle(RangeStyle.BOUNCE);
		while (true) {
			t.step(10);
			JPAZUtilities.delay(100);
		}
	}
}
