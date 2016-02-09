package sk.upjs.jpaz2.inspector;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for Color values.
 */
@SuppressWarnings("serial")
class OIColorRenderer extends DefaultTableCellRenderer {
	/**
	 * Rendered color value
	 */
	private Color color;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		color = (Color) value;
		if (color != null)
			setToolTipText("RGB: " + color.getRed() + ", " + color.getGreen()
					+ ", " + color.getBlue());
		
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		if (color != null) {
			g2.setPaint(color);
			g2.fillRect(2, 2, getWidth() - 4, getHeight() - 3);
			g2.setColor(Color.black);
			g2.drawRect(1, 1, getWidth() - 3, getHeight() - 2);
		} else {
			g2.setColor(Color.black);
			g2.drawRect(1, 1, getWidth() - 3, getHeight() - 2);
			g2.drawLine(1, 1, getWidth() - 2, getHeight() - 2);
			g2.drawLine(1, getHeight() - 2, getWidth() - 2, 1);
		}
	}
}
