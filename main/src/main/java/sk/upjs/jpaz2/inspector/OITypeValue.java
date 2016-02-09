package sk.upjs.jpaz2.inspector;

/**
 * The encapsulation for class type.
 */
class OITypeValue {
	/**
	 * Class type
	 */
	Class<?> type;
	
	/**
	 * Construct a wrapped class type.
	 * @param type the class type
	 */
	public OITypeValue(Class<?> type) {
		this.type = type;
	}
}
