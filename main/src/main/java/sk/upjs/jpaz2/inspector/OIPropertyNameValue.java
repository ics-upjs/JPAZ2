package sk.upjs.jpaz2.inspector;

import java.beans.PropertyDescriptor;

/**
 * The encapsulation for property descriptor.
 */
class OIPropertyNameValue {
	/**
	 * Property descriptor
	 */
	PropertyDescriptor pd;

	/**
	 * Constructs a property name value
	 * 
	 * @param pd
	 *            the property descriptor
	 */
	public OIPropertyNameValue(PropertyDescriptor pd) {
		this.pd = pd;
	}
}
