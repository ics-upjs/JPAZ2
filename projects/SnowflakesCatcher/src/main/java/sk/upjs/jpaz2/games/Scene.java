package sk.upjs.jpaz2.games;

import sk.upjs.jpaz2.Pane;

/**
 * Base class for creating scenes of a game.
 */
public abstract class Scene extends Pane {

	/**
	 * Game to which the scene belongs.
	 */
	private final Game game;

	/**
	 * Constructs a scene that is a part of game.
	 * 
	 * @param game
	 *            the game.
	 */
	public Scene(Game game) {
		super(game.getWidth(), game.getHeight());
		this.game = game;
	}

	/**
	 * Returns game to which the scene belongs.
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Starts the scene.
	 */
	public abstract void start();

	/**
	 * Stops the scene.
	 */
	public abstract void stop();
}
