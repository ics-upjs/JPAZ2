package sk.upjs.jpaz2;

import java.awt.*;

/**
 * The interface for painters to pane graphics content
 */
interface PanePainter {
	
	/**
	 * Paints to the pane graphics.
	 * 
	 * @param graphics
	 *            the graphics
	 */
	void paint(Graphics2D graphics);
}
