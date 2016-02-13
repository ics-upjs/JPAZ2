package sk.upjs.snowflakes;

import sk.upjs.jpaz2.*;

public class LostSnowflakesPanel extends Pane {

	/**
	 * Maximum number of snowflakes that can be lost.
	 */
	private static final int MAX_SNOWFLAKES = 6;

	/**
	 * Number of snowflakes in this panel.
	 */
	private int snowflakeCount = 0;

	/**
	 * Painter for painting snowflakes.
	 */
	private Turtle painter;

	public LostSnowflakesPanel() {
		// set size calling the constructor of superclass with parameters
		super(40, 235);

		setBorderWidth(0);
		setTransparentBackground(true);

		painter = new Turtle();
		painter.setVisible(false);
		painter.setShape(new ImageShape("images", "lost_snowflake.png"));
		add(painter);
	}

	/**
	 * Resets the panel and sets the number of lost snowflakes to 0.
	 */
	public void reset() {
		snowflakeCount = 0;
		repaintPanel();
	}

	/**
	 * Returns whether this panel is full (allowed number of lost snowflakes is
	 * reached).
	 */
	public boolean isFull() {
		return (snowflakeCount > MAX_SNOWFLAKES);
	}

	/**
	 * Changes the number of lost snowflakes by given number of snowflakes.
	 */
	public void changeBy(int number) {
		snowflakeCount += number;
		if (snowflakeCount < 0)
			snowflakeCount = 0;

		repaintPanel();
	}

	/**
	 * Repaints the panel.
	 */
	private void repaintPanel() {
		clear();

		painter.center();
		painter.setY(getHeight() - 20);
		for (int i = 0; i < Math.min(snowflakeCount, MAX_SNOWFLAKES); i++) {
			painter.stamp();
			painter.setY(painter.getY() - 40);
		}
	}
}
