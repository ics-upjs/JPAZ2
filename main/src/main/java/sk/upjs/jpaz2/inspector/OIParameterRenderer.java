package sk.upjs.jpaz2.inspector;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for OIParameterType (a parameter record).
 */
@SuppressWarnings("serial")
class OIParameterRenderer extends DefaultTableCellRenderer {	
	
	/**
	 * Constructs the type name renderer.
	 */
	public OIParameterRenderer() {
		// set border
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		OIParameterValue parameterRecord = (OIParameterValue)value;
		
		if (parameterRecord.name != null)
			setValue("<html><font color=red>" + parameterRecord.name + "</font>&nbsp;(" + parameterRecord.type.getSimpleName() + ")</html>");
		else
			setValue(parameterRecord.type.getSimpleName());
			
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
