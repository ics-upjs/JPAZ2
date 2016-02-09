package sk.upjs.jpaz2.inspector;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.SwingUtilities;

/**
 * Thread periodically updating the current property values.
 */
class PropertyUpdateThread extends Thread {
	/**
	 * Refresh period in milliseconds
	 */
	private long updatePeriod = 200;

	/**
	 * List of properties that should be updated
	 */
	private Map<PropertyDescriptor, Object> properties;

	/**
	 * The object whose properties are read
	 */
	private Object object;

	/**
	 * Constructs the thread updating (reading) property values of an object.
	 * 
	 * @param object
	 *            the object whose property values are read
	 * @param properties
	 *            the list of properties that are updated
	 */
	public PropertyUpdateThread(Object object,
			List<PropertyDescriptor> properties) {
		super();

		// prepare map to store current values
		this.object = object;
		this.properties = new HashMap<PropertyDescriptor, Object>();
		for (PropertyDescriptor p : properties)
			this.properties.put(p, new UnknownValue());
	}

	/**
	 * Gets the update period in milliseconds.
	 */
	public synchronized long getUpdatePeriod() {
		return updatePeriod;
	}

	/**
	 * Sets the update period.
	 * 
	 * @param updatePeriod
	 *            the update period in milliseconds
	 */
	public synchronized void setUpdatePeriod(long updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			// update values
			for (PropertyDescriptor p : properties.keySet()) {
				Method readMethod = p.getReadMethod();
				if (readMethod != null) {
					try {
						// invoke read method
						properties.put(p, readMethod.invoke(object,
								new Object[0]));
					} catch (Exception e) {
						// if anything failed, we return unknown value
						properties.put(p, new UnknownValue());
					}
				}

				if (isInterrupted())
					return;
			}

			// notify new values of properties
			notifyNewValues();

			if (isInterrupted())
				return;

			// sleep thread for a given amount of time
			long sleepPeriod = 0;
			synchronized (this) {
				sleepPeriod = updatePeriod;
			}
			try {
				Thread.sleep(sleepPeriod);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Notifies that an update iteration is completed and forces update of
	 * values in EDT
	 */
	private void notifyNewValues() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					updateValuesInSwing(properties);
				}
			});
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Updates values of other components according to map of current values.
	 * This method is always called in EDT while the update thread is blocked.
	 * 
	 * @param values
	 *            the map of current values
	 */
	public void updateValuesInSwing(Map<PropertyDescriptor, Object> values) {
		// update code
	}
}
