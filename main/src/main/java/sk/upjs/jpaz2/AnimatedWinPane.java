package sk.upjs.jpaz2;

/**
 * The {@link WinPane} that configures all turtles to have animated move-turn
 * actions.
 */
public class AnimatedWinPane extends WinPane {

	/**
	 * Animation speeds.
	 */
	public enum AnimationSpeed {
		NONE(0.0), SLOW(10.0), NORMAL(5.0), FAST(2.5), SUPER_FAST(1);

		/**
		 * Move-turn speed of turtles.
		 */
		private final double speed;

		/**
		 * Constructs predefined animation speed.
		 * 
		 * @param speed
		 *            the move-turn speed of turtles in the pane.
		 */
		private AnimationSpeed(double animationFactor) {
			this.speed = animationFactor;
		}
	}

	/**
	 * Animation speed of move-turn actions of turtles in the pane.
	 */
	private AnimationSpeed animationSpeed = AnimationSpeed.NORMAL;

	/**
	 * Constructs a new animated pane inside a window with predefined size
	 */
	public AnimatedWinPane() {
		super();
	}

	/**
	 * Constructs a new animated pane inside a window at a given position and with a
	 * given title.
	 * 
	 * @param x
	 *            the x-coordinate of the window top-left corner
	 * @param y
	 *            the y-coordinate of the window top-left corner
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 * @param title
	 *            the title displayed in the frame's border
	 */
	public AnimatedWinPane(int x, int y, int width, int height, String title) {
		super(x, y, width, height, title);
	}

	/**
	 * Constructs a new animated pane inside a window at a given position.
	 * 
	 * @param x
	 *            the x-coordinate of the window top-left corner
	 * @param y
	 *            the y-coordinate of the window top-left corner
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public AnimatedWinPane(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	/**
	 * Constructs a new animated pane inside a window with a given title.
	 * 
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 * @param title
	 *            the title displayed in the frame's border
	 */
	public AnimatedWinPane(int width, int height, String title) {
		super(width, height, title);
	}

	/**
	 * Constructs a new animated pane inside a window.
	 * 
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public AnimatedWinPane(int width, int height) {
		super(width, height);
	}

	@Override
	public void add(PaneObject o) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			super.add(o);
			configureTurtle((Turtle) o);
		}
	}

	/**
	 * Configures the turtle with respect to global settings.
	 * 
	 * @param t
	 *            the turtle to be configured.
	 */
	private void configureTurtle(Turtle t) {
		if (t == null) {
			return;
		}

		t.setMoveTurnSpeed(animationSpeed.speed);
	}

	/**
	 * Returns the animation speed.
	 * 
	 * @return the speed of animation.
	 */
	public AnimationSpeed getAnimationSpeed() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return animationSpeed;
		}
	}

	/**
	 * Sets the animation speed of move-turn operations.
	 * 
	 * @param animationSpeed
	 *            the speed of animations.
	 */
	public void setAnimationSpeed(AnimationSpeed animationSpeed) {
		if (animationSpeed == null) {
			animationSpeed = AnimationSpeed.NONE;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.animationSpeed == animationSpeed) {
				return;
			}

			this.animationSpeed = animationSpeed;
			for (Turtle t : getTurtles()) {
				t.setMoveTurnSpeed(animationSpeed.speed);
			}
		}
	}
}
