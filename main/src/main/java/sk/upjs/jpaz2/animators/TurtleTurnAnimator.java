package sk.upjs.jpaz2.animators;

import sk.upjs.jpaz2.Turtle;

/**
 * Turn animator for turtles.
 */
public class TurtleTurnAnimator extends TurnAnimator {

	/**
	 * The animated turtle.
	 */
	private final Turtle turtle;

	/**
	 * Constructs turn animator.
	 * 
	 * @param turtle
	 *            the animated turtle.
	 * @param startDirection
	 *            the initial direction in degrees when animation starts
	 * @param endDirection
	 *            the final direction in degrees after animation
	 * @param clockwise
	 *            the direction of rotation (turning).
	 */
	public TurtleTurnAnimator(Turtle turtle, double startDirection, double endDirection, boolean clockwise) {
		super(startDirection, endDirection, clockwise);
		if (turtle == null) {
			throw new NullPointerException("The animated object cannot be null.");
		}
		this.turtle = turtle;
	}

	@Override
	public void animate(double fraction) {
		turtle.setDirection(computeDirection(fraction));
	}
}
