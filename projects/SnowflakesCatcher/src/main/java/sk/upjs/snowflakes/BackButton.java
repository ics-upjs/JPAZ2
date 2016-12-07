package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * A back button displayed on the game scene.
 */
public class BackButton extends Pane {

	/**
	 * The stage.
	 */
	private Stage stage;

	/**
	 * Constructs a back button.
	 * 
	 * @param game
	 *            the game.
	 */
	public BackButton(Stage stage) {
		// create pane with proper size
		super(32, 32);
		this.stage = stage;

		setBorderWidth(0);
		setTransparentBackground(true);

		// paint button image
		Turtle painter = new Turtle();
		painter.setShape(new ImageShape("images", "back_icon.png"));
		add(painter);
		painter.center();
		painter.stamp();
		remove(painter);
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
