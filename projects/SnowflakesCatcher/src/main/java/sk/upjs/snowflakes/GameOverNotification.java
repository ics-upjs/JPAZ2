package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * A panel displaying game over notification in the game scene.
 */
public class GameOverNotification extends Pane {

	/**
	 * The stage.
	 */
	private Stage stage;

	/**
	 * Constructs new panel.
	 * 
	 * @param stage
	 *            the stage.
	 */
	public GameOverNotification(Stage stage) {
		// resize pane
		super(715, 64);
		this.stage = stage;

		setBorderWidth(0);
		setTransparentBackground(true);

		// paint content of this pane (stamp an image)
		Turtle painter = new Turtle();
		painter.setShape(new ImageShape("images", "game_over.png"));
		add(painter);
		painter.center();
		painter.stamp();
		remove(painter);
	}

	/**
	 * Displays the panel slowly.
	 */
	public void showSlowly() {
		setTransparency(1);
		setTickPeriod(100);
	}

	@Override
	protected void onTick() {
		// change transparency
		setTransparency(getTransparency() - 0.04);

		// if the panel is fully displayed, then stop ticking
		if (getTransparency() == 0) {
			setTickPeriod(0);
		}
	}

	@Override
	protected boolean onCanClick(int x, int y) {
		return true;
	}

	@Override
	protected void onMouseClicked(int x, int y, MouseEvent detail) {
		if (detail.getButton() == MouseEvent.BUTTON1) {
			stage.changeScene(IntroScene.NAME, TransitionEffect.FADE_OUT_WHITE_FADE_IN, 1500);
		}
	}
}
