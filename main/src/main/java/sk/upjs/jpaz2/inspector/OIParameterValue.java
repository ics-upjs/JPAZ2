package sk.upjs.jpaz2.inspector;

/**
 * The encapsulation for a parameter record.
 */
class OIParameterValue {
	/**
	 * The type of parameter
	 */
	Class<?> type;
	
	/**
	 * The name of parameter
	 */
	String name;
	
	/**
	 * Construct a wrapped parameter record.
	 * @param name the name of parameter
	 * @param type the class of parameter 
	 */
	public OIParameterValue(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}
}
