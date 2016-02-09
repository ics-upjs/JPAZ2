package sk.upjs.jpaz2.inspector;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for OITypeValue (class type).
 */
@SuppressWarnings("serial")
class OITypeRenderer extends DefaultTableCellRenderer {	
	
	/**
	 * Constructs the type name renderer.
	 */
	public OITypeRenderer() {
		// set border
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setValue((((OITypeValue)value).type).getSimpleName());
		setFont(getFont().deriveFont(Font.BOLD));
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// draw separator line
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.LIGHT_GRAY);
		g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1);
	} 
}
