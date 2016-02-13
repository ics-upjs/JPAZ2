package sk.upjs.jpaz2;

import java.awt.*;

/**
 * The interface for painters to pane graphics content
 */
public interface PanePainter {
	/**
	 * Paints to the pane graphics.
	 * 
	 * @param graphics
	 *            the graphics
	 */
	void doPaint(Graphics2D graphics);
}
