package sk.upjs.jpaz2;

import java.awt.Graphics2D;
import java.util.*;

/**
 * Composite pane painter that encapsulates collection of pane painters.
 */
class CompositePanePainter implements PanePainter {

	/**
	 * List of pane painters that form the painter.
	 */
	private final ArrayList<PanePainter> panePainters = new ArrayList<>();

	/**
	 * Constructs composite pane painter.
	 * 
	 * @param panePainters
	 *            the painters that form the painter.
	 */
	public CompositePanePainter(List<PanePainter> panePainters) {
		this.panePainters.addAll(panePainters);
	}

	@Override
	public void paint(Graphics2D graphics) {
		for (PanePainter painter : panePainters) {
			Graphics2D g2d = (Graphics2D) graphics.create();
			painter.paint(g2d);
			g2d.dispose();
		}
	}
}
