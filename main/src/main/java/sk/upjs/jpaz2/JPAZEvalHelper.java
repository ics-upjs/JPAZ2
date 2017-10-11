package sk.upjs.jpaz2;

import java.awt.event.MouseEvent;

/**
 * The class JPAZEvalHelper contains methods simplifying automatic evaluation of
 * JPAZ-based codes.
 */
public final class JPAZEvalHelper {

	// ---------------------------------------------------------------------------------------------------
	// Private constructor
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Private constructor forbidding creation of class instances
	 */
	private JPAZEvalHelper() {
	}

	/**
	 * Fires a mouse event in a pane.
	 * 
	 * @param pane
	 *            the pane where the event will be fired.
	 * @param x
	 *            the x-coordinate of the mouse event.
	 * @param y
	 *            the y-coordinate of the mouse event.
	 * @param type
	 *            the type of the event.
	 * @param detail
	 *            the additional information about the event.
	 */
	public static void fireMouseEventInPane(Pane pane, int x, int y, int type, MouseEvent detail) {
		pane.fireMouseEvent(x, y, type, detail, true);
	}
}
