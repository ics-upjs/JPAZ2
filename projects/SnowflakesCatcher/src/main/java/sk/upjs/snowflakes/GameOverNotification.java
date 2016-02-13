package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.games.Game;

/**
 * A panel displaying game over notification.
 */
public class GameOverNotification extends Pane {

	/**
	 * The game.
	 */
	private Game game;

	/**
	 * Constructs new panel.
	 * 
	 * @param game
	 *            the game.
	 */
	public GameOverNotification(Game game) {
		// resize pane
		super(715, 64);
		this.game = game;

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
		if (getTransparency() == 0)
			setTickPeriod(0);
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
