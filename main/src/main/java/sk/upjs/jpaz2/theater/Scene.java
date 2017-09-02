package sk.upjs.jpaz2.theater;

import sk.upjs.jpaz2.Pane;

/**
 * Base class for creating scenes displayed on the stage.
 */
public abstract class Scene extends Pane {

	/**
	 * Stage to which the scene belongs.
	 */
	private final Stage stage;

	/**
	 * Constructs a scene that is a part of the stage.
	 * 
	 * @param stage
	 *            the stage.
	 */
	public Scene(Stage stage) {
		super(stage.getWidth(), stage.getHeight());
		this.stage = stage;
	}

	/**
	 * Returns the hosting stage, i.e., a stage to which the scene belongs.
	 * 
	 * @return the hosting stage.
	 */
	public Stage getStage() {
		return stage;
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
