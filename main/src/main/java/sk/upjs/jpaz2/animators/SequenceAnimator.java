package sk.upjs.jpaz2.animators;

import java.util.List;
import sk.upjs.jpaz2.Animator;

/**
 * Animator that is formed as sequence of other animators.
 */
public class SequenceAnimator implements WeightedAnimator {

	/**
	 * Animator fraction of a sequence animator.
	 */
	public static class AnimatorFraction {
		/**
		 * Sequence number of corresponding animator in the sequence.
		 */
		public int sequenceNumber;

		/**
		 * The corresponding animator.
		 */
		public Animator animator;

		/**
		 * Fractional part of the animator.
		 */
		public double fraction;

		/**
		 * Constructs the animator fraction.
		 * 
		 * @param sequenceNumber
		 *            the sequence number of animator.
		 * @param animator
		 *            the animator.
		 * @param fraction
		 *            the fraction of animation (with respect to the animator).
		 */
		public AnimatorFraction(int sequenceNumber, Animator animator, double fraction) {
			this.sequenceNumber = sequenceNumber;
			this.animator = animator;
			this.fraction = fraction;
		}
	}

	/**
	 * Animators forming the sequence.
	 */
	private final Animator[] animators;

	/**
	 * Normalized weights of animators in the sequence.
	 */
	private final double[] weights;

	/**
	 * Total weight of animators.
	 */
	private final long totalWeight;

	/**
	 * Constructs the sequence animator from animator.
	 * 
	 * @param animator
	 *            the animator.
	 * @param weight
	 *            the weight of the animator.
	 */
	public SequenceAnimator(Animator animator, long weight) {
		if (animator == null) {
			throw new NullPointerException("The animator cannot be null.");
		}

		if (weight < 0) {
			throw new IllegalArgumentException("The weight must be positive number.");
		}

		animators = new Animator[1];
		animators[0] = animator;

		weights = new double[0];
		weights[0] = 1.0;

		totalWeight = weight;
	}

	/**
	 * Constructs the sequence animator from array of weighted animators.
	 * 
	 * @param animators
	 *            the array of weighted animators.
	 */
	public SequenceAnimator(WeightedAnimator[] animators) {
		if (animators == null) {
			throw new NullPointerException("The array of animators cannot be null.");
		}

		if (animators.length == 0) {
			throw new IllegalArgumentException("Sequence animator must be consist of at least one animator.");
		}

		this.animators = new Animator[animators.length];
		for (Animator animator : animators) {
			if (animator == null) {
				throw new NullPointerException("No animator in the sequence cannot be null.");
			}
		}
		System.arraycopy(animators, 0, this.animators, 0, animators.length);

		this.weights = new double[animators.length];
		long sumOfWeights = 0;
		for (int i = 0; i < animators.length; i++) {
			long weight = animators[i].getWeight();
			if (weight < 0) {
				throw new IllegalArgumentException("The weight of animator must be non-negative.");
			}

			weights[i] = weight;
			sumOfWeights += weight;
		}

		this.totalWeight = sumOfWeights;
		for (int i = 0; i < weights.length; i++) {
			weights[i] = weights[i] / sumOfWeights;
		}
	}

	/**
	 * Constructs the sequence animator from list of weighted animators.
	 * 
	 * @param animators
	 *            the list of weighted animators.
	 */
	public SequenceAnimator(List<WeightedAnimator> animators) {
		this(animators.toArray(new WeightedAnimator[animators.size()]));
	}

	@Override
	public void animate(double fraction) {
		AnimatorFraction animatorFraction = getAnimatorForFraction(fraction);
		for (int i = 0; i < animatorFraction.sequenceNumber; i++) {
			animators[i].animate(1.0);
		}

		animatorFraction.animator.animate(animatorFraction.fraction);
	}

	/**
	 * Returns animator with other details for given fraction of the sequence
	 * animator.
	 * 
	 * @param fraction
	 *            the fraction.
	 * @return details about animation fraction.
	 */
	public AnimatorFraction getAnimatorForFraction(double fraction) {
		if (fraction <= 0) {
			return new AnimatorFraction(0, animators[0], 0);
		}

		if ((fraction >= 1.0) || (totalWeight == 0)) {
			return new AnimatorFraction(animators.length - 1, animators[animators.length - 1], 1.0);
		}

		int idx = 0;
		while ((idx < weights.length) && (fraction > weights[idx])) {
			fraction -= weights[idx];
			idx++;
		}

		if (idx >= weights.length) {
			idx = weights.length - 1;
		}

		// normalize fraction with respect to animator
		if (weights[idx] == 0) {
			fraction = 1.0;
		} else {
			fraction = fraction / weights[idx];
		}

		return new AnimatorFraction(idx, animators[idx], fraction);
	}

	/**
	 * Returns the animator in the sequence.
	 * 
	 * @param index
	 *            the index of the animator.
	 * @return the animator.
	 */
	public Animator getAnimator(int index) {
		if ((index >= 0) && (index < animators.length)) {
			return animators[index];
		} else {
			return null;
		}
	}

	@Override
	public long getWeight() {
		return totalWeight;
	}
}
