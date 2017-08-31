package sk.upjs.jpaz2.animators;

import sk.upjs.jpaz2.Animator;

/**
 * Weighted animator that adds weight of the animator. The weight can be used to
 * construct complex composite animators.
 */
public interface WeightedAnimator extends Animator {

	/**
	 * Returns weight of the animator.
	 * 
	 * @return the weight of animator. It must be a non-negative number.
	 */
	public long getWeight();

}
