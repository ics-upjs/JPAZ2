package sk.upjs.jpaz2.animators;

import java.awt.geom.Point2D;

import sk.upjs.jpaz2.Turtle;

/**
 * Move animator for turtles.
 */
public class TurtleMoveAnimator extends MoveAnimator {

	/**
	 * The animated turtle.
	 */
	private final Turtle turtle;

	/**
	 * Constructs the move animator for a turtle along a path defined by given
	 * set of points.
	 * 
	 * @param turtle
	 *            the animated turtle.
	 * @param points
	 *            the trajectory points.
	 */
	public TurtleMoveAnimator(Turtle turtle, Point2D... points) {
		super(points);
		if (turtle == null) {
			throw new NullPointerException("The animated object cannot be null.");
		}
		this.turtle = turtle;
	}

	@Override
	public void animate(double fraction) {
		turtle.moveTo(computePosition(fraction));
	}

}
