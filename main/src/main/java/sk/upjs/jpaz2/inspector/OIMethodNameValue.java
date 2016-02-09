package sk.upjs.jpaz2.inspector;

import java.lang.reflect.Method;

/**
 * The encapsulation for method and method descriptor.
 */
class OIMethodNameValue {
	/**
	 * Encapsulated method
	 */
	Method method;

	/**
	 * Constructs an encapsulated method.
	 * 
	 * @param method
	 *            the method
	 */
	public OIMethodNameValue(Method method) {
		this.method = method;
	}
}
