package sk.upjs.jpaz2.animators;

import java.util.List;

import sk.upjs.jpaz2.Animator;

/**
 * Represents animator that is a composition of animator.
 */
public class CompositeAnimator implements Animator {
	/**
	 * Animators forming the composition.
	 */
	private final Animator[] animators;

	/**
	 * Constructs the composite animator from sequence of animators.
	 * 
	 * @param animators
	 *            the sequence of animators.
	 */
	public CompositeAnimator(Animator... animators) {
		if (animators == null) {
			throw new NullPointerException("The array of animators cannot be null.");
		}

		if (animators.length == 0) {
			throw new IllegalArgumentException("The animator must consist of at least one animator.");
		}

		this.animators = new Animator[animators.length];
		for (Animator animator : animators) {
			if (animator == null) {
				throw new NullPointerException("No animator in the composition cannot be null.");
			}
		}
		System.arraycopy(animators, 0, this.animators, 0, animators.length);
	}

	/**
	 * Constructs the animator from list of animators.
	 * 
	 * @param animators
	 *            the list of animators.
	 */
	public CompositeAnimator(List<Animator> animators) {
		this(animators.toArray(new Animator[animators.size()]));
	}

	@Override
	public void animate(double fraction) {
		for (Animator animator : animators) {
			animator.animate(fraction);
		}
	}
}
