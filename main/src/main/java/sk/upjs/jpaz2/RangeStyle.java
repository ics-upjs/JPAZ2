package sk.upjs.jpaz2;

/**
 * The enumeration of supported turtles' range styles. The range style determines what happens
 * if the turtle reaches out of its predefined range while doing a step.
 */
public enum RangeStyle {
	/**
	 * The turtle is bounced from the range border.
	 */
	BOUNCE, 
	/**
	 * The turtle is moved to the position inside the range nearest to the target position.
	 */
	FENCE,
	/**
	 * Range is not considered.
	 */
	WINDOW,
	/**
	 * The turtle disappears on the border and appears on the opposite border side.
	 */
	WRAP
}
