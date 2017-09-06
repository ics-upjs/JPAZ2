package ${package};

import java.awt.Color;
import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * The scene with introduction.
 */
public class IntroScene extends Scene {

	/**
	 * Identifier (name) of this scene.
	 */
	public static final String NAME = "Intro";

	/**
	 * Constructs the scene.
	 * 
	 * @param stage
	 *            the stage where the scene will be shown.
	 */
	public IntroScene(Stage stage) {
		super(stage);
		setBackgroundColor(Color.white);
	}

	@Override
	public void start() {
		// draw message using a random color
		clear();

		Turtle painter = new Turtle();
		int r = (int) (Math.random() * 256);
		int g = (int) (Math.random() * 256);
		int b = (int) (Math.random() * 256);
		painter.setPenColor(new Color(r, g, b));

		add(painter);
		painter.center();
		painter.setDirection(90);
		painter.setFont(painter.getFont().deriveFont(30.0f));
		painter.printCenter("Click to start");
		remove(painter);
	}

	@Override
	public void stop() {

	}

	@Override
	protected void onMousePressed(int x, int y, MouseEvent detail) {
		getStage().changeScene(MainScene.NAME, TransitionEffect.MOVE_RIGHT, 1000);
	}
}
