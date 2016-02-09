package sk.upjs.jpaz2.inspector;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for OIRowSeparatorValue.
 */
@SuppressWarnings("serial")
class OIRowSeparatorRenderer extends DefaultTableCellRenderer {
	
	/**
	 * Column index of the current cell
	 */
	private int column;
	
	/**
	 * Table for which the rendering is realized
	 */
	private JTable table;
	
	/**
	 * Value of the current cell
	 */
	private OIRowSeparatorValue value;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		// copy all current parameters 
		this.column = column;
		this.table = table;
		
		// set text that is shown
		this.value = (OIRowSeparatorValue)value;
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// -- this code is written in order to solve the absence of colspan parameter for cells
		
		// compute shift according to information which cell is rendered
		TableColumnModel cm = table.getColumnModel();		
		int pixelShift = 0;
		int totalWidth = 0;
		int totalHeight = getHeight(); 
		
		for (int i=0; i<column; i++)
			pixelShift += cm.getColumn(i).getWidth();
		
		for (int i=0; i<cm.getColumnCount(); i++)
			totalWidth += cm.getColumn(i).getWidth();
		
		g2.translate(-pixelShift, 0);
		
		// paint gradient
		g2.setPaint(new GradientPaint(0, 0, Color.LIGHT_GRAY, 0, table.getRowHeight(), Color.DARK_GRAY));
		g2.fillRect(0, 0, totalWidth, totalHeight);

		// draw ellapsed/collapsed symbol
		if (value.collapsed || (!value.active)) {
			Polygon p = new Polygon();
			p.addPoint(4, 3);
			p.addPoint(4, 13);
			p.addPoint(9, 8);
			g2.setPaint(value.active ? Color.WHITE : new Color(200, 200, 200));
			g2.fill(p);
		} else {
			Polygon p = new Polygon();
			p.addPoint(2, 6);
			p.addPoint(11, 6);
			p.addPoint(6, 11);
			g2.setPaint(Color.WHITE);
			g2.fill(p);
		}
			
		// draw value
		g2.setPaint(value.active ? Color.WHITE : new Color(230, 230, 230));
		g2.setFont(g2.getFont().deriveFont(Font.BOLD));
		g2.drawString(value.text, 15, 13);
		g2.setPaint(Color.white);
		g2.drawLine(0, totalHeight-1, totalWidth, totalHeight-1);
	}
}