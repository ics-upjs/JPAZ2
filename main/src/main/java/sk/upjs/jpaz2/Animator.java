package sk.upjs.jpaz2;

/**
 * The animator interface that realizes changes of animated objects.
 */
public interface Animator {

	/**
	 * Changes the state of object with respect current phase of animation.
	 * 
	 * @param fraction
	 *            the animation fraction - a real number between 0 and 1.
	 */
	public void animate(double fraction);

}
