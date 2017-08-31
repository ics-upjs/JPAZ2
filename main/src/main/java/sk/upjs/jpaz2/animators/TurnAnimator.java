package sk.upjs.jpaz2.animators;

import sk.upjs.jpaz2.JPAZUtilities;

/**
 * Base class for turn animators.
 */
public abstract class TurnAnimator implements WeightedAnimator {

	/**
	 * Initial direction when animation starts.
	 */
	private double startDirection;

	/**
	 * Final direction after animation.
	 */
	private double endDirection;

	/**
	 * Change of angle during animation.
	 */
	private double angleChange;

	/**
	 * Constructs turn animator.
	 * 
	 * @param startDirection
	 *            the initial direction in degrees when animation starts
	 * @param endDirection
	 *            the final direction in degrees after animation
	 * @param clockwise
	 *            the direction of rotation (turning).
	 */
	protected TurnAnimator(double startDirection, double endDirection, boolean clockwise) {
		this.startDirection = JPAZUtilities.normalizeAngleInDegrees(startDirection);
		this.endDirection = JPAZUtilities.normalizeAngleInDegrees(endDirection);
		if (clockwise) {
			this.angleChange = JPAZUtilities.normalizeAngleInDegrees(endDirection - startDirection);
		} else {
			this.angleChange = -JPAZUtilities.normalizeAngleInDegrees(startDirection - endDirection);
		}
	}

	/**
	 * Returns change of angle in degrees during the turn animation.
	 * 
	 * @return the change of angle in degrees.
	 */
	public double getAngleChange() {
		return Math.abs(angleChange);
	}

	@Override
	public long getWeight() {
		return Math.round(getAngleChange());
	}

	/**
	 * Computes direction that corresponds to given animation fraction.
	 * 
	 * @param fraction
	 *            the animation fraction.
	 * @return the corresponding angle in degrees.
	 */
	protected double computeDirection(double fraction) {
		if (fraction <= 0) {
			return startDirection;
		}

		if (fraction >= 1.0) {
			return endDirection;
		}

		return JPAZUtilities.normalizeAngleInDegrees(startDirection + (fraction * angleChange));
	}
}
