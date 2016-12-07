package sk.upjs.jpaz2.theater;

import sk.upjs.jpaz2.Turtle;

/**
 * Represents an actor that can appear and play in a scene.
 */
public class Actor extends Turtle {

	/**
	 * Counter for auto-generated names of actors.
	 */
	private static int actorNameCounter = 0;

	/**
	 * Constructs an actor with auto-generated name located at position [0, 0].
	 */
	public Actor() {
		this(0, 0);
	}

	/**
	 * Constructs an actor with auto-generated name located at given position.
	 * 
	 * @param x
	 *            the X-coordinate of the actor's position.
	 * @param y
	 *            the Y-coordinate of the actor's position.
	 */
	public Actor(double x, double y) {
		this(x, y, generateDefaultActorName());
	}

	/**
	 * Constructs a named actor located at given position.
	 * 
	 * @param x
	 *            the X-coordinate of the actor's position.
	 * @param y
	 *            the Y-coordinate of the actor's position.
	 * @param name
	 *            the name of the actor.
	 */
	public Actor(double x, double y, String name) {
		super(x, y, name);
		setPenDown(false);
	}

	/**
	 * Generates the default name for an actor.
	 */
	private static String generateDefaultActorName() {
		actorNameCounter++;
		return "Actor" + actorNameCounter;
	}
}
