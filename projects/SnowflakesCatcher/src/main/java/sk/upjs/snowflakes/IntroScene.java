package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * Scene (pane) with intro to game that includes a simple menu.
 */
public class IntroScene extends Scene {

	/**
	 * Name of the scene.
	 */
	public static final String NAME = "Intro";

	/**
	 * Animated snowflake close to "START"
	 */
	private Turtle snowflakeStart;

	/**
	 * Animated snowflake close to "EXIT"
	 */
	private Turtle snowflakeExit;

	/**
	 * Turtle that serves as an on/off switch for playing music
	 */
	private MusicOnOffSwitch musicOnOff;

	/**
	 * Constructs the intro scene with menu.
	 */
	public IntroScene(Stage stage) {
		super(stage);

		// construct all objects in this pane
		prepareScreen();
	}

	/**
	 * Initializes screen (creates all objects on the screen and paints the
	 * background).
	 */
	private void prepareScreen() {
		setBorderWidth(0);

		// paint background
		Turtle painter = new Turtle();
		painter.setShape(new ImageShape("images", "intro.jpg"));
		add(painter);
		painter.center();
		painter.stamp();
		remove(painter);

		// create animated snowflakes
		ImageShape snowflakeShape = new ImageShape("images", "snowflake1.gif");

		snowflakeStart = new Turtle();
		snowflakeStart.setShape(snowflakeShape);
		snowflakeStart.setPosition(70, 326);
		add(snowflakeStart);

		snowflakeExit = new Turtle();
		snowflakeExit.setShape(snowflakeShape);
		snowflakeExit.setPosition(70, 405);
		// set initial animation frame randomly
		snowflakeExit.setFrameIndex((int) (Math.random() * snowflakeExit.getFrameCount()));
		add(snowflakeExit);

		// create and add the music on/off switch
		musicOnOff = new MusicOnOffSwitch(getStage());
		musicOnOff.setPosition(5, 5);
		add(musicOnOff);
	}

	/**
	 * Returns whether given point is a part of the START button.
	 */
	private boolean isOverSTART(int x, int y) {
		return (snowflakeStart.getX() - 20 <= x) && (x <= snowflakeStart.getX() + 180)
				&& (snowflakeStart.getY() - 20 <= y) && (y <= snowflakeStart.getY() + 20);
	}

	/**
	 * Returns whether given point is a part of the EXIT button.
	 */
	private boolean isOverEXIT(int x, int y) {
		return (snowflakeExit.getX() - 20 <= x) && (x <= snowflakeExit.getX() + 180) && (snowflakeExit.getY() - 20 <= y)
				&& (y <= snowflakeExit.getY() + 20);
	}

	/**
	 * Starts the scene.
	 */
	public void start() {
		musicOnOff.updateView();
		snowflakeStart.setShapeAnimation(true);
		snowflakeExit.setShapeAnimation(true);
	}

	/**
	 * Stops the scene.
	 */
	public void stop() {
		snowflakeStart.setShapeAnimation(false);
		snowflakeExit.setShapeAnimation(false);
	}

	@Override
	protected void onMouseClicked(int x, int y, MouseEvent detail) {
		// simulation of buttons without creating a button as an extension of
		// the Pane class
		if (detail.getButton() == MouseEvent.BUTTON1) {
			if (isOverSTART(x, y)) {
				getStage().changeScene(GameScene.NAME, TransitionEffect.FADE_OUT_WHITE_FADE_IN, 1500);
				return;
			}

			if (isOverEXIT(x, y)) {
				System.exit(0);
			}
		}
	}

	@Override
	protected boolean onCanClick(int x, int y) {
		// super.onCanClick(x, y) can be replaced with musicOnOff.contains(x, y)
		// - indeed, there are no other subpanes in this pane
		return super.onCanClick(x, y) || isOverSTART(x, y) || isOverEXIT(x, y);
	}
}
