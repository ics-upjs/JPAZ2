package sk.upjs.snowflakes;

import sk.upjs.jpaz2.*;

/**
 * A boy catching snowflakes.
 */
public class Boy extends Turtle {

	/**
	 * Indicates that the boy is turned left.
	 */
	private boolean isLeft = true;

	/**
	 * Minimal X-coordinate of a position of the boy.
	 */
	private static final int MIN_X = 85;

	/**
	 * Maximal X-coordinate of a position of the boy.
	 */
	private static final int MAX_X = 595;

	/**
	 * X-coordinate of relative position of tongue center with respect to the
	 * center of the boy shape.
	 */
	private static final int TONGUE_CENTER_DX = -41;

	/**
	 * Y-coordinate of relative position of tongue center with respect to the
	 * center of the boy shape.
	 */
	private static final int TONGUE_CENTER_DY = -17;

	/**
	 * Maximal distance of a snowflake to the center of boy's tongue that allows
	 * to eat the snowflake.
	 */
	private static final int EAT_DISTANCE = 20;

	/**
	 * Constructs a new boy catching snowflakes.
	 */
	public Boy() {
		// set shape
		ImageShape.Builder builder = new ImageShape.Builder("images", "boy.png");
		builder.setViewCount(2);
		setShape(builder.createShape());
	}

	/**
	 * Turns boy to left or does one step left.
	 */
	public void stepLeft() {
		setViewIndex(0);
		if ((isLeft) && (getX() - 15 >= MIN_X))
			setX(getX() - 15);
		isLeft = true;
	}

	/**
	 * Turns boy to right or does one step right.
	 */
	public void stepRight() {
		setViewIndex(1);
		if ((!isLeft) && (getX() + 15 <= MAX_X))
			setX(getX() + 15);
		isLeft = false;
	}

	/**
	 * Returns whether a given snowflake can be eaten by this boy.
	 * 
	 * @param snowflake
	 *            a snowflake.
	 * @return true, if this boy can eat this snowflake.
	 */
	public boolean canEat(Snowflake snowflake) {
		// compute coordinates of the center of boy's tongue
		double tongueCenterY = getY() + TONGUE_CENTER_DY;
		double tongueCenterX;
		if (isLeft)
			tongueCenterX = getX() + TONGUE_CENTER_DX;
		else
			tongueCenterX = getX() - TONGUE_CENTER_DX;

		// compute distance from tongue center to snowflake and check whether it
		// is less than 20
		return (snowflake.distanceTo(tongueCenterX, tongueCenterY) < EAT_DISTANCE);
	}

}
