package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.beans.PropertyDescriptor;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for property names.
 */
@SuppressWarnings("serial")
class OIPropertyNameRenderer extends DefaultTableCellRenderer {
	private PropertyDescriptor pd;
	
	/**
	 * Constructs the property name renderer
	 */
	public OIPropertyNameRenderer() {
		setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		pd = ((OIPropertyNameValue)value).pd;
		
		setValue(pd.getDisplayName());
		setToolTipText(pd.getPropertyType().getSimpleName());
		if (pd.getWriteMethod() != null)
			setForeground(Color.black);
		else
			setForeground(Color.gray);
		
		setFont(getFont().deriveFont(Font.BOLD));

		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.LIGHT_GRAY);
		g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1);
	}
}
