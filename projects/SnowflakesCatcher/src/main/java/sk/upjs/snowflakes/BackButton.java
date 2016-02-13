package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.games.Game;

/**
 * A back button displayed on the game screen.
 */
public class BackButton extends Pane {

	/**
	 * The game.
	 */
	private Game game;

	/**
	 * Constructs a back button.
	 * 
	 * @param game
	 *            the game.
	 */
	public BackButton(Game game) {
		// create pane with proper size
		super(32, 32);
		this.game = game;

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
			game.changeScene(IntroScene.NAME);
		}
	}
}
