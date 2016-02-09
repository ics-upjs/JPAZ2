package sk.upjs.jpaz2;

import java.awt.*;

/**
 * The basic interface prescribing basic methods for all objects able to live on
 * a pane.
 */
public interface PaneObject {
	/**
	 * Gets the parent pane.
	 * 
	 * @return the pane containing the object or null, if the object does not
	 *         belong to a pane.
	 */
	Pane getPane();

	/**
	 * Sets the parent pane. The pane object is first removed from its current
	 * parent pane and later added to the desired parent pane.
	 * 
	 * @param p
	 *            the new parent pane
	 */
	void setPane(Pane p);

	/**
	 * Draws the visual representation of the object to the given graphics
	 * considering that it is graphics of the parent pane.
	 * 
	 * @param g
	 *            the graphics used to draw the object
	 */
	void paintToPaneGraphics(Graphics2D g);
}
