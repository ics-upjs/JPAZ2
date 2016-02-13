package sk.upjs.snowflakes;

import sk.upjs.jpaz2.*;

/**
 * A class representing a falling snowflake. It stores the speed of the falling
 * snowflake.
 */
public class Snowflake extends Turtle {

	/**
	 * Speed of the falling snowflake.
	 */
	private double speed = 5;

	/**
	 * Returns the speed of the snowflake.
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed of the snowflake.
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Realizes one falling step.
	 */
	public void doFallingStep() {
		setY(getY() + speed);
	}
}
