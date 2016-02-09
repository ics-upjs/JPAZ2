package sk.upjs.jpaz2.inspector;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * The basic renderer based on displaying toString value.
 */
@SuppressWarnings("serial")
class OIDefaultCellRenderer extends DefaultTableCellRenderer {

	/**
	 * Constructs the renderer
	 */
	public OIDefaultCellRenderer() {
		setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
		setFont(getFont().deriveFont(0));
		putClientProperty("html.disable", true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setValue(value);
		return this;
	}
}
