package sk.upjs.snowflakes;

import java.awt.Color;
import java.awt.Font;

import sk.upjs.jpaz2.*;

/**
 * A class representing a scoreboard that displays the number of eaten
 * snowflakes.
 */
public class Scoreboard extends Pane {

	/**
	 * Score displayed on this scoreboard.
	 */
	private int score = 0;

	/**
	 * Turtle used as a painter.
	 */
	private Turtle painter;

	/**
	 * Construcs a new scoreboard
	 */
	public Scoreboard() {
		// set size calling the constructor of superclass
		super(60, 35);
		setBorderWidth(0);
		setTransparentBackground(true);

		painter = new Turtle();
		painter.setVisible(false);
		painter.setDirection(90);
		painter.setPenColor(Color.white);
		painter.setFont(new Font("Lucida Sans", Font.BOLD, 20));
		add(painter);
		painter.center();

		resetScore();
	}

	/**
	 * Sets the score to 0.
	 */
	public void resetScore() {
		score = 0;
		repaintScore();
	}

	/**
	 * Increase score by given number of points.
	 * 
	 * @param points
	 *            the number of points to be added.
	 */
	public void increaseScore(int points) {
		this.score += points;
		repaintScore();
	}

	/**
	 * Returns the current score.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Repaints the displayed score.
	 */
	private void repaintScore() {
		clear();
		painter.printCenter(Integer.toString(score));
	}
}
