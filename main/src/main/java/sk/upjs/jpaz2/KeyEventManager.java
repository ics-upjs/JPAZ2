package sk.upjs.jpaz2;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.SwingUtilities;

/**
 * Internal class designed for proper generation of repeated key pressed event
 * independently of OS.
 */
abstract class KeyEventManager {

    private static class HoldKeyCodeRecord {
	long lastFire;
	Object source;

	public HoldKeyCodeRecord(long lastFire, Object source) {
	    this.lastFire = lastFire;
	    this.source = source;
	}
    }

    /**
     * Period in milliseconds in which repeated key pressed events are generated
     * (0 for OS repeated strategy)
     */
    private long repeatPeriod = 0;

    /**
     * Specific repeat periods (in milliseconds) for particular key codes <Key
     * code, Repeat period>
     */
    private Map<Integer, Long> repeatPeriodsPerKeys;

    /**
     * Map that for each currently hold key code stores time when key pressed
     * event was fired last time and source of the first key pressed event
     */
    private Map<Integer, HoldKeyCodeRecord> holdKeys;

    /**
     * Runnable for JPAZ scheduler
     */
    private Runnable keyPressRunnable = null;

    /**
     * Modifiers of last received key event
     */
    private int lastModifiers = 0;

    /**
     * Constructs a new KeyEventManager
     */
    public KeyEventManager() {
	repeatPeriodsPerKeys = new HashMap<Integer, Long>();
	holdKeys = new HashMap<Integer, HoldKeyCodeRecord>();

	keyPressRunnable = new Runnable() {
	    public void run() {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			fireRepeatedKeyPressed();
		    }
		});
	    }
	};
    }

    /**
     * Processes a key event. This methods is called from Pane objects.
     */
    public void processKeyEvent(int type, KeyEvent evt) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    lastModifiers = evt.getModifiers();

	    // if repeat period is 0 and there are no keys with specific repeat
	    // period, we just call fireKeyEvent
	    if ((repeatPeriod == 0) && (repeatPeriodsPerKeys.isEmpty())) {
		fireKeyEvent(type, evt);
		return;
	    }

	    // KEY_TYPED - only forwarded to fireKeyEvent
	    if (type == KeyEvent.KEY_TYPED) {
		fireKeyEvent(type, evt);
		return;
	    }

	    // KEY_RELEASED - forwarded to fireKeyEvent and underlying key code
	    // is
	    // marked as released
	    if (type == KeyEvent.KEY_RELEASED) {
		holdKeys.remove(evt.getKeyCode());
		fireKeyEvent(type, evt);
		return;
	    }

	    // KEY_PRESSED
	    if (type == KeyEvent.KEY_PRESSED) {
		// event is stopped for any key code that is hold and a special
		// repeat period should be applied for this key code
		boolean shouldBeStopped = holdKeys.containsKey(evt.getKeyCode())
			&& ((repeatPeriod > 0) || (repeatPeriodsPerKeys.containsKey(evt.getKeyCode())));

		if (!shouldBeStopped) {
		    holdKeys.put(evt.getKeyCode(), new HoldKeyCodeRecord(System.currentTimeMillis(), evt.getSource()));
		    scheduleNextRepeatedFire();
		    fireKeyEvent(type, evt);
		}
	    }
	}
    }

    /**
     * Schedules next fire of repeated key pressed events.
     */
    private void scheduleNextRepeatedFire() {
	if (holdKeys.isEmpty())
	    return;

	// compute time of next key pressed event that will respect defined
	// repeat periods
	long nextRepeatedFire = Long.MAX_VALUE;
	for (Map.Entry<Integer, HoldKeyCodeRecord> holdKeyCode : holdKeys.entrySet()) {
	    int keyCode = holdKeyCode.getKey();
	    if (repeatPeriodsPerKeys.containsKey(keyCode)) {
		nextRepeatedFire = Math.min(nextRepeatedFire, holdKeyCode.getValue().lastFire
			+ repeatPeriodsPerKeys.get(keyCode));
	    } else if (repeatPeriod > 0)
		nextRepeatedFire = Math.min(nextRepeatedFire, holdKeyCode.getValue().lastFire + repeatPeriod);
	}

	// if there is no need for a repeated key pressed event, we are done
	if (nextRepeatedFire == Long.MAX_VALUE)
	    return;

	long delay = nextRepeatedFire - System.currentTimeMillis();

	if (delay > 0)
	    JPAZUtilities.getScheduler().schedule(keyPressRunnable, nextRepeatedFire - System.currentTimeMillis(),
		    TimeUnit.MILLISECONDS);
	else
	    fireRepeatedKeyPressed();
    }

    /**
     * Called by scheduler and invoked inside EDT in order to fire all required
     * repeated key pressed events.
     */
    private void fireRepeatedKeyPressed() {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    long currentTime = System.currentTimeMillis();

	    // Find key codes for which repeated key pressed event should be
	    // fired now
	    ArrayList<Integer> firedKeyCodes = new ArrayList<Integer>();
	    for (Map.Entry<Integer, HoldKeyCodeRecord> holdKeyCode : holdKeys.entrySet()) {
		int keyCode = holdKeyCode.getKey();

		// compute next (expected) fire of repeated key pressed event
		long nextFireTime = -1;
		if (repeatPeriodsPerKeys.containsKey(keyCode)) {
		    nextFireTime = holdKeyCode.getValue().lastFire + repeatPeriodsPerKeys.get(keyCode);
		} else if (repeatPeriod > 0)
		    nextFireTime = holdKeyCode.getValue().lastFire + repeatPeriod;

		if ((nextFireTime >= 0) && (nextFireTime <= currentTime))
		    firedKeyCodes.add(keyCode);
	    }

	    // Fire key pressed event for all found key codes
	    for (int keyCode : firedKeyCodes) {
		HoldKeyCodeRecord holdKeyCodeRecord = holdKeys.get(keyCode);

		fireKeyEvent(KeyEvent.KEY_PRESSED, new KeyEvent((Component) holdKeyCodeRecord.source, KeyEvent.KEY_PRESSED,
			currentTime, lastModifiers, keyCode, KeyEvent.CHAR_UNDEFINED));
		holdKeyCodeRecord.lastFire = currentTime;
	    }

	    // Schedule next fire
	    scheduleNextRepeatedFire();
	}
    }

    /**
     * Returns period (in milliseconds) used for generating repeated key pressed
     * events of key codes that are currently hold (0 for handling by JRE).
     */
    public long getRepeatPeriod() {
	return repeatPeriod;
    }

    /**
     * Sets period (in milliseconds) used for generating repeated key pressed
     * events of key codes that are currently hold (0 for handling by JRE).
     */
    public void setRepeatPeriod(long repeatPeriod) {
	if (repeatPeriod < 0)
	    this.repeatPeriod = 0;
	else
	    this.repeatPeriod = repeatPeriod;
    }

    /**
     * Returns period (in milliseconds) used for generating repeated key pressed
     * events of a given key code (0 for global repeat strategy).
     */
    public void setRepeatPeriod(int keyCode, long period) {
	if (period <= 0)
	    repeatPeriodsPerKeys.remove(keyCode);
	else
	    repeatPeriodsPerKeys.put(keyCode, period);
    }

    /**
     * Sets period (in milliseconds) used for generating repeated key pressed
     * events of a given key code (0 for global repeat strategy).
     */
    public long getRepeatPeriod(int keyCode) {
	Long period = repeatPeriodsPerKeys.get(keyCode);
	if (period != null)
	    return period;
	else
	    return 0;
    }

    /**
     * Abstract method called to handle generated key events (overridden by
     * Panes)
     */
    abstract void fireKeyEvent(int type, KeyEvent evt);
}
