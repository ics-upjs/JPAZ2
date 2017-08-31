package sk.upjs.jpaz2.animators;

import java.awt.geom.Point2D;

import sk.upjs.jpaz2.Animator;

public abstract class MoveAnimator implements Animator {

	/**
	 * Points forming trajectory.
	 */
	private final Point2D[] trajectoryPoints;

	/**
	 * Distances between neighboring points.
	 */
	private final double[] joinLengths;

	/**
	 * The length of the trajectory.
	 */
	private final double trajectoryLength;

	/**
	 * Constructs animator for a path defined by given set of points.
	 * 
	 * @param points
	 *            the points.
	 */
	protected MoveAnimator(Point2D... points) {
		if ((points == null) || (points.length < 2)) {
			throw new NullPointerException("Path must be formed by at least 2 points.");
		}

		// copy points
		trajectoryPoints = new Point2D[points.length];
		for (int i = 0; i < this.trajectoryPoints.length; i++) {
			trajectoryPoints[i] = new Point2D.Double(points[i].getX(), points[i].getY());
		}

		// compute distances between points
		joinLengths = new double[points.length - 1];
		for (int i = 0; i < joinLengths.length; i++) {
			joinLengths[i] = trajectoryPoints[i + 1].distance(trajectoryPoints[i]);
		}

		// compute total length of the trajectory
		double sum = 0;
		for (double joinLength : joinLengths) {
			sum += joinLength;
		}
		this.trajectoryLength = sum;

		// normalize join lengths
		if (trajectoryLength > 0) {
			for (int i = 0; i < joinLengths.length; i++) {
				joinLengths[i] = joinLengths[i] / trajectoryLength;
			}
		}
	}

	/**
	 * Returns total length of the movement trajectory.
	 * 
	 * @return the total length of trajectory.
	 */
	public double getTrajectoryLength() {
		return trajectoryLength;
	}

	/**
	 * Computes position that corresponds to given animation fraction.
	 * 
	 * @param fraction
	 *            the animation fraction.
	 * @return the corresponding position.
	 */
	protected Point2D computePosition(double fraction) {
		if ((fraction <= 0) || (trajectoryLength <= 0)) {
			return trajectoryPoints[0];
		}

		if (fraction >= 1.0) {
			return trajectoryPoints[trajectoryPoints.length - 1];
		}

		int joinIdx = 0;
		while ((joinIdx < joinLengths.length) && (fraction > joinLengths[joinIdx])) {
			fraction -= joinLengths[joinIdx];
			joinIdx++;
		}

		if (joinIdx >= joinLengths.length) {
			return trajectoryPoints[trajectoryPoints.length - 1];
		}

		double dx = trajectoryPoints[joinIdx + 1].getX() - trajectoryPoints[joinIdx].getX();
		double dy = trajectoryPoints[joinIdx + 1].getY() - trajectoryPoints[joinIdx].getY();
		double joinFraction = (joinLengths[joinIdx] > 0) ? fraction / joinLengths[joinIdx] : 0;
		return new Point2D.Double(trajectoryPoints[joinIdx].getX() + dx * joinFraction,
				trajectoryPoints[joinIdx].getY() + dy * joinFraction);
	}
}
