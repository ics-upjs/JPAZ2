package ${package};

import java.awt.Color;
import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * The scene with main content.
 */
public class MainScene extends Scene {

	/**
	 * Identifier (name) of this scene.
	 */
	public static final String NAME = "Main";

	/**
	 * The actor on the stage during this scene.
	 */
	private Actor franklin;

	/**
	 * Constructs the scene.
	 * 
	 * @param stage
	 *            the stage where the scene will be shown.
	 */
	public MainScene(Stage stage) {
		super(stage);
		setBackgroundColor(Color.lightGray);

		// create and configure actors
		franklin = new Actor();
		add(this.franklin);
		franklin.center();

		franklin.setRangeStyle(RangeStyle.BOUNCE);
		franklin.penDown();
	}

	@Override
	public void start() {
		// start scene updates - periodically update the scene
		setTickPeriod(50);
	}

	@Override
	public void stop() {
		// stop scene updates
		setTickPeriod(0);
	}

	@Override
	protected void onMousePressed(int x, int y, MouseEvent detail) {
		getStage().changeScene(IntroScene.NAME, TransitionEffect.MOVE_RIGHT, 1000);
	}

	@Override
	protected void onTick() {
		// update scene
		franklin.setDirection(Math.random() * 360);
		franklin.step(10);
	}
}
